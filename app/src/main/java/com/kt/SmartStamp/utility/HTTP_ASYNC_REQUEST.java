package com.kt.SmartStamp.utility;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.kt.SmartStamp.BuildConfig;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;

import java.io.InputStream;

public class HTTP_ASYNC_REQUEST<T> {
	private Activity ExternalActivityInstance;
	private HTTP_UTIL HttpRequester;
	private HTTP_RESULT_LISTENER ResultEventListener;

	private Dialog CustomDialog;
	private BitmapDiskCache BitmapDiskCacheInstance;

	private String REST_API_AUTHORIZE_KEY;
	private boolean ACTIVATE_PROGRESS;

	public HTTP_ASYNC_REQUEST(Activity ActivityInstance, HTTP_RESULT_LISTENER EventListener ) {
		ExternalActivityInstance = ActivityInstance;
		ResultEventListener = EventListener;

		if( ActivityInstance != null ) {
			CustomDialog = CommonUtil.GetCustomProgressDialog( ExternalActivityInstance );
			BitmapDiskCacheInstance = new BitmapDiskCache( ActivityInstance );
			ACTIVATE_PROGRESS = false;
		}
	}

	public HTTP_ASYNC_REQUEST(Activity ActivityInstance, String RestApiAuthorizeKey, HTTP_RESULT_LISTENER EventListener ) {
		ExternalActivityInstance = ActivityInstance;
		REST_API_AUTHORIZE_KEY = RestApiAuthorizeKey;
		ResultEventListener = EventListener;

		if( ActivityInstance != null ) {
			CustomDialog = CommonUtil.GetCustomProgressDialog( ExternalActivityInstance );
			BitmapDiskCacheInstance = new BitmapDiskCache( ActivityInstance );
			ACTIVATE_PROGRESS = false;
		}
	}

	public void Destroy() {
		ResultEventListener = null;
	}

	public void SetUseProgress( boolean UseProgress ) {
		ACTIVATE_PROGRESS = UseProgress;
	}

	public void ShowProgress() {
		if( CustomDialog != null ) CustomDialog.show();
	}

	public void DismissProgress() {
		if( CustomDialog != null ) CustomDialog.dismiss();
	}

	public void AddHeaderData( String DataName, String DataValue ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		HttpRequester.AddHeaderData( DataName, DataValue );
	}

	public void AddHeaderData( String DataName, String DataValue, boolean NeedEncodeJson ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		HttpRequester.AddHeaderData( DataName, DataValue, NeedEncodeJson );
	}

	public void RequestHttpGetData( String HttpUrl, String Token, int RequestCode ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpGetPutData AsyncTaskInstance = new AsyncTask_HttpGetPutData( HttpRequester, HttpUrl, RequestCode, null );
		AsyncTaskInstance.execute( "GET", Token );
		HttpRequester = null;
	}

	public void RequestHttpGetData( String HttpUrl, String Token, int RequestCode, Object PassThroughData ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpGetPutData AsyncTaskInstance = new AsyncTask_HttpGetPutData( HttpRequester, HttpUrl, RequestCode, PassThroughData );
		AsyncTaskInstance.execute( "GET", Token );
		HttpRequester = null;
	}

	public void RequestHttpPutData( String HttpUrl, String Token, int RequestCode ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpGetPutData AsyncTaskInstance = new AsyncTask_HttpGetPutData( HttpRequester, HttpUrl, RequestCode, null );
		AsyncTaskInstance.execute( "PUT", Token );
		HttpRequester = null;
	}

	public void RequestHttpPutData( String HttpUrl, String Token, int RequestCode, Object PassThroughData ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpGetPutData AsyncTaskInstance = new AsyncTask_HttpGetPutData( HttpRequester, HttpUrl, RequestCode, PassThroughData );
		AsyncTaskInstance.execute( "PUT", Token );
		HttpRequester = null;
	}

	public void RequestHttpPostData( String HttpUrl, String Token, int RequestCode ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpPostDeletePatchData AsyncTaskInstance = new AsyncTask_HttpPostDeletePatchData( HttpRequester, HttpUrl, RequestCode, null );
		AsyncTaskInstance.execute( "POST", Token );
		HttpRequester = null;
	}

	public void RequestHttpPostData( String HttpUrl, String Token, int RequestCode, Object PassThroughData ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpPostDeletePatchData AsyncTaskInstance = new AsyncTask_HttpPostDeletePatchData( HttpRequester, HttpUrl, RequestCode, PassThroughData );
		AsyncTaskInstance.execute( "POST", Token );
		HttpRequester = null;
	}

	public void RequestHttpDeleteData( String HttpUrl, String Token, int RequestCode ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpPostDeletePatchData AsyncTaskInstance = new AsyncTask_HttpPostDeletePatchData( HttpRequester, HttpUrl, RequestCode, null );
		AsyncTaskInstance.execute( "DELETE", Token );
		HttpRequester = null;
	}

