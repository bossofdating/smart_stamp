package com.kt.SmartStamp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kt.SmartStamp.R;
import com.kt.SmartStamp.activity.DetailHistoryActivity;
import com.kt.SmartStamp.data.ServerDataContract;
import com.kt.SmartStamp.data.ServerDataStamp;
import com.kt.SmartStamp.listener.LIST_ITEM_LISTENER;

import java.util.ArrayList;

public class RecyclerViewAdapterStampLocation extends RecyclerView.Adapter<RecyclerViewAdapterStampLocation.ViewHolder> {
	private static final long MIN_CLICK_INTERVAL = 1000;
	private long mLastClickTime;

	/******************************************* ViewHolder *******************************************/
	class ViewHolder extends RecyclerView.ViewHolder {
		public LinearLayout item_linearlayout;
		public TextView status_textview;
		public TextView reg_dt_textview;
		public TextView addr_textview;

		ViewHolder(View view) {
			super(view);
			// 레이아웃 연결
			item_linearlayout = view.findViewById(R.id.item_linearlayout);
			status_textview = view.findViewById( R.id.status_textview );
			reg_dt_textview = view.findViewById( R.id.reg_dt_textview );
			addr_textview = view.findViewById( R.id.addr_textview );
		}
	}

	/*************************************** 클래스 전역변수 영역 *************************************/
	private ArrayList<ServerDataStamp> arrayListStampLocation;
	private LayoutInflater layoutInflater;
	private Context context;

	/******************************************** 생성자 **********************************************/
	public RecyclerViewAdapterStampLocation(Context context, ArrayList<ServerDataStamp> arrayListStampLocation, LIST_ITEM_LISTENER EventListener) {
		// 외부변수 연결
		this.context = context;
		this.arrayListStampLocation = arrayListStampLocation;

		// 인스턴스 생성
		layoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
	}

	/*********************************** OnAttachedToRecyclerView **********************************/
	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

	/********************************* OnDetachedFromRecyclerView ********************************/
	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		super.onDetachedFromRecyclerView(recyclerView);
	}

	/************************************** OnCreateViewHolder **************************************/
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sublayout_listview_stamp_history, parent, false);
		return new ViewHolder(convertView);
	}
	/*************************************** OnBindViewHolder ***************************************/
	@Override
	public void onBindViewHolder(@NonNull ViewHolder Holder, int position ) {
		// 레이아웃 출력
		ServerDataStamp serverDataStamp = arrayListStampLocation.get(position);
		if ("open".equals(serverDataStamp.status)) {
			Holder.status_textview.setText("열기");
			Holder.status_textview.setTextColor(context.getResources().getColor(R.color.colorBlue));
		} else if ("close".equals(serverDataStamp.status)) {
			Holder.status_textview.setText("닫기");
			Holder.status_textview.setTextColor(context.getResources().getColor(R.color.colorAccent));
		}
		Holder.reg_dt_textview.setText("일시 : " + serverDataStamp.reg_dt);
		Holder.addr_textview.setText("주소 : " + serverDataStamp.addr);
	}
	/**************************************** OnViewRecycled ****************************************/
	@Override
	public void onViewRecycled(ViewHolder Holder) {
		super.onViewRecycled(Holder);
	}
	/******************************************** GetCount ********************************************/
	@Override
	public int getItemCount() {
		return arrayListStampLocation.size();
	}
}
