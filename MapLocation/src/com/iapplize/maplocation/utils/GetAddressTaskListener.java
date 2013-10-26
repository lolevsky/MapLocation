package com.iapplize.maplocation.utils;

import com.google.android.gms.maps.model.Marker;

public interface GetAddressTaskListener {
	public void onAddressTaskResult(String address, Marker marker);
}
