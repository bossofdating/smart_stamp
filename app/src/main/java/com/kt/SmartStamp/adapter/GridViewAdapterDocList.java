package com.kt.SmartStamp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kt.SmartStamp.R;
import com.kt.SmartStamp.activity.ActivityImageSelector;
import com.kt.SmartStamp.activity.Activity_ImageViewer_Multi;
import com.kt.SmartStamp.activity.DetailListActivity;
import com.kt.SmartStamp.customview.Dialog_Common;
import com.kt.SmartStamp.data.ServerDataDoc;

import java.util.ArrayList;
import java.util.Arrays;

public class GridViewAdapterDocList extends BaseAdapter implements View.OnClickListener {
	/*************************************** 클래스 전역변수 영역 *************************************/
	private ArrayList<ServerDataDoc> ArrayList_Doc;
	private int layerWidth;
	private int layerHeight;
	private LayoutInflater Inflater;
	private Context context;

	LinearLayout LinearLayout_Doc;
	LinearLayout LinearLayout_Doc_Complete;

	private ArrayList<String> SELECTED_PROFILE_PHOTO_DIALOG_ITEM;								// 사진 선택 다이얼로그 가변 적용 보관 변수

	private static final long MIN_CLICK_INTERVAL = 1000;
	private long mLastClickTime;

	private String modifyDocBefIdx;
	private String modifyFl = "n";

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

		if (ArrayList_Doc.get(Position).doc_aft_idx != null && !"".equals(ArrayList_Doc.get(Position).doc_aft_idx)) {
			ImageView_Doc_Reg.setVisibility(View.GONE);
			String afterImageURL = ArrayList_Doc.get(Position).aft_picture;
			Glide.with(context).load(afterImageURL).into(ImageView_Doc_Complete);
		} else {
			ImageView_Doc_Complete.setVisibility(View.GONE);
			ImageView_Doc_Reg.setTag(Position);
			ImageView_Doc_Reg.setOnClickListener(this);
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
				modifyDocBefIdx = view.getTag().toString();
				DisplayDialog_Doc_Act();
				break;
			case R.id.ImageView_Doc_Reg :
				modifyDocBefIdx = view.getTag().toString();
				DisplayDialog_Doc();
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

	/**************************** 사진 등록 다이얼로그 선택 분기 실행 *****************************/
	private void BranchExecutionPhotoDialog(int DialogItemIndex) {
		String [] ProfilePhotoBasisItemList = context.getResources().getStringArray(R.array.photo_select);
		if( SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get(DialogItemIndex).equals( ProfilePhotoBasisItemList[0] ) ) {		// 앨범
			DisplayActivity_SelectPhoto(ActivityImageSelector.SELECTED_TYPE_GALLERY, 1);
		}
		else if( SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get( DialogItemIndex ).equals(ProfilePhotoBasisItemList[1])) {	// 카메라
			DisplayActivity_SelectPhoto(ActivityImageSelector.SELECTED_TYPE_CAMERA, 1);
		}
	}

	/**************************************** 액티비티 출력 *******************************************/
	public void DisplayActivity_SelectPhoto(int SelectedType, int requestCode) {
		((DetailListActivity)DetailListActivity.mContext).DisplayActivity_SelectPhoto(SelectedType, requestCode, modifyDocBefIdx, modifyFl);
	}

	private void ActionPhotoDialogSelect(int DialogItemIndex) {
		String [] ProfilePhotoBasisItemList = context.getResources().getStringArray(R.array.photo_act);
		if (SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get( DialogItemIndex ).equals(ProfilePhotoBasisItemList[1])) {	    // 수정
			modifyFl = "y";
			DisplayDialog_Doc();
		} else if (SELECTED_PROFILE_PHOTO_DIALOG_ITEM.get( DialogItemIndex ).equals(ProfilePhotoBasisItemList[2])) {    	// 삭제
			modifyFl = "n";
			DisplayDialog_Doc_Del();
		}
	}

	/*************************************** 다이얼로그 출력 ******************************************/
	private void DisplayDialog_Doc() {
		Dialog_Common DialogBuilder = new Dialog_Common(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
		DialogBuilder.setTitle("선택");
		SELECTED_PROFILE_PHOTO_DIALOG_ITEM = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.photo_select)));
		DialogBuilder.setListItem(SELECTED_PROFILE_PHOTO_DIALOG_ITEM, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				BranchExecutionPhotoDialog(which);
				dialog.dismiss();
			}
		});
		DialogBuilder.setOnNegativeButtonClickListener("취소", null);
		DialogBuilder.show();
	}
	private void DisplayDialog_Doc_Act() {
		Dialog_Common DialogBuilder = new Dialog_Common(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
		DialogBuilder.setTitle("선택");
		SELECTED_PROFILE_PHOTO_DIALOG_ITEM = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.photo_flag)));
		DialogBuilder.setListItem(SELECTED_PROFILE_PHOTO_DIALOG_ITEM, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ActionPhotoDialogSelect(which);
				dialog.dismiss();
			}
		});
		DialogBuilder.setOnNegativeButtonClickListener("취소", null);
		DialogBuilder.show();
	}
	private void DisplayDialog_Doc_Del() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);
		builder.setTitle("문서 삭제 확인");
		builder.setMessage("문서를 삭제하시겠습니까? 삭제된 문서는 복구가 불가합니다.");
		builder.setNegativeButton("취소", null);
		builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((DetailListActivity)DetailListActivity.mContext).requestHttpDataDocDelete(modifyDocBefIdx);
			}
		});
		AlertDialog theAlertDialog = builder.create();
		theAlertDialog.show();
		TextView textView = theAlertDialog.findViewById(android.R.id.message);
		textView.setTextSize(15.0f);
	}
}
