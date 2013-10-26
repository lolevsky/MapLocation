package com.iapplize.maplocation.utils;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.iapplize.maplocation.R;

public class GetAddressTask extends AsyncTask<Void, Void, String> {

	Context mContext;
	Marker mMarker;
	LatLng latLon;
	GetAddressTaskListener mListener;

	public GetAddressTask(Context context, Marker marker,
			GetAddressTaskListener listener) {
		super();
		mContext = context;
		mMarker = marker;
		latLon = mMarker.getPosition();
		mListener = listener;
	}

	@Override
	protected String doInBackground(Void... params) {

		try {
			Geocoder geo = new Geocoder(mContext, Locale.US);
			List<Address> addresses = geo.getFromLocation(latLon.latitude,
					latLon.longitude, 1);
			if (addresses.isEmpty()) {
				return mContext.getString(R.string.not_found);
			} else {
				if (addresses.size() > 0) {

					StringBuilder sb = new StringBuilder();

					if (addresses.get(0).getAddressLine(0) != null) {
						sb.append(addresses.get(0).getAddressLine(0)).append(
								" ");
					}

					if (addresses.get(0).getLocality() != null) {
						sb.append(addresses.get(0).getLocality()).append(" ");
					}

					if (addresses.get(0).getCountryName() != null) {
						sb.append(addresses.get(0).getCountryName());
					}

					return sb.toString();
				}
			}
		} catch (Exception e) {
			return mContext.getString(R.string.not_found);
		}
		return mContext.getString(R.string.not_found);

	}

	@Override
	protected void onPostExecute(String address) {

		if (mListener != null) {
			mListener.onAddressTaskResult(address, mMarker);
		}

	}

}
