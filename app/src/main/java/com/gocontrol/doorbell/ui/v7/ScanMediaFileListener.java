package com.gocontrol.doorbell.ui.v7;

import android.graphics.Bitmap;


public interface ScanMediaFileListener {
	public void onThumbnallGeted(int position, Bitmap thumbnall);
	public void onDurationGeted(int position, int ms);
	public void onFinished(int position);
}
