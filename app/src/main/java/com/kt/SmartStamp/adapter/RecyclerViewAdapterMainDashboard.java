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
import com.kt.SmartStamp.activity.DetailReadyActivity;
import com.kt.SmartStamp.data.ServerDataContract;
import com.kt.SmartStamp.listener.LIST_ITEM_LISTENER;

import java.util.ArrayList;

public class RecyclerViewAdapterMainDashboard extends RecyclerView.Adapter<RecyclerViewAdapterMainDashboard.ViewHolder> implements View.OnClickListener {
	private static final long MIN_CLICK_INTERVAL = 1000;
	private long mLastClickTime;

	/******************************************* ViewHolder *******************************************/
	class ViewHolder extends RecyclerView.ViewHolder {
		public LinearLayout item_linearlayout;
		public TextView cont_name_textview;
		public TextView cont_state_textview;
		public TextView cont_date_textview;
		public TextView cont_detail_textview;

		ViewHolder(View view) {
			super(view);
			// 레이아웃 연결
			item_linearlayout = view.findViewById(R.id.item_linearlayout);
			cont_name_textview = view.findViewById(R.id.cont_name_textview);
			cont_state_textview = view.findViewById(R.id.cont_state_textview);
			cont_date_textview = view.findViewById(R.id.cont_date_textview);
			cont_detail_textview = view.findViewById(R.id.cont_detail_textview);
		}
	}

	/*************************************** 클래스 전역변수 영역 *************************************/
	private ArrayList<ServerDataContract> contractArrayList;
	private LayoutInflater layoutInflater;
	private Context context;

	/******************************************** 생성자 **********************************************/
	public RecyclerViewAdapterMainDashboard(Context context, ArrayList<ServerDataContract> contractArrayList, LIST_ITEM_LISTENER EventListener) {
		// 외부변수 연결
		this.context = context;
		this.contractArrayList = contractArrayList;

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
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sublayout_listview_main, parent, false);
		convertView.setOnClickListener(this);
		return new ViewHolder(convertView);
	}
	/*************************************** OnBindViewHolder ***************************************/
	@Override
	public void onBindViewHolder(@NonNull ViewHolder Holder, int position ) {
		// 레이아웃 설정
		Holder.item_linearlayout.setOnClickListener(this);
		Holder.item_linearlayout.setTag(position);

		// 레이아웃 출력
		ServerDataContract serverDataContract = contractArrayList.get(position);
		Holder.cont_name_textview.setText(serverDataContract.cont_name);
		Holder.cont_date_textview.setText("반출 기간 : " + serverDataContract.appr_st_dt + " ~ " + serverDataContract.appr_st_dt);
		Holder.cont_detail_textview.setText(serverDataContract.cont_detail);

		int doc_before_cnt = Integer.parseInt(serverDataContract.doc_before_cnt);
		if (doc_before_cnt > 0) {
			Holder.cont_state_textview.setText("등록중 (" + serverDataContract.doc_before_cnt + ")");
			Holder.cont_state_textview.setTextColor(context.getResources().getColor(R.color.colorAccent));
		} else Holder.cont_state_textview.setText("등록 대기");
	}
	/**************************************** OnViewRecycled ****************************************/
	@Override
	public void onViewRecycled(ViewHolder Holder) {
		super.onViewRecycled(Holder);
	}
	/******************************************** GetCount ********************************************/
	@Override
	public int getItemCount() {
		return contractArrayList.size();
	}

	/************************************** 클릭 이벤트 핸들러 ****************************************/
	@Override
	public void onClick(View view) {
		// 중복 클릭 방지
		long currentClickTime= SystemClock.uptimeMillis();
		long elapsedTime=currentClickTime-mLastClickTime;
		if(elapsedTime<=MIN_CLICK_INTERVAL){
			return;
		}
		mLastClickTime=currentClickTime;

		int position = (int)view.getTag();

		Intent IntentInstance = new Intent(context, DetailReadyActivity.class );
		IntentInstance.putExtra("cont_idx", contractArrayList.get(position).cont_idx);
		context.startActivity( IntentInstance );
	}
}
