package com.kt.SmartStamp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;

public class FragmentMainList extends Fragment implements  HTTP_RESULT_LISTENER, AdapterView.OnItemClickListener {
	public static SessionManager sessionManager;
	public static HTTP_ASYNC_REQUEST httpAsyncRequest;

	public static final int ANIMATION_DELAY_TIME = 100;
	private static final long MIN_CLICK_INTERVAL = 500;
	private long mLastClickTime;

	private TextView testTextView;

	@Override
	public void onAttach(Context context) {
		super.onAttach( context );
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View FragmentView = inflater.inflate(R.layout.fragment_main_list, container, false);
		sessionManager = new SessionManager(getActivity());
		httpAsyncRequest = new HTTP_ASYNC_REQUEST(getActivity(), COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);

		testTextView = FragmentView.findViewById(R.id.testTextView);

		// 기본 레이아웃 출력
		displayLayoutDefault();

		return FragmentView;
	}

	@Override
	public void onStart() { super.onStart();	}

	@Override
	public void onDestroyView() {
		httpAsyncRequest.Destroy();
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) { super.onConfigurationChanged(newConfig); }

	/************************************ 액티비티 실행 결과 수신 *************************************/
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch( requestCode ) { }
		super.onActivityResult(requestCode, resultCode, data);
	}

	/************************************* ListView 이벤트 핸들러 *************************************/
	@Override
	public void onItemClick(AdapterView<?> Parent, View view, final int Position, long id) {
		// 중복 클릭인 경우
		long currentClickTime= SystemClock.uptimeMillis();
		long elapsedTime=currentClickTime-mLastClickTime;
		if(elapsedTime<=MIN_CLICK_INTERVAL){
			return;
		}
		mLastClickTime=currentClickTime;
		new Handler().postDelayed(new Runnable() {
			public void run() {
			}
		}, ANIMATION_DELAY_TIME < 0 ? 0 : ANIMATION_DELAY_TIME);
	}

	/************************************* HTTP  데이터요청 ******************************************/
	// test
	public static void requestHttpDataTest() {
		httpAsyncRequest.RequestHttpGetData(String.format(HTTP_DEFINE.HTTP_URL_CONT_LIST, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 1);
	}

	/*************************************** Http 요청결과 수신 ***************************************/
	@Override
	public void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData) {
		if(Success) {
			if(RequestCode == 1) parseJsonTest(ResultData);		// 테스트
		}
		else Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
	}

	/**************************************** JsonData 변환 *******************************************/
	// test
	private void parseJsonTest(String JsonData) {
	}

	/**************************************** 레이아웃 출력 *******************************************/
	private void displayLayoutDefault() {
	}

	/*************************************** 다이얼로그 출력 ******************************************/
	private void displayDialogTest(final int ItemPosition) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);
		builder.setTitle("title");
		builder.setMessage("");
		builder.setNegativeButton("취소", null );
		builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
			@Override
			public void onClick( DialogInterface dialog, int which ) {
				requestHttpDataTest();
			}
		} );
		builder.show();
	}

}
