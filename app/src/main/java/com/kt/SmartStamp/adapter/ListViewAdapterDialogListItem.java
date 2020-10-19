package com.kt.SmartStamp.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kt.SmartStamp.BuildConfig;
import com.kt.SmartStamp.R;

import java.util.ArrayList;

public class ListViewAdapterDialogListItem extends BaseAdapter implements View.OnClickListener {
	/******************************************* 내부 클래스 *******************************************/
	public static class DialogItem {
		public int Index;
		public String Text;
		public boolean Selected;
		public DialogItem( int Index, String Text, boolean Selected ) {
			this.Index = Index;
			this.Text = Text;
			this.Selected = Selected;
		}
	}
	/*************************************** 클래스 전역변수 영역 *************************************/
	private ArrayList<DialogItem> ArrayList_DataSrc;

	private LayoutInflater Inflater;

	/******************************************* 생성자 함수 *******************************************/
	public ListViewAdapterDialogListItem(Context context, ArrayList<DialogItem> ArrayList_DataSrc ) {
		this.ArrayList_DataSrc = ArrayList_DataSrc;

		Inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
	}
	/******************************************* getCount **********************************************/
	@Override
	public int getCount() {
		return ArrayList_DataSrc.size();
	}
	/******************************************** getItem **********************************************/
	@Override
	public Object getItem( int Position ) {
		return ArrayList_DataSrc.get( Position );
	}
	/******************************************* getItemId *********************************************/
	@Override
	public long getItemId( int Position ) {
		return Position;
	}
	/******************************************** getView **********************************************/
	@Override
	public View getView( int Position, View ConvertView, ViewGroup Parent ) {
		ConvertView = Inflater.inflate( R.layout.sublayout_listview_dialog_listitem, Parent, false );

		// 레이아웃 연결
		TextView TextView_ItemText = ConvertView.findViewById( R.id.TextView_ItemText );
		ImageView ImageView_ItemSelected = ConvertView.findViewById( R.id.ImageView_ItemSelected );
		// 레이아웃 출력
		try {
			if( Position % 2 == 1 ) ConvertView.setBackgroundResource( R.color.Common_Dialog_EvenLayer );
			else ConvertView.setBackgroundResource( R.color.Common_Background_White );
			TextView_ItemText.setText( Html.fromHtml( ArrayList_DataSrc.get( Position ).Text ) );
			ImageView_ItemSelected.setSelected( ArrayList_DataSrc.get( Position ).Selected );
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
		}
		return ConvertView;
	}
	/*************************************** 클릭 이벤트 핸들러 ***************************************/
	@Override
	public void onClick(View view) {
	}
	/****************************************************************************************************/
}
