package com.kt.SmartStamp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.activity.MainActivity;
import com.kt.SmartStamp.adapter.RecyclerViewAdapterMainCompleteList;
import com.kt.SmartStamp.data.ServerDataContract;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.listener.LIST_ITEM_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;

import java.util.ArrayList;

public class FragmentMainCompleteList extends Fragment implements HTTP_RESULT_LISTENER, LIST_ITEM_LISTENER, View.OnClickListener {
	public static HTTP_ASYNC_REQUEST httpAsyncRequest;
	public static SessionManager sessionManager;
	private JSONService jsonService;
	private ArrayList<ServerDataContract> contractArrayList;
	public static RecyclerViewAdapterMainCompleteList recyclerViewAdapterMainCompleteList;

	public static final int ANIMATION_DELAY_TIME = 100;
	private static final long MIN_CLICK_INTERVAL = 1000;
	private static final long MIN_BTN_CLICK_INTERVAL = 3000;
	private long mLastClickTime;
	private int offset = 0;

	private NestedScrollView completeListNestedscrollview;
	private RecyclerView recyclerViewMainCompleteList;
	private LinearLayout completeListLinearLayout;
	private LinearLayout contNCntLinearLayout;
	private LinearLayout contRCntLinearLayout;
	private TextView contCntTextView;
	private TextView contNCntTextView;
	private TextView contRCntTextView;
	private TextView contYCntTextView;
	private TextView nodataTextview;
	private RelativeLayout contractListRelativelayout;

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
		View FragmentView = inflater.inflate(R.layout.fragment_main_complete_list, container, false);
		httpAsyncRequest = new HTTP_ASYNC_REQUEST(getActivity(), COMMON_DEFINE.REST_API_AUTHORIZE_KEY_NAME, this);
		sessionManager = new SessionManager(getActivity());
		jsonService = new JSONService();
		contractArrayList = new ArrayList<>();

		final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) FragmentView.findViewById(R.id.swipe_layout);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				offset = 0;
				contractArrayList = new ArrayList<>();
				requestHttpDataContCnt();
				swipeRefreshLayout.setRefreshing(false);
			}
		});

		completeListNestedscrollview = FragmentView.findViewById(R.id.complete_list_nestedscrollview);
		recyclerViewMainCompleteList = FragmentView.findViewById(R.id.contract_list_recyclerview);
		completeListLinearLayout = FragmentView.findViewById(R.id.complete_list_linearlayout);
		contNCntLinearLayout = FragmentView.findViewById(R.id.cont_n_cnt_linearlayout);
		contRCntLinearLayout = FragmentView.findViewById(R.id.cont_r_cnt_linearlayout);
		contCntTextView = FragmentView.findViewById(R.id.cont_cnt_textview);
		contNCntTextView = FragmentView.findViewById(R.id.cont_n_cnt_textview);
		contRCntTextView = FragmentView.findViewById(R.id.cont_r_cnt_textview);
		contYCntTextView = FragmentView.findViewById(R.id.cont_y_cnt_textview);
		nodataTextview = FragmentView.findViewById(R.id.nodata_textview);
		contractListRelativelayout = FragmentView.findViewById(R.id.contract_list_relativelayout);

		contNCntLinearLayout.setOnClickListener(this);
		contRCntLinearLayout.setOnClickListener(this);

		completeListNestedscrollview.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
			@Override
			public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
					offset++;
					requestHttpDataContNList(offset);
				}
			}
		});

		requestHttpDataContCnt();

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

	@Override
	public void onItemClick(Object CallerObject, int clickType, int position) {	}

	@Override
	public void onReachedLastItem(Object callerObject) {}

	/************************************ 액티비티 실행 결과 수신 *************************************/
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) { }
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**************************************** 레이아웃 출력 *******************************************/
	private void displayLayoutDefault() {
		completeListLinearLayout.setVisibility(View.VISIBLE);
	}

	/************************************** 클릭 이벤트 핸들러 ****************************************/
	@Override
	public void onClick(View view) {
		// 중복 클릭 방지
		long currentClickTime= SystemClock.uptimeMillis();
		long elapsedTime=currentClickTime-mLastClickTime;
		if(elapsedTime<=MIN_BTN_CLICK_INTERVAL){
			return;
		}
		mLastClickTime=currentClickTime;

		switch(view.getId()) {
			case R.id.cont_n_cnt_linearlayout :
				((MainActivity) getActivity()).navView.setSelectedItemId(R.id.navigation_dashboard);
				break;
			case R.id.cont_r_cnt_linearlayout :
				((MainActivity) getActivity()).navView.setSelectedItemId(R.id.navigation_list);
				break;
		}
	}

	/************************************* HTTP  데이터요청 ******************************************/
	// 계약 상태별 카운트 - 1
	public static void requestHttpDataContCnt() {
		httpAsyncRequest.RequestHttpGetData(String.format(HTTP_DEFINE.HTTP_URL_CONT_CNT, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 1);
	}
	// 날인 완료 계약 리스트 - 2
	public static void requestHttpDataContNList(int offset) {
		httpAsyncRequest.AddHeaderData("type", "y");
		httpAsyncRequest.AddHeaderData("offset", Integer.toString(offset));
		httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_CONT_LIST, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 2);
	}

	/*************************************** Http 요청결과 수신 ***************************************/
	@Override
	public void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData) {
		if(Success) {
			if(RequestCode == 1) parseJsonContCnt(ResultData);		// 계약 상태별 카운트 - 1
			if(RequestCode == 2) parseJsonContNList(ResultData);	// 날인 완료 계약 리스트 - 2
		} else Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
	}
	// 계약 상태별 카운트 - 1
	private void parseJsonContCnt(String jsonData) {
		jsonService.CreateJSONObject(jsonData);

		if(jsonService != null) {
			String contNCnt = jsonService.GetString("cont_n_cnt", null);
			String contRCnt = jsonService.GetString("cont_r_cnt", null);
			String contYCnt = jsonService.GetString("cont_y_cnt", null);

			if (Integer.parseInt(contYCnt) > 0) {
				nodataTextview.setVisibility(View.GONE);
				contractListRelativelayout.setVisibility(View.VISIBLE);
			} else {
				contractListRelativelayout.setVisibility(View.GONE);
				nodataTextview.setVisibility(View.VISIBLE);
			}

			contCntTextView.setText(contYCnt + "건");
			contNCntTextView.setText(contNCnt + "건");
			contRCntTextView.setText(contRCnt + "건");
			contYCntTextView.setText(contYCnt + "건");
		}

		requestHttpDataContNList(offset);
	}
	// 날인 완료 계약 리스트 - 2
	private void parseJsonContNList(String jsonData) {
		jsonService.CreateJSONArray(jsonData);

		if(jsonService != null && jsonService.GetArrayLength() > 0) {
			while(jsonService.SetNextNode()) {
				ServerDataContract serverDataContract = jsonService.GetClass(ServerDataContract.class);
				contractArrayList.add(serverDataContract);
			}

			recyclerViewAdapterMainCompleteList = new RecyclerViewAdapterMainCompleteList(getActivity(), contractArrayList, this);
			recyclerViewAdapterMainCompleteList.notifyDataSetChanged();
			recyclerViewMainCompleteList.setLayoutManager(new LinearLayoutManager(getActivity()));
			recyclerViewMainCompleteList.setAdapter(recyclerViewAdapterMainCompleteList);
		} else {
			offset--;
		}

		// 기본 레이아웃 출력
		displayLayoutDefault();
	}

}
