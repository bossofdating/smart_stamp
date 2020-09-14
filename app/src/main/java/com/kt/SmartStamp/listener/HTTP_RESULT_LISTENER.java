package com.kt.SmartStamp.listener;

import android.graphics.Bitmap;

public interface HTTP_RESULT_LISTENER<T> {
	void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData);
}
