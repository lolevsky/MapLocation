package com.iapplize.maplocation.imageload;

import java.io.File;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader.ImageCache;

public class DiskBitmapCache extends DiskBasedCache implements ImageCache {
 
    public DiskBitmapCache(File rootDirectory, int maxCacheSizeInBytes) {
        super(rootDirectory, maxCacheSizeInBytes);
    }
 
    public DiskBitmapCache(File cacheDir) {
        super(cacheDir);
    }
 
    public Bitmap getBitmap(String url) {
        final Entry requestedItem = get(url);
 
        if (requestedItem == null)
            return null;
 
        return BitmapFactory.decodeByteArray(requestedItem.data, 0, requestedItem.data.length);
    }
 
    public void putBitmap(String url, Bitmap bitmap) {
        final Entry entry = new Entry();
 
        ByteBuffer buffer = ByteBuffer.allocate(getSizeInBytes(bitmap));
        bitmap.copyPixelsToBuffer(buffer);
        entry.data = buffer.array();
 
        put(url, entry);
    }
    
    @SuppressLint("NewApi")
	public static int getSizeInBytes(Bitmap bitmap) {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }
}