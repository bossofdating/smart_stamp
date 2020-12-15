package com.kt.SmartStamp.define;

import android.annotation.SuppressLint;

import com.kt.SmartStamp.utility.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


@SuppressLint( "DefaultLocale" )
public class HTTP_DEFINE {
	// AWS
	//private static String SERVER_ADDRESS = "http://3.34.249.16/smartstampAPI/";
	// DEV / PROD
	private static String SERVER_ADDRESS = "https://app.iotstamp.kt.co.kr/smartstampAPI/";

	// API URL
	// AWS
	//public static String HTTP_URL_LOGIN = SERVER_ADDRESS + "login/aws";
	// DEV
	public static String HTTP_URL_LOGIN = SERVER_ADDRESS + "login/dev";
	// PROD
	//public static String HTTP_URL_LOGIN = SERVER_ADDRESS + "login";
	public static String HTTP_URL_KEY_INFO = SERVER_ADDRESS + "keyinfo";
    public static String HTTP_URL_TERMS_LIST = SERVER_ADDRESS + "terms";
	public static String HTTP_URL_TERMS_AGREE = SERVER_ADDRESS + "terms/%s";
	public static String HTTP_URL_CONT_LIST = SERVER_ADDRESS + "contract/%s";
	public static String HTTP_URL_CONT_CNT = SERVER_ADDRESS + "contractcnt/%s";
	public static String HTTP_URL_CONT_DETAIL = SERVER_ADDRESS + "contractdetail/%s";
	public static String HTTP_URL_DOC_LIST = SERVER_ADDRESS + "doclist/%s";
	public static String HTTP_URL_DOC_REG = SERVER_ADDRESS + "incbefdoc/%s/%s";
	public static String HTTP_URL_DOC_MOD = SERVER_ADDRESS + "uptbefdoc/%s/%s";
	public static String HTTP_URL_DOC_DEL = SERVER_ADDRESS + "delbefdoc/%s";
	public static String HTTP_URL_CONT_COMPLETE = SERVER_ADDRESS + "contract/%s";
	public static String HTTP_URL_DOC_REG_AFT = SERVER_ADDRESS + "incaftdoc/%s/%s/%s";
	public static String HTTP_URL_DOC_MOD_AFT = SERVER_ADDRESS + "uptaftdoc/%s/%s";
	public static String HTTP_URL_DOC_DEL_AFT = SERVER_ADDRESS + "delaftdoc/%s";
	public static String HTTP_URL_STAMP_CHECK = SERVER_ADDRESS + "stampcheck/%s";
	public static String HTTP_URL_STAMP = SERVER_ADDRESS + "stamp/%s";
	public static String HTTP_URL_APPR_CONT_CNT = SERVER_ADDRESS + "apprcontractcnt/%s";
	public static String HTTP_URL_APPR_CONT_LIST = SERVER_ADDRESS + "apprcontract/%s";
	public static String HTTP_URL_STAMP_LOCATION = SERVER_ADDRESS + "stamplocation/%s";

	public static String GetUploadImageFileName( int ImageFileType, int MemberIndex, String FileNameExtension ) {
		String FileNamePrefix = "";
		SimpleDateFormat SimpleDateFormatInstance = new SimpleDateFormat( "MMddHHmm", Locale.getDefault() );
		String CurrentDateString = SimpleDateFormatInstance.format( new Date( System.currentTimeMillis() ) );
		switch( ImageFileType ) {
			case COMMON_DEFINE.IMAGE_FILE_TYPE_PROFILE :	FileNamePrefix = "d%d%s%s"; break;
		}
		FileNamePrefix = String.format( FileNamePrefix, MemberIndex, CurrentDateString, FileNameExtension );
		return CommonUtil.GetNoneOverlapFileName( FileNamePrefix );
	}
}
