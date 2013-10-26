package com.iapplize.maplocation.imageload;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

@SuppressWarnings("rawtypes")
public class BitmapCache  extends LruCache implements ImageCache {
    public BitmapCache(int maxSize) {
        super(maxSize);
    }
 
    @SuppressWarnings("unchecked")
	@Override
    public Bitmap getBitmap(String url) {
        return (Bitmap) get(url);
    }
 
    @SuppressWarnings("unchecked")
	@Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}