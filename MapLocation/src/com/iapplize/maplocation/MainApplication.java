package com.iapplize.maplocation;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.plus.PlusClient;
import com.iapplize.maplocation.imageload.BitmapCache;

public class MainApplication extends Application {

	public RequestQueue mRequestQueue;
	public RequestQueue mImageRequestQueue;
	public ImageLoader mImageLoader;

	private PlusClient mPlusClient = null;

	@Override
	public void onCreate() {
		super.onCreate();

		mRequestQueue = Volley.newRequestQueue(this);
		mImageRequestQueue = Volley.newRequestQueue(this);
		// mImageLoader = new ImageLoader(mRequestQueue, new
		// DiskBitmapCache(getCacheDir()));

		mImageLoader = new ImageLoader(mImageRequestQueue, new BitmapCache(4));
	}

	public PlusClient getPlusClient() {
		return mPlusClient;
	}

	public void setPlusClient(PlusClient player) {
		this.mPlusClient = player;
	}

}
