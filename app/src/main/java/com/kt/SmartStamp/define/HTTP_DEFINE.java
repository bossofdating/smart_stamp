package com.kt.SmartStamp.define;

import android.annotation.SuppressLint;

import com.kt.SmartStamp.utility.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


@SuppressLint( "DefaultLocale" )
public class HTTP_DEFINE {
	// DEV
	private static String SERVER_ADDRESS = "http://3.34.249.16/smartstampAPI/";
    // PROD
	//private static String SERVER_ADDRESS = "";

	// API URL
	public static String HTTP_URL_LOGIN = SERVER_ADDRESS + "login";
    public static String HTTP_URL_TERMS_LIST = SERVER_ADDRESS + "terms";
	public static String HTTP_URL_TERMS_AGREE = SERVER_ADDRESS + "terms/%s";
	public static String HTTP_URL_CONT_LIST = SERVER_ADDRESS + "contract/%s";
	public static String HTTP_URL_CONT_CNT = SERVER_ADDRESS + "contractcnt/%s";
	public static String HTTP_URL_CONT_DETAIL = SERVER_ADDRESS + "contractdetail/%s";
	public static String HTTP_URL_DOC_LIST = SERVER_ADDRESS + "doclist/%s";
	public static String HTTP_URL_DOC_REG = SERVER_ADDRESS + "incbefdoc/%s/%s";

	public static String GetUploadImageFileName( int ImageFileType, int MemberIndex, String FileNameExtension ) {
		String FileNamePrefix = "";
		SimpleDateFormat SimpleDateFormatInstance = new SimpleDateFormat( "MMddHHmm", Locale.getDefault() );
		String CurrentDateString = SimpleDateFormatInstance.format( new Date( System.currentTimeMillis() ) );
		switch( ImageFileType ) {
			case COMMON_DEFINE.IMAGE_FILE_TYPE_PROFILE :	FileNamePrefix = "p%08d%d%s%s"; break;
		}
		FileNamePrefix = String.format( FileNamePrefix, MemberIndex, CurrentDateString, FileNameExtension );
		return CommonUtil.GetNoneOverlapFileName( FileNamePrefix );
	}
}
