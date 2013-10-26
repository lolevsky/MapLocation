package com.iapplize.maplocation.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.iapplize.maplocation.MainApplication;
import com.iapplize.maplocation.R;
import com.iapplize.maplocation.dialog.ErrorDialogFragment;

public class SignUpActivity extends ActionBarActivity implements
		OnClickListener, ConnectionCallbacks, OnConnectionFailedListener  {

	MainApplication mApp;

	// Google
	SignInButton signInButton;

	PlusClient mPlusClient;

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private static final int OUR_REQUEST_CODE = 49404;
	private boolean mResolveOnFail;

	private ConnectionResult mConnectionResult;

	private ProgressDialog mConnectionProgressDialog;

	// Google

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_signup);

		if (!servicesConnected()) {
			Toast.makeText(this, R.string.google_play_services_not_found,
					Toast.LENGTH_SHORT).show();
			finish();
		}

		mApp = ((MainApplication) getApplication());

		findViewById(R.id.sign_in_button).setOnClickListener(this);

		// google
		mPlusClient = new PlusClient.Builder(this, this, this).build();
		mResolveOnFail = false;

		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");
		// end google

	}
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {

		if (requestCode == OUR_REQUEST_CODE && responseCode == RESULT_OK) {
			// If we have a successful result, we will want to be able to
			// resolve any further errors, so turn on resolution with our
			// flag.
			mResolveOnFail = true;
			// If we have a successful result, lets call connect() again. If
			// there are any more errors to resolve we'll get our
			// onConnectionFailed, but if not, we'll get onConnected.
			mPlusClient.connect();
		} else if (requestCode == OUR_REQUEST_CODE && responseCode != RESULT_OK) {
			// If we've got an error we can't resolve, we're no
			// longer in the midst of signing in, so we can stop
			// the progress spinner.
			mConnectionProgressDialog.dismiss();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sign_in_button:
			
			if (!mPlusClient.isConnected()) {
				// Show the dialog as we are now signing in.
				mConnectionProgressDialog.show();
				// Make sure that we will start the resolution (e.g. fire the
				// intent and pop up a dialog for the user) for any errors
				// that come in.
				mResolveOnFail = true;
				// We should always have a connection result ready to resolve,
				// so we can start that process.
				if (mConnectionResult != null) {
					startResolution();
				} else {
					// If we don't have one though, we can start connect in
					// order to retrieve one.
					mPlusClient.connect();
				}
			}
			break;
		}
	}

//@Override
//public void onSignInFailed() {
//	new AlertDialog.Builder(SignUpActivity.this)
//	.setTitle(R.string.cancelled)
//	.setMessage(R.string.error_on_signin)
//	.setPositiveButton(R.string.ok, null).show();
//}
//
//@Override
//public void onSignInSucceeded() {
//	Player p = getGamesClient().getCurrentPlayer();
//	
//	mApp.setmGamesClient(getGamesClient());
//	mApp.setPlayer(p);
//	
//	startActivity(new Intent(SignUpActivity.this, MainActivity.class));
//	finish();
//}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			mConnectionResult = result;
			if (mResolveOnFail) {
				// This is a local helper function that starts
				// the resolution of the problem, which may be
				// showing the user an account chooser or similar.
				startResolution();
			}
		} else {
			mConnectionProgressDialog.dismiss();

			new AlertDialog.Builder(SignUpActivity.this)
					.setTitle(R.string.cancelled)
					.setMessage(R.string.error_on_signin)
					.setPositiveButton(R.string.ok, null).show();
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		mResolveOnFail = false;

		// Hide the progress dialog if its showing.
		mConnectionProgressDialog.dismiss();

		if (mPlusClient != null) {

			mApp.setPlusClient(mPlusClient);
			
			startActivity(new Intent(SignUpActivity.this, MainActivity.class));
			finish();

		} else {

			new AlertDialog.Builder(SignUpActivity.this)
					.setTitle(R.string.cancelled)
					.setMessage(R.string.error_on_signin)
					.setPositiveButton(R.string.ok, null).show();

		}
	}

	@Override
	public void onDisconnected() {
		mConnectionProgressDialog.dismiss();
	}

	private void startResolution() {
		try {
			// Don't start another resolution now until we have a
			// result from the activity we're about to start.
			mResolveOnFail = false;
			// If we can resolve the error, then call start resolution
			// and pass it an integer tag we can use to track. This means
			// that when we get the onActivityResult callback we'll know
			// its from being started here.
			mConnectionResult.startResolutionForResult(this, OUR_REQUEST_CODE);
		} catch (SendIntentException e) {
			// Any problems, just try to connect() again so we get a new
			// ConnectionResult.
			mPlusClient.connect();
		}
	}

}
