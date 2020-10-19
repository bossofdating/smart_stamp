package com.kt.SmartStamp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.WindowManager;

import com.kt.SmartStamp.BuildConfig;
import com.kt.SmartStamp.R;
import com.kt.SmartStamp.utility.CommonUtil;

import java.io.File;

public class ActivityImageSelector extends Activity {
	/*************************************** 클래스 전역변수 영역 ************************************/
	public static final String INTENT_DATA_SELECTED_TYPE =									"SELECTED_TYPE";
	public static final String INTENT_DATA_SELECTED_IMAGE_PATH =								"SELECTED_IMAGE_PATH";

	public static final int SELECTED_TYPE_GALLERY =											1;
	public static final int SELECTED_TYPE_CAMERA =											2;

	public static final int IMAGE_TYPE_NORMAL =												1;
	public static final int IMAGE_TYPE_PROFILE =												2;

	private final int ACTIVITY_REQUEST_CODE_GALLERY =											1;
	private final int ACTIVITY_REQUEST_CODE_CAMERA =											2;

	private static final int IMAGE_QUALITY =													95;
	private static final int IMAGE_RESIZE_WIDTH_NORMAL =										1280;
	private static final int IMAGE_RESIZE_WIDTH_PROFILE =									500;

	private String TEMPORARY_IMAGE_PATH;

	private int SELECTED_TYPE;
	private String SELECTED_IMAGE_PATH;

	/******************************************* OnCreate *********************************************/
	@Override
	public void onCreate(Bundle onSavedInstanceState) {
		super.onCreate( onSavedInstanceState );
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );
		setContentView( R.layout.activity_image_selector );

