package com.iapplize.maplocation.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.plus.PlusClient;
import com.iapplize.maplocation.MainApplication;
import com.iapplize.maplocation.R;
import com.iapplize.maplocation.http.ConnectionInterface;
import com.iapplize.maplocation.http.SendVolleyRequest;

public class CheckInDialog extends DialogFragment implements ConnectionInterface{

	public static String LOCATION_NAME = "location_name";
	public static String LOCATION_LAT = "location_lat";
	public static String LOCATION_LON = "location_lon";
	
	PlusClient mUser;
	
	public static CheckInDialog newInstance(String locationName, Double lat, Double lon) {
		CheckInDialog f = new CheckInDialog();

	    Bundle args = new Bundle();
	    args.putString(LOCATION_NAME, locationName);
	    args.putString(LOCATION_LAT, String.valueOf(lat));
	    args.putString(LOCATION_LON, String.valueOf(lon));
	    f.setArguments(args);

	    return f;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		mUser = ((MainApplication)getActivity().getApplication()).getPlusClient();
		
		String name = getArguments().getString(LOCATION_NAME);
		
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.check_in)
				.setMessage(getString(R.string.check_in_text) + name + ".")
				.setCancelable(true)
				.setPositiveButton(getString(R.string.check_in),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								
								List<NameValuePair> nvps = new ArrayList<NameValuePair>();

								nvps.add(new BasicNameValuePair(PAR_USERNAME, mUser.getCurrentPerson().getName().getGivenName()));
								nvps.add(new BasicNameValuePair(PAR_USERLASTNAME, mUser.getCurrentPerson().getName().getFamilyName()));
								nvps.add(new BasicNameValuePair(PAR_USERID, mUser.getCurrentPerson().getId()));
								nvps.add(new BasicNameValuePair(PAR_USERIMAGEURL, mUser.getCurrentPerson().getImage().getUrl()));
								nvps.add(new BasicNameValuePair(PAR_LAT, getArguments().getString(LOCATION_LAT)));
								nvps.add(new BasicNameValuePair(PAR_LON, getArguments().getString(LOCATION_LON)));
								
								SendVolleyRequest.sendStringRequest(((MainApplication)getActivity().getApplication()), CALL_CHECKIN, nvps);

								dialog.dismiss();
							}
						})
				.setNegativeButton(getString(R.string.cancell),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
	}

}
