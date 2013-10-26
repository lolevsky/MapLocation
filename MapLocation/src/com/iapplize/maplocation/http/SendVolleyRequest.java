package com.iapplize.maplocation.http;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.iapplize.maplocation.MainApplication;
import com.iapplize.maplocation.R;

public class SendVolleyRequest implements ConnectionInterface {

	private static String TAG = "SendVolleyRequest";

	public static void sendStringRequest(final MainApplication mApp,
			String requestName, List<NameValuePair> nvps) {

		String url = BASE_URL + requestName + "?"
				+ URLEncodedUtils.format(nvps, "UTF-8");

		StringRequest sr = new StringRequest(Request.Method.GET, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.i(TAG, response);
						Toast.makeText(mApp,
								R.string.check_in_completed_successfully,
								Toast.LENGTH_SHORT).show();
					};
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						if (error == null || error.getMessage() == null) {
							Log.i(TAG, "error");
						} else {
							Log.i(TAG, error.getMessage());
						}

						Toast.makeText(mApp, R.string.check_in_error,
								Toast.LENGTH_SHORT).show();

					}

				});

		if (mApp.mRequestQueue != null) {

			mApp.mRequestQueue.add(sr);
			Log.d(TAG, "add - " + sr.getUrl());
		} else {
			Log.e(TAG, "Error sending");
		}

	}

	public static void sendJsonRequest(final MainApplication mApp,
			String requestName, JSONObject json , final GetCheckinsListener getCheckinsListener) {

		String url = BASE_URL + requestName;

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.POST, url, json,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {

						Log.i(TAG, response.toString());
						if(getCheckinsListener != null){
							getCheckinsListener.onGetCheckinsResponse(response);
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						if (error == null || error.getMessage() == null) {
							Log.i(TAG, "error");
						} else {
							Log.i(TAG, error.getMessage());
						}

						Toast.makeText(mApp, R.string.error,
								Toast.LENGTH_SHORT).show();
					}

				});
		if (mApp.mRequestQueue != null) {

			mApp.mRequestQueue.add(jsObjRequest);
			Log.d(TAG, "add - " + jsObjRequest.getUrl());
		} else {
			Log.e(TAG, "Error sending");
		}

	}
}
