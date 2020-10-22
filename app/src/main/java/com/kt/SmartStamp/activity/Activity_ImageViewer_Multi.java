package com.kt.SmartStamp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.adapter.ViewPagerAdapter_ImageViewer_Multi;

import java.util.ArrayList;

public class Activity_ImageViewer_Multi extends AppCompatActivity {
	/************************************** 클래스 전역변수 영역 *************************************/
	public static final String INTENT_DATA_IMAGE_PATH_LIST = 										"IMAGE_PATH_LIST";
	public static final String INTENT_DATA_ITEM_POSITION = 											"ITEM_POSITION";

	private RelativeLayout RelativeLayout_RootLayer;
	private ViewPager ViewPager_FullImage;

	private ArrayList<String> IMAGE_PATH_LIST;
	private int ITEM_POSITION;

	private ViewPagerAdapter_ImageViewer_Multi VPA_MultiImageView;

	/******************************************* OnCreate *********************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE );
		setContentView( R.layout.activity_imageviewer_multi );

		// 인텐트 데이터 수집
		Intent IntentInstance = getIntent();
		IMAGE_PATH_LIST = IntentInstance.getStringArrayListExtra( INTENT_DATA_IMAGE_PATH_LIST );
		ITEM_POSITION = IntentInstance.getIntExtra( INTENT_DATA_ITEM_POSITION, 0 );
		// 레이아웃 연결
		RelativeLayout_RootLayer = findViewById( R.id.RelativeLayout_RootLayer );
		ViewPager_FullImage = findViewById( R.id.ViewPager_FullImage );
		// 기본 레이아웃 출력
		DisplayLayout_Default();
	}
	/******************************************* OnDestroy ********************************************/
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	/******************************************* OnResume *******************************************/
	@Override
	protected void onResume() {
		overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
		super.onResume();
	}
	/******************************************* OnPause **********************************************/
	@Override
	protected void onPause() {
		overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
		super.onPause();
	}
	/************************************ OnOptionsItemSelected *************************************/
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch( item.getItemId() ) {
			case android.R.id.home :
				finish();
				break;
		}
		return super.onOptionsItemSelected( item );
	}
	/**************************************** 레이아웃 출력 *******************************************/
	private void DisplayLayout_Default() {
		// 뷰페이저 설정
		VPA_MultiImageView = new ViewPagerAdapter_ImageViewer_Multi( this, IMAGE_PATH_LIST );
		ViewPager_FullImage.setAdapter( VPA_MultiImageView );
		ViewPager_FullImage.setCurrentItem( ITEM_POSITION );
	}
}
