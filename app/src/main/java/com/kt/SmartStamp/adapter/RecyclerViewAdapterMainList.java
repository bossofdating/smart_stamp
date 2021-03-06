package com.kt.SmartStamp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.kt.SmartStamp.activity.DetailListActivity;
import com.kt.SmartStamp.data.ServerDataContract;
import com.kt.SmartStamp.fragment.FragmentMainList;
import com.kt.SmartStamp.listener.LIST_ITEM_LISTENER;

import java.util.ArrayList;

public class RecyclerViewAdapterMainList extends RecyclerView.Adapter<RecyclerViewAdapterMainList.ViewHolder> implements View.OnClickListener {
	private static final long MIN_CLICK_INTERVAL = 1000;
	private long mLastClickTime;

	/******************************************* ViewHolder *******************************************/
	class ViewHolder extends RecyclerView.ViewHolder {
		public LinearLayout item_linearlayout;
		public TextView cont_name_textview;
		public TextView cont_state_textview;
		public TextView cont_date_textview;
		public TextView cont_detail_textview;

		private static final long MIN_CLICK_INTERVAL = 1000;
		private long mLastClickTime;

		ViewHolder(View view) {
			super(view);
			// 레이아웃 연결
			item_linearlayout = view.findViewById(R.id.item_linearlayout);
			cont_name_textview = view.findViewById( R.id.cont_name_textview );
			cont_state_textview = view.findViewById( R.id.cont_state_textview );
			cont_date_textview = view.findViewById( R.id.cont_date_textview );
			cont_detail_textview = view.findViewById( R.id.cont_detail_textview );
		}
	}

	/*************************************** 클래스 전역변수 영역 *************************************/
	private ArrayList<ServerDataContract> contractArrayList;
	private LayoutInflater layoutInflater;
	private Context context;
	private FragmentMainList fragment;

	/******************************************** 생성자 **********************************************/
	public RecyclerViewAdapterMainList(Context context, ArrayList<ServerDataContract> contractArrayList, LIST_ITEM_LISTENER EventListener, FragmentMainList fragment) {
		// 외부변수 연결
		this.context = context;
		this.contractArrayList = contractArrayList;
		this.fragment = fragment;

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
		Holder.cont_date_textview.setText("반출 기간 : " + serverDataContract.appr_st_dt + " ~ " + serverDataContract.appr_ed_dt);
		Holder.cont_detail_textview.setText(serverDataContract.cont_detail);

		int doc_after_cnt = Integer.parseInt(serverDataContract.doc_after_cnt);
		if (doc_after_cnt > 0) {
			Holder.cont_state_textview.setText("날인중 (" + serverDataContract.doc_after_cnt + "/" + serverDataContract.doc_before_cnt + ")");
			Holder.cont_state_textview.setTextColor(context.getResources().getColor(R.color.colorAccent));
		} else {
			Holder.cont_state_textview.setText("날인 대기 (" + serverDataContract.doc_after_cnt + "/" + serverDataContract.doc_before_cnt + ")");
			Holder.cont_state_textview.setTextColor(context.getResources().getColor(R.color.colorBlue));
		}
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

		if ("y".equals(contractArrayList.get((int)view.getTag()).open_fl)) {
			int position = (int)view.getTag();
			fragment.startActivityfrag(position);
		} else {
			DisplayDialog_Open_Guide();
		}
	}

	// 반출기간 열람 안내
	private void DisplayDialog_Open_Guide() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);
		builder.setTitle("안내");
		builder.setMessage("반출 기간에만 열람이 가능합니다.");
		builder.setCancelable(false);
		builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog theAlertDialog = builder.create();
		theAlertDialog.show();
		TextView textView = theAlertDialog.findViewById(android.R.id.message);
		textView.setTextSize(15.0f);
	}
}
