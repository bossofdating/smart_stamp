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
import android.widget.TextView;
import android.widget.Toast;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.adapter.RecyclerViewAdapterMainDashboard;
import com.kt.SmartStamp.data.ServerDataContract;
import com.kt.SmartStamp.define.COMMON_DEFINE;
import com.kt.SmartStamp.define.HTTP_DEFINE;
import com.kt.SmartStamp.listener.HTTP_RESULT_LISTENER;
import com.kt.SmartStamp.listener.LIST_ITEM_LISTENER;
import com.kt.SmartStamp.service.SessionManager;
import com.kt.SmartStamp.utility.HTTP_ASYNC_REQUEST;
import com.kt.SmartStamp.utility.JSONService;

import java.util.ArrayList;

public class FragmentMainDashboard extends Fragment implements  HTTP_RESULT_LISTENER, LIST_ITEM_LISTENER {
	public static HTTP_ASYNC_REQUEST httpAsyncRequest;
	public static SessionManager sessionManager;
	private JSONService jsonService;
	private ArrayList<ServerDataContract> contractArrayList;
	public static RecyclerViewAdapterMainDashboard recyclerViewAdapterMainDashboard;

	public static final int ANIMATION_DELAY_TIME = 100;
	private static final long MIN_CLICK_INTERVAL = 500;
	private long mLastClickTime;
	private int offset = 0;

	private NestedScrollView dashboardNestedscrollview;
	private RecyclerView recyclerViewMainDashboard;
	private LinearLayout dashboardLinearLayout;
	private TextView utnameTextView;
	private TextView nameTextView;
	private TextView contCntTextView;
	private TextView contNCntTextView;
	private TextView contRCntTextView;
	private TextView contYCntTextView;

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
		View FragmentView = inflater.inflate(R.layout.fragment_main_dashboard, container, false);
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

		dashboardNestedscrollview = FragmentView.findViewById(R.id.dashboard_nestedscrollview);
		recyclerViewMainDashboard = FragmentView.findViewById(R.id.contract_list_recyclerview);
		dashboardLinearLayout = FragmentView.findViewById(R.id.dashboard_linearlayout);
		utnameTextView = FragmentView.findViewById(R.id.utname_textview);
		nameTextView = FragmentView.findViewById(R.id.name_textview);
		contCntTextView = FragmentView.findViewById(R.id.cont_cnt_textview);
		contNCntTextView = FragmentView.findViewById(R.id.cont_n_cnt_textview);
		contRCntTextView = FragmentView.findViewById(R.id.cont_r_cnt_textview);
		contYCntTextView = FragmentView.findViewById(R.id.cont_y_cnt_textview);

		dashboardNestedscrollview.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
			@Override
			public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
					offset++;
					requestHttpDataContCnt();
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

	/********************************** 리스트 아이템 이벤트 핸들러 **********************************/
	@Override
	public void onItemClick(Object CallerObject, int clickType, int position) {
		// 중복 클릭인 경우
		long currentClickTime= SystemClock.uptimeMillis();
		long elapsedTime=currentClickTime-mLastClickTime;
		if(elapsedTime<=MIN_CLICK_INTERVAL){
			return;
		}
		mLastClickTime=currentClickTime;

		switch(clickType) {
			case COMMON_DEFINE.LIST_ITEM_CLICK_TYPE_NORMAL :
				break;
			case COMMON_DEFINE.LIST_ITEM_CLICK_TYPE_DELETE :
				break;
		}
	}

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
		utnameTextView.setText(sessionManager.getUtName());
		nameTextView.setText(sessionManager.getMemName());

		dashboardLinearLayout.setVisibility(View.VISIBLE);
	}

	/************************************* HTTP  데이터요청 ******************************************/
	// 계약 상태별 카운트 - 1
	public static void requestHttpDataContCnt() {
		httpAsyncRequest.RequestHttpGetData(String.format(HTTP_DEFINE.HTTP_URL_CONT_CNT, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 1);
	}
	// 등록 대기 문서 리스트 - 2
	public static void requestHttpDataContNList(int offset) {
		httpAsyncRequest.AddHeaderData("type", "n");
		httpAsyncRequest.AddHeaderData("offset", Integer.toString(offset));
		httpAsyncRequest.RequestHttpPostData(String.format(HTTP_DEFINE.HTTP_URL_CONT_LIST, sessionManager.getMemIdx()), sessionManager.getAuthKey(), 2);
	}

	/*************************************** Http 요청결과 수신 ***************************************/
	@Override
	public void onReceiveHttpResult(boolean Success, String ResultData, Bitmap ResultBitmap, int RequestCode, int HttpResponseCode, Object PassThroughData) {
		if(Success) {
			if(RequestCode == 1) parseJsonContCnt(ResultData);		// 계약 상태별 카운트 - 1
			if(RequestCode == 2) parseJsonContNList(ResultData);	// 등록 대기 문서 리스트 - 2
		} else Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
	}

	/**************************************** JsonData 변환 *******************************************/
	// 계약 상태별 카운트 - 1
	private void parseJsonContCnt(String jsonData) {
		jsonService.CreateJSONObject(jsonData);

		if(jsonService != null) {
			String contNCnt = jsonService.GetString( "cont_n_cnt", null );
			String contRCnt = jsonService.GetString( "cont_r_cnt", null );
			String contYCcnt = jsonService.GetString( "cont_y_cnt", null );

			contCntTextView.setText(contNCnt + "건");
			contNCntTextView.setText(contNCnt + "건");
			contRCntTextView.setText(contRCnt + "건");
			contYCntTextView.setText(contYCcnt + "건");
		}

		requestHttpDataContNList(offset);
	}
	// 등록 대기 문서 리스트 - 2
	private void parseJsonContNList(String jsonData) {
		jsonService.CreateJSONArray(jsonData);

		if(jsonService != null && jsonService.GetArrayLength() > 0) {
			while(jsonService.SetNextNode()) {
				ServerDataContract serverDataContract = jsonService.GetClass(ServerDataContract.class);
				contractArrayList.add(serverDataContract);
			}

			recyclerViewAdapterMainDashboard = new RecyclerViewAdapterMainDashboard(getActivity(), contractArrayList, this);
			recyclerViewAdapterMainDashboard.notifyDataSetChanged();
			recyclerViewMainDashboard.setLayoutManager(new LinearLayoutManager(getActivity()));
			recyclerViewMainDashboard.setAdapter(recyclerViewAdapterMainDashboard);
		} else {
			offset--;
		}

		// 기본 레이아웃 출력
		displayLayoutDefault();
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
			}
		} );
		builder.show();
	}

}