		// 인텐트 데이터 수집
		Intent IntentInstance = getIntent();
		SELECTED_TYPE = IntentInstance.getIntExtra( INTENT_DATA_SELECTED_TYPE, SELECTED_TYPE_GALLERY );
		// 내부변수 초기화
		TEMPORARY_IMAGE_PATH = getCacheDir().getAbsolutePath() + File.separator + "TempImage.jpg";
		// 기본 레이아웃 출력
		DisplayLayout_Default();
	}
	/******************************************* OnDestroy ********************************************/
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	/******************************************** OnStart **********************************************/
	@Override
	protected void onStart() {
		super.onStart();
	}
	/************************************ OnRestoreInstanceState ************************************/
	@Override
	protected void onRestoreInstanceState( Bundle savedInstanceState ) {
		SELECTED_IMAGE_PATH = savedInstanceState.getString( INTENT_DATA_SELECTED_IMAGE_PATH );
		super.onRestoreInstanceState(savedInstanceState);
	}
	/************************************* OnSaveInstanceState **************************************/
	@Override
	protected void onSaveInstanceState( Bundle outState ) {
		outState.putString( INTENT_DATA_SELECTED_IMAGE_PATH, SELECTED_IMAGE_PATH );
		super.onSaveInstanceState(outState);
	}
	/*********************************** 액티비티 실행 결과 수신 *************************************/
	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		try {
			switch( requestCode ) {
				case ACTIVITY_REQUEST_CODE_CAMERA :
					if( resultCode == RESULT_OK ) {
						if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) SELECTED_IMAGE_PATH = TEMPORARY_IMAGE_PATH;
						else SELECTED_IMAGE_PATH = GetImagePathFromURI( data.getData() );
						Intent IntentInstance = new Intent();
						IntentInstance.putExtra( INTENT_DATA_SELECTED_IMAGE_PATH, SELECTED_IMAGE_PATH );
						setResult( RESULT_OK, IntentInstance );
					}
					break;
				case ACTIVITY_REQUEST_CODE_GALLERY :
					if( resultCode == RESULT_OK ) {
						SELECTED_IMAGE_PATH = GetImagePathFromURI( data.getData() );
						Intent IntentInstance = new Intent();
						IntentInstance.putExtra( INTENT_DATA_SELECTED_IMAGE_PATH, SELECTED_IMAGE_PATH );
						setResult( RESULT_OK, IntentInstance );
					}
					break;
			}
			finish();
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
		}
		super.onActivityResult( requestCode, resultCode, data );
	}
	/**************************************** 레이아웃 출력 *******************************************/
	private void DisplayLayout_Default() {
		switch( SELECTED_TYPE ) {
			case SELECTED_TYPE_GALLERY :
				GetImageFromGallery();
				break;
			case SELECTED_TYPE_CAMERA :
				GetImageFromCamera();
				break;
		}
	}
	/********************************** 사진 첨부 - 카메라에서 선택 **********************************/
	private void GetImageFromCamera() {
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
			File CameraImage = new File( TEMPORARY_IMAGE_PATH );
			Uri CameraImageURI = FileProvider.getUriForFile( this, BuildConfig.APPLICATION_ID + ".fileprovider", CameraImage );
			grantUriPermission( BuildConfig.APPLICATION_ID + ".fileprovider", CameraImageURI,  Intent.FLAG_GRANT_WRITE_URI_PERMISSION );

			Intent IntentInstance = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
			IntentInstance.putExtra( MediaStore.EXTRA_OUTPUT, CameraImageURI );
			startActivityForResult( IntentInstance, ACTIVITY_REQUEST_CODE_CAMERA );
		}
		else if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
			Intent IntentInstance = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
			File CameraImageFile = new File( TEMPORARY_IMAGE_PATH );
			IntentInstance.putExtra( MediaStore.EXTRA_OUTPUT, Uri.fromFile( CameraImageFile ) );
			startActivityForResult( IntentInstance, ACTIVITY_REQUEST_CODE_CAMERA );
		}
		else {
			Intent IntentInstance = new Intent();
			IntentInstance.setAction( MediaStore.ACTION_IMAGE_CAPTURE );
			startActivityForResult( IntentInstance, ACTIVITY_REQUEST_CODE_CAMERA );
		}
	}
	/*********************************** 사진 첨부 - 앨범에서 선택 ***********************************/
	private void GetImageFromGallery() {
		Intent IntentInstance = new Intent( Intent.ACTION_PICK ) ;
		IntentInstance.setType( MediaStore.Images.Media.CONTENT_TYPE );
		startActivityForResult( IntentInstance, ACTIVITY_REQUEST_CODE_GALLERY );
	}
	/******************************* 갤러리 URI 스토리지 경로 변환 함수 ******************************/
	private String GetImagePathFromURI( Uri GalleryURI ) {
		String StoreImagePath;
		String[] Projection = { MediaStore.Images.Media.DATA };
		CursorLoader CursorLoaderInstance = new CursorLoader( this, GalleryURI, Projection, null, null, null );
		Cursor ImageFileCursor = CursorLoaderInstance.loadInBackground();
		int ColumnIndex = ImageFileCursor.getColumnIndexOrThrow( MediaStore.Images.Media.DATA );
		ImageFileCursor.moveToFirst();
		StoreImagePath = ImageFileCursor.getString( ColumnIndex );
		ImageFileCursor.close();
		return StoreImagePath;
	}
	/************************************ 기본 가공된 비트맵 수집 ************************************/
	public static String GetDefaultProcessingBitmapImage( Context context, String ImageFilePath, int ImageType ) {
		Bitmap ResizedBitmap = null;
		String ProcessingFilePath;
		int ImageResizeWidth;

		BitmapFactory.Options BitmapInfo = new BitmapFactory.Options();

		switch( ImageType ) {
			default :
				ImageResizeWidth = IMAGE_RESIZE_WIDTH_NORMAL; break;
			case IMAGE_TYPE_PROFILE :
				ImageResizeWidth = IMAGE_RESIZE_WIDTH_PROFILE; break;
		}
		try {
			// 비트맵 정보 수집
			BitmapInfo.inJustDecodeBounds = true;
			BitmapFactory.decodeFile( ImageFilePath, BitmapInfo );
			// 이미지 파일 경로 설정
			String FileExtensionName = CommonUtil.GetFileExtension( ImageFilePath );
			ProcessingFilePath = context.getCacheDir().getAbsolutePath() + File.separator + "TempImage" + FileExtensionName;
			// 이미지 변환
			ResizedBitmap = CommonUtil.GetResizedBitmap( ImageFilePath, BitmapInfo.outWidth >= BitmapInfo.outHeight ? ImageResizeWidth : 0, BitmapInfo.outHeight >= BitmapInfo.outWidth ? ImageResizeWidth : 0 );
			if( FileExtensionName.toLowerCase().contains( ".jpg" ) || FileExtensionName.toLowerCase().contains( ".jpeg" ) ) {
				int RotateDegree = CommonUtil.GetRotationDegree( ImageFilePath );
				ResizedBitmap = CommonUtil.GetRotateBitmap( ResizedBitmap, RotateDegree, true );
			}
			CommonUtil.WriteBitmapToFile( ResizedBitmap, ProcessingFilePath, IMAGE_QUALITY, true );
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
			if( ResizedBitmap != null ) ResizedBitmap.recycle();
			ProcessingFilePath = "";
		}
		return ProcessingFilePath;
	}
	/************************************ 기본 가공된 비트맵 수집 ************************************/
	public static Bitmap GetDefaultProcessingBitmapImage( String ImageFilePath, int ResizeWidth ) {
		Bitmap ResizedBitmap = null;
		try {
			int RotateDegree = CommonUtil.GetRotationDegree( ImageFilePath );
			ResizedBitmap = CommonUtil.GetResizedBitmap( ImageFilePath, ResizeWidth, 0 );
			ResizedBitmap = CommonUtil.GetRotateBitmap( ResizedBitmap, RotateDegree, true );
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
			if( ResizedBitmap != null ) ResizedBitmap.recycle();
			ResizedBitmap = null;
		}
		return ResizedBitmap;
	}
	/***************************************************************************************************/
}
