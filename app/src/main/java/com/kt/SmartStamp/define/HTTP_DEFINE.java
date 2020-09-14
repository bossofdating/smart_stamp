package com.kt.SmartStamp.define;

import android.annotation.SuppressLint;


@SuppressLint( "DefaultLocale" )
public class HTTP_DEFINE {
	// DEV
	private static String SERVER_ADDRESS = "http://3.34.249.16/smartstampAPI/";
    // PROD

	// API URL
	public static String HTTP_URL_LOGIN = SERVER_ADDRESS + "login";
    public static String HTTP_URL_TERMS_LIST = SERVER_ADDRESS + "terms";
	public static String HTTP_URL_TERMS_AGREE = SERVER_ADDRESS + "terms/%s";
	public static String HTTP_URL_CONT_LIST = SERVER_ADDRESS + "contract/%s";
	public static String HTTP_URL_CONT_CNT = SERVER_ADDRESS + "contractcnt/%s";

}