	public void RequestHttpDeleteData( String HttpUrl, String Token, int RequestCode, Object PassThroughData ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpPostDeletePatchData AsyncTaskInstance = new AsyncTask_HttpPostDeletePatchData( HttpRequester, HttpUrl, RequestCode, PassThroughData );
		AsyncTaskInstance.execute( "DELETE", Token );
		HttpRequester = null;
	}

	public void RequestHttpPatchData( String HttpUrl, String Token, int RequestCode ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpPostDeletePatchData AsyncTaskInstance = new AsyncTask_HttpPostDeletePatchData( HttpRequester, HttpUrl, RequestCode, null );
		AsyncTaskInstance.execute( "PATCH", Token );
		HttpRequester = null;
	}

	public void RequestHttpPatchData( String HttpUrl, String Token, int RequestCode, Object PassThroughData ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_HttpPostDeletePatchData AsyncTaskInstance = new AsyncTask_HttpPostDeletePatchData( HttpRequester, HttpUrl, RequestCode, PassThroughData );
		AsyncTaskInstance.execute( "PATCH", Token );
		HttpRequester = null;
	}

	public void RequestImage( String HttpUrl, int RequestCode ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_ReceiveHttpImage AsyncTaskInstance = new AsyncTask_ReceiveHttpImage( HttpRequester, HttpUrl, RequestCode, null );
		AsyncTaskInstance.execute();
		HttpRequester = null;
	}

	public void RequestImage( String HttpUrl, int RequestCode, Object PassThroughData ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_ReceiveHttpImage AsyncTaskInstance = new AsyncTask_ReceiveHttpImage( HttpRequester, HttpUrl, RequestCode, PassThroughData );
		AsyncTaskInstance.execute();
		HttpRequester = null;
	}

	public void RequestFile( String HttpUrl, String WriteFilePath, int RequestCode ) {
		if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
		AsyncTask_ReceiveHttpFile AsyncTaskInstance = new AsyncTask_ReceiveHttpFile( HttpRequester, HttpUrl, WriteFilePath, RequestCode, null );
		AsyncTaskInstance.execute();
		HttpRequester = null;
	}

	public void UploadPostImage( String HttpUrl, String Token, int RequestCode, String ImagePath, String ImageParameter, String ServerFileName ) {
		try {
			if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
			new AsyncTask_UploadHttpPostPatchImage( HttpRequester, HttpUrl, RequestCode, null, ImagePath, ImageParameter, ServerFileName ).execute( "POST", Token );
			HttpRequester = null;
		}
		catch( Exception e ) {}
	}

	public void UploadPatchImage( String HttpUrl, String Token, int RequestCode, String ImagePath, String ImageParameter, String ServerFileName ) {
		try {
			if( HttpRequester == null ) HttpRequester = new HTTP_UTIL( REST_API_AUTHORIZE_KEY );
			new AsyncTask_UploadHttpPostPatchImage( HttpRequester, HttpUrl, RequestCode, null, ImagePath, ImageParameter, ServerFileName ).execute( "PATCH", Token );
			HttpRequester = null;
		}
		catch( Exception e ) {}
	}

	private class AsyncTask_HttpGetPutData extends AsyncTask<String, Void, String> {
		HTTP_UTIL HttpRequester;
		String HTTP_URL;
		int HTTP_REQUEST_CODE;
		Object HTTP_PASS_THROUGH_DATA;

		AsyncTask_HttpGetPutData( HTTP_UTIL HttpUtil, String HttpURL, int HttpRequestCode, Object PassThroughData ) {
			HttpRequester = HttpUtil;
			HTTP_URL = HttpURL;
			HTTP_REQUEST_CODE = HttpRequestCode;
			HTTP_PASS_THROUGH_DATA = PassThroughData;
		}
		@Override
		protected void onPreExecute() {
			if( CustomDialog != null && ACTIVATE_PROGRESS ) CustomDialog.show();
		}
		@Override
		protected String doInBackground( String... Params ) {
			String HttpResultData;
			if( HTTP_URL == null ) return null;
			if( HTTP_URL.startsWith( "https" ) ) HttpResultData = HttpRequester.RequestHttpGetPutSSL( HTTP_URL, Params[0], Params[1] );
			else HttpResultData = HttpRequester.RequestHttpGetPut( HTTP_URL, Params[0], Params[1] );
			if( BuildConfig.DEBUG ) Log.i( "DEBUG", "[HTTP Request Result]\n- URL : " + HTTP_URL + "\n- Result Data : " + HttpResultData );
			return HttpResultData;
		}
		@Override
		protected void onPostExecute( String Result ) {
			if( CustomDialog != null && ACTIVATE_PROGRESS ) CustomDialog.dismiss();
			if( ResultEventListener != null ) ResultEventListener.onReceiveHttpResult( (Result != null), Result, null, HTTP_REQUEST_CODE, HttpRequester.RESPONSE_CODE, HTTP_PASS_THROUGH_DATA );
		}
	}

