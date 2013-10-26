package com.iapplize.maplocation.fragment;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.PlusClient;
import com.iapplize.maplocation.MainApplication;
import com.iapplize.maplocation.R;
import com.iapplize.maplocation.activity.MainActivity;
import com.iapplize.maplocation.dialog.CheckInDialog;
import com.iapplize.maplocation.http.ConnectionInterface;
import com.iapplize.maplocation.http.GetCheckinsListener;
import com.iapplize.maplocation.http.SendVolleyRequest;
import com.iapplize.maplocation.utils.GetAddressTask;
import com.iapplize.maplocation.utils.GetAddressTaskListener;
import com.iapplize.maplocation.utils.UserData;

public class MainMapFragment extends SupportMapFragment implements
		OnMarkerClickListener, OnInfoWindowClickListener, OnMarkerDragListener,
		GetAddressTaskListener, OnMapLongClickListener, ConnectionInterface {

	MainApplication mApp;
	
	private GoogleMap mMap;
	private Marker mMarker;

	private Map<Marker, UserData> mUserDataMap = new HashMap<Marker, UserData>();

	static MainMapFragment instance;

	public State mState = State.LIVE;

	public enum State {
		LIVE, MY_LOCATION, MY_CHECKINS, ALL_LAST_CHECKINS
	}

	public static MainMapFragment getInstance() {

		if (instance == null) {
			instance = new MainMapFragment();
		}

		return instance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		
		mApp = (MainApplication)getActivity().getApplication();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mMap == null) {
			mMap = getMap();

			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mMap.setMyLocationEnabled(true);
			mMap.setTrafficEnabled(false);
			mMap.setIndoorEnabled(false);

			mMap.getUiSettings().setZoomControlsEnabled(false);
			mMap.getUiSettings().setCompassEnabled(true);
			mMap.getUiSettings().setMyLocationButtonEnabled(true);

			mMap.setOnMarkerClickListener(this);
			mMap.setOnInfoWindowClickListener(this);
			mMap.setOnMarkerDragListener(this);
			mMap.setOnMapLongClickListener(this);

			mMap.setInfoWindowAdapter(infoWindowAdapter);
		}
	}

	public void checkIn() {
		if (mState == State.MY_LOCATION && mMarker != null) {
			FragmentManager fm = getActivity().getSupportFragmentManager();
			
			UserData ud = mUserDataMap.get(mMarker);
			if(ud == null){
				ud = new UserData();
			}
			
			CheckInDialog checkInDialog = CheckInDialog.newInstance(
					ud.address, mMarker.getPosition().latitude,
					mMarker.getPosition().longitude);
			checkInDialog.show(fm, checkInDialog.getClass().getSimpleName());
		}
	}

	public void updateState(State state) {
		mState = state;

		if (mMap != null && getActivity() != null) {
			mMarker = null;
			mMap.clear();
			mUserDataMap.clear();
			
			switch (state) {
			case LIVE:
			case MY_LOCATION:

				if (((MainActivity) getActivity()).mCurrentLocation != null) {
					setLocation(((MainActivity) getActivity()).mCurrentLocation);
				}

				mMap.setOnInfoWindowClickListener(this);
				mMap.setOnMapLongClickListener(this);

				break;
			case MY_CHECKINS:

				mMap.setOnMarkerClickListener(null);
				mMap.setOnMapLongClickListener(null);

				mMarker = null;
				
				boolean isFaild = false;
				
				JSONObject jObj = new JSONObject();
				
				try {
					jObj.put(PAR_USERID, mApp.getPlusClient().getCurrentPerson().getId());
				} catch (JSONException e) {
					isFaild = true;
				}
				
				if(!isFaild){
					SendVolleyRequest.sendJsonRequest(mApp, CALL_GETCHECKINS, jObj, new GetCheckinsListener() {
						
						@SuppressLint("NewApi")
						@Override
						public void onGetCheckinsResponse(JSONObject jObj) {
							if(mState == State.MY_CHECKINS){
								
								if(!jObj.isNull(PAR_DATA)){
									JSONArray jArray = jObj.optJSONArray(PAR_DATA);
									
									for(int i = 0; i < jArray.length() ; i ++){
										UserData ud = new UserData();
										
										String name = jArray.optJSONObject(i).optString(PAR_USERNAME, "");
										String lName = jArray.optJSONObject(i).optString(PAR_USERLASTNAME, "");
										String imageUrl = jArray.optJSONObject(i).optString(PAR_USERIMAGEURL, "");
										String lat = jArray.optJSONObject(i).optString(PAR_LAT, "");
										String lon = jArray.optJSONObject(i).optString(PAR_LON, "");
										
										ud.userName = name + " " + lName;
										ud.userImageUrl = imageUrl;
										
										Location loc = new Location("user location");
										loc.setLatitude(Double.valueOf(lat));
										loc.setLongitude(Double.valueOf(lon));
										
										
										
										Marker marker = addMarker(loc, ud, false);
										
										GetAddressTask getAddressTask = new GetAddressTask(getActivity(),
												marker, MainMapFragment.this);
							
										if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
											getAddressTask.executeOnExecutor(
													AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
										} else {
											getAddressTask.execute((Void[]) null);
										}
										
										//mUserDataMap.put(marker, ud);
									}
								}
							}
						}
					});
				}
				
				break;
			case ALL_LAST_CHECKINS:

				mMap.setOnMarkerClickListener(null);
				mMap.setOnMapLongClickListener(null);

				mMarker = null;
				
				SendVolleyRequest.sendJsonRequest(mApp, CALL_GETCHECKINS, null, new GetCheckinsListener() {
					
					@SuppressLint("NewApi")
					@Override
					public void onGetCheckinsResponse(JSONObject jObj) {
						if(mState == State.ALL_LAST_CHECKINS){
							
							if(!jObj.isNull(PAR_DATA)){
								JSONArray jArray = jObj.optJSONArray(PAR_DATA);
								
								for(int i = 0; i < jArray.length() ; i ++){
									UserData ud = new UserData();
									
									String name = jArray.optJSONObject(i).optString(PAR_USERNAME, "");
									String lName = jArray.optJSONObject(i).optString(PAR_USERLASTNAME, "");
									String imageUrl = jArray.optJSONObject(i).optString(PAR_USERIMAGEURL, "");
									String lat = jArray.optJSONObject(i).optString(PAR_LAT, "");
									String lon = jArray.optJSONObject(i).optString(PAR_LON, "");
									
									ud.userName = name + " " + lName;
									ud.userImageUrl = imageUrl;
									
									Location loc = new Location("user location");
									loc.setLatitude(Double.valueOf(lat));
									loc.setLongitude(Double.valueOf(lon));
									
									
									
									Marker marker = addMarker(loc, ud, false);
									
									GetAddressTask getAddressTask = new GetAddressTask(getActivity(),
											marker, MainMapFragment.this);
						
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
										getAddressTask.executeOnExecutor(
												AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
									} else {
										getAddressTask.execute((Void[]) null);
									}
									
									//mUserDataMap.put(marker, ud);
								}
							}
						}
					}
				});

				break;
			}

		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	
	private Marker addMarker(Location loc, UserData ud, boolean isDraggable){
		
		Marker marker = mMap
				.addMarker(new MarkerOptions()
				.draggable(isDraggable)
						.position(
								new LatLng(loc.getLatitude(), loc
										.getLongitude()))
						.title(getString(R.string.loading))
						.draggable(true)
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

		marker.setDraggable(isDraggable);
		
		mUserDataMap.put(marker, ud);

		return marker;
	}
	
	public void setLocation(Location loc) {
		if (mMarker == null && mState == State.MY_LOCATION) {

			UserData ud = new UserData();

			PlusClient user = mApp.getPlusClient();

			ud.userName = user.getCurrentPerson().getName().getGivenName()
					+ " " + user.getCurrentPerson().getName().getFamilyName();
			ud.userImageUrl = user.getCurrentPerson().getImage().getUrl();
			
			mMarker = addMarker(loc, ud, mState == State.LIVE?false:true);
			
			onMarkerDragEnd(mMarker);
		}
	}
	
	public void setLocation(Location loc, String recognitionState) {

		if (mState == State.LIVE) {

			//mMap.clear();
			mUserDataMap.clear();
			
			UserData ud = new UserData();

			PlusClient user = mApp.getPlusClient();

			ud.userName = user.getCurrentPerson().getName().getGivenName()
					+ " " + user.getCurrentPerson().getName().getFamilyName();
			ud.userImageUrl = user.getCurrentPerson().getImage().getUrl();
			ud.address = recognitionState;
			
			if(mMarker == null){
				mMarker = addMarker(loc, ud, mState == State.LIVE?false:true);
			}else{
				mMarker.setPosition(new LatLng(loc.getLatitude(), loc.getLongitude()));
				mMarker.setDraggable(false);
				mUserDataMap.put(mMarker, ud);
			}

			mMarker.showInfoWindow();
			
			onMarkerDragEnd(mMarker);
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		marker.showInfoWindow();

		// Toast.makeText(getActivity(), marker.getTitle(), Toast.LENGTH_SHORT)
		// .show();

		return false;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		marker.hideInfoWindow();
	}

	@Override
	public void onMarkerDrag(Marker marker) {

	}

	@SuppressLint("NewApi")
	@Override
	public void onMarkerDragEnd(Marker marker) {
		
		if(mState == State.MY_LOCATION){
		
			if (Geocoder.isPresent()) {
	
				GetAddressTask getAddressTask = new GetAddressTask(getActivity(),
						marker, this);
	
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					getAddressTask.executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
				} else {
					getAddressTask.execute((Void[]) null);
				}
	
			}
	
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					marker.getPosition(), mMap.getCameraPosition().zoom);
			mMap.animateCamera(cameraUpdate);
		}else if(mState == State.LIVE){
			
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					marker.getPosition(), mMap.getCameraPosition().zoom);
			
			mMap.animateCamera(cameraUpdate);
		}
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		marker.hideInfoWindow();
	}

	@Override
	public void onMapLongClick(LatLng latlng) {
		mMarker.setPosition(latlng);
		mMarker.hideInfoWindow();
		onMarkerDragEnd(mMarker);
	}

	@Override
	public void onAddressTaskResult(String address, Marker marker) {

		//marker.setTitle(address);

		UserData ud = mUserDataMap.get(marker);
		if(ud == null){
			ud = new UserData();
		}
		
		ud.address = address;
		
		mUserDataMap.put(marker, ud);
		
		
//		onMarkerDragEnd(marker);

		if(mState == State.LIVE || mState == State.MY_LOCATION ){
			marker.showInfoWindow();
		}
	}

	private InfoWindowAdapter infoWindowAdapter = new InfoWindowAdapter() {

		@Override
		public View getInfoWindow(Marker marker) {
			return null;
		}

		@Override
		public View getInfoContents(Marker marker) {

			View v = getActivity().getLayoutInflater().inflate(
					R.layout.info_window_item, null);

			UserData ud = mUserDataMap.get(marker);

			if(ud != null){
				TextView name = (TextView) v.findViewById(R.id.name);
				TextView location = (TextView) v.findViewById(R.id.location);
	
				final ImageView networkimage = ImageView.class.cast(v.findViewById(R.id.avatar));
				mApp.mImageLoader.get(ud.userImageUrl, getImageListener(marker, networkimage, R.drawable.ic_launcher, R.drawable.ic_action_btn_menu));
	
				name.setText(ud.userName);
				location.setText(ud.address);
			}
			
			return v;
		}
		
		 public ImageListener getImageListener(final Marker marker,final ImageView view,
		            final int defaultImageResId, final int errorImageResId) {
		        return new ImageListener() {
		            @Override
		            public void onErrorResponse(VolleyError error) {
		                if (errorImageResId != 0) {
		                    view.setImageResource(errorImageResId);
		                }

		            }

		            @Override
		            public void onResponse(ImageContainer response, boolean isImmediate) {
		                if (response.getBitmap() != null) {
		                    view.setImageBitmap(response.getBitmap());
		                } else if (defaultImageResId != 0) {
		                    view.setImageResource(defaultImageResId);
		                }
		                
	                	if(marker.isInfoWindowShown()){
	                		marker.hideInfoWindow();
		                	marker.showInfoWindow();
		                }		               
		            }
		        };
		    }
	};

}
