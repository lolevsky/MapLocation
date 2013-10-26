package com.iapplize.maplocation.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.iapplize.maplocation.MainApplication;
import com.iapplize.maplocation.R;
import com.iapplize.maplocation.dialog.ErrorDialogFragment;
import com.iapplize.maplocation.fragment.MainMapFragment;
import com.iapplize.maplocation.fragment.MainMapFragment.State;
import com.iapplize.maplocation.service.ActivityRecognitionService;

public class MainActivity extends ActionBarActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		ListView.OnItemClickListener, LocationListener{

	MainApplication mApp;
	
	private LocationClient mLocationClient;
	public Location mCurrentLocation;
	private ConnectionResult mConnectionResult;
	
	private ActivityRecognitionClient mArclient;
	private BroadcastReceiver receiver;
	private PendingIntent pIntent;
	
	String locationActivityString = "";
	
	private LocationRequest locationrequest;
	
	// Global constants
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private ActionBarDrawerToggle mDrawerToggle;
	DrawerLayout mDrawerLayout;
	FrameLayout mDrawerFrameLayout;

	private MainMapFragment mMainMapFragment;
	private ListView mDrawerList;

	

	private String[] mDrawerTitles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mApp = ((MainApplication) getApplication());
		
		mDrawerTitles = getResources().getStringArray(R.array.drawer_array);

		if (savedInstanceState == null) {
			mMainMapFragment = new MainMapFragment();
		} else {
			mMainMapFragment = MainMapFragment.getInstance();
		}

		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, mDrawerTitles));
		mDrawerList.setOnItemClickListener(this);

		initDrawer();
		initActionBar();

		if (servicesConnected()) {
			mLocationClient = new LocationClient(this, this, this);

			mArclient = new ActivityRecognitionClient(this, this, this);

			receiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {

					locationActivityString = "Activity :" + intent.getStringExtra("Activity")
							+ " " + "Confidence : "
							+ intent.getExtras().getInt("Confidence") + "n";

					if(mCurrentLocation != null){
						mMainMapFragment.setLocation(mCurrentLocation, locationActivityString);
					}
				}

			};
			
		} else {
			Toast.makeText(this, R.string.google_play_services_not_found,
					Toast.LENGTH_SHORT).show();
			finish();
		}

		if (savedInstanceState == null) {
			selectItem(0);
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		mLocationClient.connect();
		
		IntentFilter filter = new IntentFilter();

		filter.addAction("com.kpbird.myactivityrecognition.ACTIVITY_RECOGNITION_DATA");

		registerReceiver(receiver, filter);
		mArclient.connect();

	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		
		if (mArclient != null) {

			if (pIntent != null) {
				mArclient.removeActivityUpdates(pIntent);
			}

			mArclient.disconnect();

		}

		unregisterReceiver(receiver);
		
		
		super.onStop();
	}

	private void initDrawer() {
		FragmentTransaction t = this.getSupportFragmentManager()
				.beginTransaction();

		t.replace(R.id.right_fragment, mMainMapFragment);
		t.commit();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_action_btn_menu, /* nav drawer icon to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description */
		R.string.drawer_close /* "close drawer" description */
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {

			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {

			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void initActionBar() {

		ActionBar ab = getSupportActionBar();

		ab.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));

		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setDisplayShowCustomEnabled(true);
		ab.setDisplayShowTitleEnabled(true);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		
		if(mMainMapFragment.mState == State.MY_LOCATION){
			getMenuInflater().inflate(R.menu.main, menu);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.action_check_in:

			if (mMainMapFragment != null) {
				mMainMapFragment.checkIn();
			}

			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				/*
				 * Try the request again
				 */
				break;
			}
		}
	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Get the error code
			showError();
		}
		return false;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (mConnectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				mConnectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			showError();
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		if(mLocationClient.isConnected()){
			mCurrentLocation = mLocationClient.getLastLocation();
			mMainMapFragment.setLocation(mCurrentLocation);
			
			locationrequest = LocationRequest.create();
			
			locationrequest.setInterval(1000);
			
			mLocationClient.requestLocationUpdates(locationrequest, this);

		}

		if(mArclient.isConnected()){
			Intent intent = new Intent(this, ActivityRecognitionService.class);

			pIntent = PendingIntent.getService(this, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			
			mArclient.requestActivityUpdates(1000, pIntent);
		}
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	private void showError() {
		int errorCode = mConnectionResult.getErrorCode();
		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {
			// Create a new DialogFragment for the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);
			// Show the error dialog in the DialogFragment
			errorFragment.show(getSupportFragmentManager(), "Location Updates");
		}
	}

	private void selectItem(int position) {

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);

		if (mMainMapFragment != null) {
			switch (position) {
			case 0:
				mMainMapFragment.updateState(State.LIVE);
				break;
			case 1:
				mMainMapFragment.updateState(State.MY_LOCATION);
				break;
			case 2:
				mMainMapFragment.updateState(State.MY_CHECKINS);
				break;
			case 3:
				mMainMapFragment.updateState(State.ALL_LAST_CHECKINS);
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	
		supportInvalidateOptionsMenu();
		
		selectItem(position);
	}

	@Override
	public void onLocationChanged(Location location) {
		mCurrentLocation = location;
		
		if(mCurrentLocation != null){
			mMainMapFragment.setLocation(mCurrentLocation, locationActivityString);
		}
		
	}

}
