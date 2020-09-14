package com.kt.SmartStamp.utility;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.kt.SmartStamp.R;

import java.util.ArrayList;
import java.util.Locale;

public class RequiredPermissionsManager {
	private final int DO_NOT_ASK_AGAIN_DELAY_THRESHOLD =										1500;

	private Context ExternalContext;

	private String [] RequiredPermissionList;
	private boolean [] RequestPermissionResultList;
	private boolean [] AlreadyDeniedAndHidePermissionList;

	private long TimeDistanceFromRequestPermissionToResponse;
	private boolean DeniedPermissionByDoNotAskAgain;

	public RequiredPermissionsManager(Context context ) {
		ExternalContext = context;
		RequiredPermissionList = null;
		TimeDistanceFromRequestPermissionToResponse = 0;
		DeniedPermissionByDoNotAskAgain = false;
	}

	public boolean CheckPermissions( String [] PermissionList ) {
		if( PermissionList == null ) return true;
		RequiredPermissionList = PermissionList;
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) return CheckPermissions();
		else return true;
	}

	private boolean CheckPermissions() {
		boolean CheckResult = true;
		RequestPermissionResultList = new boolean[RequiredPermissionList.length];
		AlreadyDeniedAndHidePermissionList = new boolean[RequiredPermissionList.length];
		for( int i=0; i<RequiredPermissionList.length; i++ ) {
			if( ContextCompat.checkSelfPermission( ExternalContext, RequiredPermissionList[i] ) == PackageManager.PERMISSION_GRANTED ) {
				RequestPermissionResultList[i] = true;
				AlreadyDeniedAndHidePermissionList[i] = false;
			}
			else {
				RequestPermissionResultList[i] = false;
				if( ActivityCompat.shouldShowRequestPermissionRationale( (Activity) ExternalContext, RequiredPermissionList[i] ) ) AlreadyDeniedAndHidePermissionList[i] = true;
				else AlreadyDeniedAndHidePermissionList[i] = false;
				CheckResult = false;
			}
		}
		return CheckResult;
	}

	public void RequestPermissions( String [] PermissionList, int RequestCode ) {
		if( PermissionList == null ) return;
		RequiredPermissionList = PermissionList;
		CheckPermissions();
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
			ArrayList<String> DENIED_PERMISSION_LIST = new ArrayList<>();
			for( int i=0; i<RequiredPermissionList.length; i++ ) if( RequestPermissionResultList[i] == false ) DENIED_PERMISSION_LIST.add( RequiredPermissionList[i] );
			if( DENIED_PERMISSION_LIST.size() > 0 ) {
				TimeDistanceFromRequestPermissionToResponse = SystemClock.elapsedRealtime();
				ActivityCompat.requestPermissions( (Activity) ExternalContext, DENIED_PERMISSION_LIST.toArray( new String[DENIED_PERMISSION_LIST.size()] ), RequestCode );
			}
		}
	}

	public String [] GetGrantedPermissionList() {
		if( RequiredPermissionList == null ) return null;
		ArrayList<String> GRANTED_Permission_LIST = new ArrayList<>();
		for( int i=0; i<RequiredPermissionList.length; i++ ) if( RequestPermissionResultList[i] == true ) GRANTED_Permission_LIST.add( RequiredPermissionList[i] );
		return GRANTED_Permission_LIST.toArray( new String[GRANTED_Permission_LIST.size()] );
	}

	public String [] GetDeniedPermissionList() {
		if( RequiredPermissionList == null ) return null;
		ArrayList<String> DENIED_PERMISSION_LIST = new ArrayList<>();
		for( int i=0; i<RequiredPermissionList.length; i++ ) if( RequestPermissionResultList[i] == false ) DENIED_PERMISSION_LIST.add( RequiredPermissionList[i] );
		return DENIED_PERMISSION_LIST.toArray( new String[DENIED_PERMISSION_LIST.size()] );
	}

	public String [] GetPreviousDeniedPermissionList() {
		if( RequiredPermissionList == null ) return null;
		ArrayList<String> ALREADY_DENIED_AND_HIDE_PERMISSION_LIST = new ArrayList<String>();
		for( int i=0; i<RequiredPermissionList.length; i++ ) if( AlreadyDeniedAndHidePermissionList[i] == true ) ALREADY_DENIED_AND_HIDE_PERMISSION_LIST.add( RequiredPermissionList[i] );
		return ALREADY_DENIED_AND_HIDE_PERMISSION_LIST.toArray( new String[ALREADY_DENIED_AND_HIDE_PERMISSION_LIST.size()] );
	}

	public boolean IsDeniedByDoNotAskAgain() {
		return DeniedPermissionByDoNotAskAgain;
	}

	public void onRequestPermissionsResult( String[] permissions, int[] grantResults ) {
		int DeniedPermissionsCount = 0;
		for( int i=0; i<permissions.length; i++ ) {
			for( int j=0; j<RequiredPermissionList.length; j++ ) {
				if( permissions[i].equalsIgnoreCase( RequiredPermissionList[j] ) ) {
					if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) RequestPermissionResultList[j] = true;
					else {
						RequestPermissionResultList[j] = false;
						DeniedPermissionsCount++;
					}
				}
			}
		}
		// [다시 묻지 않기] 설정에 의해 권한승인요청 절차 없이 즉시 반환된 경우 확인( 즉시 반환 상태의 경우 다시묻지않기 상태로 판단함, 즉시 반환의 기준시간은 0.5초 )
		if( TimeDistanceFromRequestPermissionToResponse > 0 && DeniedPermissionsCount > 0 ) {
			if( SystemClock.elapsedRealtime()-TimeDistanceFromRequestPermissionToResponse < DO_NOT_ASK_AGAIN_DELAY_THRESHOLD ) DeniedPermissionByDoNotAskAgain = true;
			else DeniedPermissionByDoNotAskAgain = false;
		}
		TimeDistanceFromRequestPermissionToResponse = 0;
	}

	public void DisplayPermissionAlertDialog( String [] PermissionList, String DialogMessage ) {
		String Title, PermissionsString = "";
		if( Locale.getDefault().getLanguage().equals( "ko" ) ) Title = "권한 거부 알림";
		else Title = "Permission Alert";
		for( String Permission : PermissionList ) PermissionsString += (Permission.replace( "android.permission.", "" ) + "\n");
		AlertDialog.Builder DialogBuilder = new AlertDialog.Builder( ExternalContext, R.style.Theme_AppCompat_Light_Dialog_Alert );
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) DialogBuilder.setIcon( ExternalContext.getDrawable( android.R.drawable.ic_dialog_alert ) );
		else DialogBuilder.setIcon( ExternalContext.getResources().getDrawable( android.R.drawable.ic_dialog_alert ) );
		DialogBuilder.setTitle( Title );
		DialogBuilder.setPositiveButton( ExternalContext.getString( android.R.string.ok ), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Activity) ExternalContext).finish();
			}
		} );
		DialogBuilder.setOnCancelListener( new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				((Activity) ExternalContext).finish();
			}
		} );
		DialogBuilder.setMessage( PermissionsString + "\n" + DialogMessage );
		DialogBuilder.show();
	}
}