	private class AsyncTask_HttpPostDeletePatchData extends AsyncTask<String, Void, String> {
		HTTP_UTIL HttpRequester;
		String HTTP_URL;
		int HTTP_REQUEST_CODE;
		Object HTTP_PASS_THROUGH_DATA;

		AsyncTask_HttpPostDeletePatchData( HTTP_UTIL HttpUtil, String HttpURL, int HttpRequestCode, Object PassThroughData ) {
			HttpRequester = HttpUtil;
			HTTP_URL = HttpURL;
			HTTP_REQUEST_CODE = HttpRequestCode;
			HTTP_PASS_THROUGH_DATA = PassThroughData;
		}
		@Override
		protected void onPreExecute() {
			if( CustomDialog != null && ACTIVATE_PROGRESS ) CustomDialog.show();
		}
		@Override
		protected String doInBackground( String... Params ) {
			String HttpResultData;
			if( HTTP_URL == null ) return null;
			if( HTTP_URL.startsWith( "https" ) ) HttpResultData = HttpRequester.RequestHttpPostDeletePatchSSL( HTTP_URL, Params[0], Params[1] );
			else HttpResultData = HttpRequester.RequestHttpPostDeletePatch( HTTP_URL, Params[0], Params[1] );
			if( BuildConfig.DEBUG ) Log.i( "DEBUG", "[HTTP Request Result]\n- URL : " + HTTP_URL + "\n- Result Data : " + HttpResultData );
			return HttpResultData;
		}
		@Override
		protected void onPostExecute( String Result ) {
			if( CustomDialog != null && ACTIVATE_PROGRESS ) CustomDialog.dismiss();
			if( ResultEventListener != null ) ResultEventListener.onReceiveHttpResult( (Result != null), Result, null, HTTP_REQUEST_CODE, HttpRequester.RESPONSE_CODE, HTTP_PASS_THROUGH_DATA );
		}
	}

	private class AsyncTask_ReceiveHttpImage extends AsyncTask<Void, Void, Bitmap> {
		HTTP_UTIL HttpRequester;
		String HTTP_URL;
		int HTTP_REQUEST_CODE;
		Object HTTP_PASS_THROUGH_DATA;

		AsyncTask_ReceiveHttpImage( HTTP_UTIL HttpUtil, String HttpURL, int HttpRequestCode, Object PassThroughData ) {
			HttpRequester = HttpUtil;
			HTTP_URL = HttpURL;
			HTTP_REQUEST_CODE = HttpRequestCode;
			HTTP_PASS_THROUGH_DATA = PassThroughData;
		}
		@Override
		protected void onPreExecute() {
			if( CustomDialog != null && ACTIVATE_PROGRESS ) CustomDialog.show();
		}
		@Override
		protected Bitmap doInBackground( Void... Params ) {
			Bitmap HttpReceivedBitmap = null;
			if( BitmapDiskCacheInstance != null ) HttpReceivedBitmap = BitmapDiskCacheInstance.LoadBitmapImageCache( HTTP_URL, null );
			if( HttpReceivedBitmap != null ) {
				if( BuildConfig.DEBUG ) Log.i( "DEBUG", "[HTTP Request Result]\n- URL : " + HTTP_URL + "\n- Result Data : 캐시파일 수집 성공" );
				return HttpReceivedBitmap;
			}
			try {
				InputStream HttpResultData = HttpRequester.GetInputStream( HTTP_URL );
				BitmapFactory.Options BitmapOptions = new BitmapFactory.Options();
				BitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
				BitmapOptions.inDither = false;
				HttpReceivedBitmap = BitmapFactory.decodeStream( HttpResultData );
				if( BitmapDiskCacheInstance != null ) BitmapDiskCacheInstance.WriteBitmapImageCache( HTTP_URL, HttpReceivedBitmap, BitmapDiskCache.CACHE_IMAGE_TYPE_JPG, null );
				HttpResultData.close();
				if( BuildConfig.DEBUG ) Log.i( "DEBUG", "[HTTP Request Result]\n- URL : " + HTTP_URL + "\n- Result Data : 비트맵 수신 성공" );
			}
			catch( Exception e ) {
				HttpReceivedBitmap = null;
			}
			return HttpReceivedBitmap;
		}
		@Override
		protected void onPostExecute( Bitmap ReceivedBitmapResult ) {
			if( CustomDialog != null && ACTIVATE_PROGRESS ) CustomDialog.dismiss();
			if( ResultEventListener != null ) ResultEventListener.onReceiveHttpResult( (ReceivedBitmapResult != null), null, ReceivedBitmapResult, HTTP_REQUEST_CODE, HttpRequester.RESPONSE_CODE, HTTP_PASS_THROUGH_DATA );
		}
	}

