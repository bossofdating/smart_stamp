package com.kt.SmartStamp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.kt.SmartStamp.R;
import com.kt.SmartStamp.data.ServerDataDoc;

import java.util.ArrayList;

public class GridViewAdapterDocReady extends BaseAdapter {
	/*************************************** 클래스 전역변수 영역 *************************************/
	private ArrayList<ServerDataDoc> ArrayList_Doc;
	private int layerWidth;
	private int layerHeight;
	private LayoutInflater Inflater;
	private Context context;

	/******************************************* 생성자 함수 *******************************************/
	public GridViewAdapterDocReady(Context context, ArrayList<ServerDataDoc> ArrayList_Doc, int layerWidth) {
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
		ConvertView = Inflater.inflate(R.layout.sublayout_gridview_doc_ready, Parent, false);

		// 레이아웃 연결
		ImageView ImageView_Doc = ConvertView.findViewById(R.id.ImageView_Doc);
		LinearLayout LinearLayout_Doc = ConvertView.findViewById(R.id.LinearLayout_Doc);
		// 레이아웃 출력
		ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) ImageView_Doc.getLayoutParams();
		params.width = (layerWidth - 3) / 3;
		double layerHeight = (layerWidth - 3) / 3 * 4 / 3;
		params.height = Integer.parseInt(String.valueOf(Math.round(layerHeight)));

		ImageView_Doc.setLayoutParams(params);
		LinearLayout_Doc.setTag(ArrayList_Doc.get(Position).doc_bef_idx);

		String imageURL = ArrayList_Doc.get(Position).bef_picture;
		Glide.with(context).load(imageURL).into(ImageView_Doc);

		return ConvertView;
	}
}
