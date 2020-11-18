package com.kt.SmartStamp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.kt.SmartStamp.activity.DetailReadyActivity;
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

public class FragmentMainSetting extends Fragment {
	private TextView textviewAppversion;

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
		View FragmentView = inflater.inflate(R.layout.fragment_setting, container, false);

		textviewAppversion = FragmentView.findViewById(R.id.textview_appversion);

		displayLayoutDefault();

		return FragmentView;
	}

	@Override
	public void onStart() { super.onStart();	}
	@Override
	public void onDestroyView() {	super.onDestroyView();	}
	@Override
	public void onDetach() {
		super.onDetach();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) { super.onConfigurationChanged(newConfig); }

	/**************************************** 레이아웃 출력 *******************************************/
	private void displayLayoutDefault() {
		textviewAppversion.setText("Version " + getVersionInfo(getContext()));
	}

	/**************************************** 버전 *******************************************/
	public String getVersionInfo(Context context){
		String version = null;
		try {
			PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = i.versionName;
		} catch(PackageManager.NameNotFoundException e) { }
		return version;
	}

}
