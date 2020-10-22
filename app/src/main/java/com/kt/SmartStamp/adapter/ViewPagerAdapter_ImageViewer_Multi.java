package com.kt.SmartStamp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.kt.SmartStamp.customview.TouchImageView;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;

import java.util.ArrayList;

public class ViewPagerAdapter_ImageViewer_Multi extends PagerAdapter implements HTTP_RESULT_LISTENER {
	/*************************************** 클래스 전역변수 영역 *************************************/
	private Context ExternalContext;
	private ArrayList<String> ArrayList_DataSrc;

	private LayoutInflater Inflater;
	private HTTP_ASYNC_REQUEST HttpAsyncRequest;

	/******************************************* 생성자 함수 ******************************************/
	public ViewPagerAdapter_ImageViewer_Multi(Context context, ArrayList<String> ArrayList_ImagePath ) {
		super();
		// 인스턴스 연결
		this.ExternalContext = context;
		this.ArrayList_DataSrc = ArrayList_ImagePath;
		// 인스턴스 생성
		Inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		HttpAsyncRequest = new HTTP_ASYNC_REQUEST( (Activity) context, null, this );
		HttpAsyncRequest.SetUseProgress( false );
	}
	/******************************************** getCount() ******************************************/
	@Override
	public int getCount() {
		return ArrayList_DataSrc.size();
	}
	/***************************************** instantiateItem() ****************************************/
	@NonNull
	public View instantiateItem( @NonNull ViewGroup Container, int Position ) {
		TouchImageView ImageView_FullImage = new TouchImageView( ExternalContext );
		ImageView_FullImage.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT ) );
		HttpAsyncRequest.RequestImage( ArrayList_DataSrc.get( Position ), 0, ImageView_FullImage );
		Container.addView( ImageView_FullImage );
		return ImageView_FullImage;
	}
	/****************************************** destroyItem() ******************************************/
	@Override
	public void destroyItem( @NonNull ViewGroup Container, int Position, @NonNull Object Object) {
		Container.removeView( (View) Object );
	}
	/*************************************** isViewFromObject() ***************************************/
	@Override
	public boolean isViewFromObject( @NonNull View Pager, @NonNull Object Object ) {
		return Pager == Object;
	}
	/***************************************** restoreState() ******************************************/
	@Override
	public void restoreState( Parcelable state, ClassLoader loader ) {
		super.restoreState( state, loader );
	}
	/*************************************** Http 요청결과 수신 ***************************************/
	@Override
	public void onReceiveHttpResult( boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData ) {
		if( Success ) {
			((TouchImageView) PassThroughData).setImageBitmap( ResultBitmap );
			((TouchImageView) PassThroughData).startAnimation( AnimationUtils.loadAnimation( ExternalContext, android.R.anim.fade_in ) );
		}
	}
	/***************************************************************************************************/
}
