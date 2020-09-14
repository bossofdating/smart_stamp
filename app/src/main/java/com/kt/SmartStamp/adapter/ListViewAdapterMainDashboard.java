package com.kt.SmartStamp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.data.ServerDataContract;

import java.util.ArrayList;

public class ListViewAdapterMainDashboard extends BaseAdapter {
	/*************************************** 클래스 전역변수 영역 *************************************/
	private Context context;
	private ArrayList<ServerDataContract> contractArrayList;

	private LayoutInflater Inflater;

	/******************************************* 생성자 함수 *******************************************/
	public ListViewAdapterMainDashboard(Context context, ArrayList<ServerDataContract> contractArrayList) {
		this.context = context;
		this.contractArrayList = contractArrayList;

		Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/******************************************* getCount **********************************************/
	@Override
	public int getCount() {
		return contractArrayList.size();
	}

	/******************************************** getItem **********************************************/
	@Override
	public Object getItem(int position) {
		return contractArrayList.get(position);
	}

	/******************************************* getItemId *********************************************/
	@Override
	public long getItemId(int position) {
		return position;
	}

	/******************************************** getView **********************************************/
	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		view = Inflater.inflate( R.layout.sublayout_listview_main_dashiboard, viewGroup, false);

		// 레이아웃 연결
		TextView contNameTextView = view.findViewById(R.id.cont_name_textview);
		TextView contStateTextView = view.findViewById(R.id.cont_state_textview);
		TextView contDateTextview = view.findViewById(R.id.cont_date_textview);
		TextView contDetailTextView = view.findViewById(R.id.cont_detail_textview);

		// 레이아웃 출력
		ServerDataContract serverDataContract = contractArrayList.get(position);
		contNameTextView.setText(serverDataContract.cont_name);
		contDateTextview.setText("반출 기간 : " + serverDataContract.appr_st_dt + " ~ " + serverDataContract.appr_st_dt);
		contDetailTextView.setText(serverDataContract.cont_detail);

		int doc_after_cnt = Integer.parseInt(serverDataContract.doc_after_cnt);
		if (doc_after_cnt > 0) {
			contStateTextView.setText("등록중 (" + serverDataContract.doc_after_cnt + ")");
			contStateTextView.setTextColor(context.getResources().getColor(R.color.colorAccent));
		} else contStateTextView.setText("등록 대기");

		return view;
	}
}
