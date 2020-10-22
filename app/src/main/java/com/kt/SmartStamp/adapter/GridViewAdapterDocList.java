package com.kt.SmartStamp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.kt.SmartStamp.R;
import com.kt.SmartStamp.activity.Activity_ImageViewer_Multi;
import com.kt.SmartStamp.data.ServerDataDoc;

import java.util.ArrayList;

public class GridViewAdapterDocList extends BaseAdapter implements View.OnClickListener {
	/*************************************** 클래스 전역변수 영역 *************************************/
	private ArrayList<ServerDataDoc> ArrayList_Doc;
	private int layerWidth;
	private int layerHeight;
	private LayoutInflater Inflater;
	private Context context;

	LinearLayout LinearLayout_Doc;
	LinearLayout LinearLayout_Doc_Complete;

	private static final long MIN_CLICK_INTERVAL = 1000;
	private long mLastClickTime;

	/******************************************* 생성자 함수 *******************************************/
	public GridViewAdapterDocList(Context context, ArrayList<ServerDataDoc> ArrayList_Doc, int layerWidth) {
		this.context = context;
		this.ArrayList_Doc = ArrayList_Doc;
		this.layerWidth = layerWidth;
		Inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
	}
	/******************************************* getCount **********************************************/
	@Override
	public int getCount() {
		return this.ArrayList_Doc.size();
	}
	/******************************************** getItem **********************************************/
	@Override
	public Object getItem( int Position ) {
		return this.ArrayList_Doc.get(Position);
	}
	/******************************************* getItemId *********************************************/
	@Override
	public long getItemId( int Position ) {
		return Position;
	}
	/******************************************** getView **********************************************/
	@Override
	public View getView(int Position, View ConvertView, ViewGroup Parent) {
		ConvertView = Inflater.inflate(R.layout.sublayout_gridview_doc_list, Parent, false);

		ImageView ImageView_Doc = ConvertView.findViewById(R.id.ImageView_Doc);
		ImageView ImageView_Doc_Complete = ConvertView.findViewById(R.id.ImageView_Doc_Complete);
		ImageView ImageView_Doc_Reg = ConvertView.findViewById(R.id.ImageView_Doc_Reg);

		LinearLayout_Doc = ConvertView.findViewById(R.id.LinearLayout_Doc);
		LinearLayout_Doc_Complete = ConvertView.findViewById(R.id.LinearLayout_Doc_Complete);

		// 레이아웃 출력
		ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) ImageView_Doc.getLayoutParams();
		params.width = (layerWidth - 3) / 2;
		double layerHeight = (layerWidth - 3) / 2 * 4 / 3;
		params.height = Integer.parseInt(String.valueOf(Math.round(layerHeight)));

		LinearLayout_Doc.setLayoutParams(params);
		LinearLayout_Doc.setTag(Position);
		LinearLayout_Doc.setOnClickListener(this);
		String beforeImageURL = ArrayList_Doc.get(Position).bef_picture;
		Glide.with(context).load(beforeImageURL).into(ImageView_Doc);

		LinearLayout_Doc_Complete.setLayoutParams(params);
		LinearLayout_Doc_Complete.setTag(Position);
		LinearLayout_Doc_Complete.setOnClickListener(this);

		if (ArrayList_Doc.get(Position).doc_aft_idx != null
				&& "".equals(ArrayList_Doc.get(Position).doc_aft_idx)) {
			ImageView_Doc_Reg.setVisibility(View.GONE);
			String afterImageURL = ArrayList_Doc.get(Position).aft_picture;
			Glide.with(context).load(afterImageURL).into(ImageView_Doc_Complete);
		} else {
			ImageView_Doc_Complete.setVisibility(View.GONE);
		}

		return ConvertView;
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

		switch(view.getId()) {
			case R.id.LinearLayout_Doc :
				ActionPhotoDialog("doc", Integer.parseInt(view.getTag().toString()));
				break;
			case R.id.LinearLayout_Doc_Complete :
				if (ArrayList_Doc.get(Integer.parseInt(view.getTag().toString())).doc_aft_idx != null
						&& "".equals(ArrayList_Doc.get(Integer.parseInt(view.getTag().toString())).doc_aft_idx)) {
					ActionPhotoDialog("com", Integer.parseInt(view.getTag().toString()));
				}
				break;
		}
	}
	/**************************** 확대보기 *****************************/
	private void ActionPhotoDialog(String type, int dposition) {
		if(ArrayList_Doc.size() > 0) {
			ArrayList<String> ArrayList_MultiView_ProfileImage = new ArrayList<>();
			if ("doc".equals(type)) ArrayList_MultiView_ProfileImage.add(ArrayList_Doc.get(dposition).bef_picture);
			else if ("com".equals(type)) ArrayList_MultiView_ProfileImage.add(ArrayList_Doc.get(dposition).aft_picture);
			Intent IntentInstance = new Intent(context, Activity_ImageViewer_Multi.class);
			IntentInstance.putExtra(Activity_ImageViewer_Multi.INTENT_DATA_IMAGE_PATH_LIST, ArrayList_MultiView_ProfileImage);
			IntentInstance.putExtra(Activity_ImageViewer_Multi.INTENT_DATA_ITEM_POSITION, 0);
			context.startActivity(IntentInstance);
		}
	}
}
