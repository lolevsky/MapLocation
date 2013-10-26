package com.iapplize.maplocation.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ErrorDialogFragment extends DialogFragment{
	private Dialog mDialog;

	// Default constructor. Sets the dialog field to null
	public ErrorDialogFragment() {
		super();
		mDialog = null;
	}

	// Set the dialog to display
	public void setDialog(Dialog dialog) {
		mDialog = dialog;
	}

	// Return a Dialog to the DialogFragment.
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return mDialog;
	}
}