	private class AsyncTask_ReceiveHttpFile extends AsyncTask<Void, Void, Boolean> {
		HTTP_UTIL HttpRequester;
		String HTTP_URL;
		String WRITE_FILE_PATH;
		int HTTP_REQUEST_CODE;
		Object HTTP_PASS_THROUGH_DATA;

		AsyncTask_ReceiveHttpFile( HTTP_UTIL HttpUtil, String HttpURL, String WriteFilePath, int HttpRequestCode, Object PassThroughData ) {
			HttpRequester = HttpUtil;
			HTTP_URL = HttpURL;
			WRITE_FILE_PATH = WriteFilePath;
			HTTP_REQUEST_CODE = HttpRequestCode;
			HTTP_PASS_THROUGH_DATA = PassThroughData;
		}
		@Override
		protected void onPreExecute() {
		}
		@Override
		protected Boolean doInBackground( Void... Params ) {
			Boolean DownloadResult = false;
			try {
				InputStream HttpResultData = HttpRequester.GetInputStream( HTTP_URL );
				DownloadResult = CommonUtil.WriteFileFromInputStream( WRITE_FILE_PATH, HttpResultData );
				HttpResultData.close();
				if( BuildConfig.DEBUG ) Log.i( "DEBUG", "[HTTP Request Result]\n- URL : " + HTTP_URL + (DownloadResult ? "\n- Result Data : 파일 수신 성공" : "\n- Result Data : 파일 수신 실패") );
			}
			catch( Exception e ) {
			}
			if( DownloadResult == false ) CommonUtil.DeleteFile( WRITE_FILE_PATH );
			return DownloadResult;
		}
		@Override
		protected void onPostExecute( Boolean Result ) {
			if( ResultEventListener != null ) ResultEventListener.onReceiveHttpResult( Result, null, null, HTTP_REQUEST_CODE, HttpRequester.RESPONSE_CODE, HTTP_PASS_THROUGH_DATA );
		}
	}

	private class AsyncTask_UploadHttpPostPatchImage extends AsyncTask<String, Void, String> {
		HTTP_UTIL HttpRequester;
		String HTTP_URL;
		int HTTP_REQUEST_CODE;
		Object HTTP_PASS_THROUGH_DATA;

		String HTTP_UPLOAD_FILE_PATH;
		String HTTP_UPLOAD_FILE_PARAMETER;
		String HTTP_UPLOAD_FILE_SERVER_FILENAME;

		AsyncTask_UploadHttpPostPatchImage( HTTP_UTIL HttpUtil, String HttpURL, int HttpRequestCode, Object PassThroughData, String UploadFilePath, String UploadFileParameter, String UploadFileServerFileName ) {
			HttpRequester = HttpUtil;
			HTTP_URL = HttpURL;
			HTTP_REQUEST_CODE = HttpRequestCode;
			HTTP_PASS_THROUGH_DATA = PassThroughData;
			HTTP_UPLOAD_FILE_PATH = UploadFilePath;
			HTTP_UPLOAD_FILE_PARAMETER = UploadFileParameter;
			HTTP_UPLOAD_FILE_SERVER_FILENAME = UploadFileServerFileName;
		}
		@Override
		protected void onPreExecute() {
			if( CustomDialog != null && ACTIVATE_PROGRESS ) CustomDialog.show();
		}
		@Override
		protected String doInBackground( String... Params ) {
			InputStream InputStream_UploadImage = CommonUtil.GetInputStreamFromFile( HTTP_UPLOAD_FILE_PATH );
			String HttpResultData = HttpRequester.RequestDataUpload( HTTP_URL, Params[0], Params[1], InputStream_UploadImage, HTTP_UPLOAD_FILE_PARAMETER, HTTP_UPLOAD_FILE_SERVER_FILENAME );
			if( BuildConfig.DEBUG ) Log.i( "DEBUG", "[HTTP Request Result]\n- URL : " + HTTP_URL + "\n- Result Data : " + HttpResultData );
			return HttpResultData;
		}
		@Override
		protected void onPostExecute( String Result ) {
			if( CustomDialog != null && ACTIVATE_PROGRESS ) CustomDialog.dismiss();
			if( ResultEventListener != null ) ResultEventListener.onReceiveHttpResult( (Result != null), Result, null, HTTP_REQUEST_CODE, HttpRequester.RESPONSE_CODE, HTTP_PASS_THROUGH_DATA );
		}
	}
}

