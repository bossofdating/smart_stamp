package com.kt.SmartStamp.utility;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kt.SmartStamp.BuildConfig;
import com.kt.SmartStamp.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
	public static InputStream GetInputStreamFromFile( String FilePath ) {
		FileInputStream InputStreamInstance;
		try {
			File FileInstance = new File( FilePath );
			InputStreamInstance = new FileInputStream( FileInstance );
		}
		catch( Exception e ) {
			return null;
		}
		return InputStreamInstance;
	}

	public static boolean WriteFileFromInputStream( String WriteFilePath, InputStream SourceInputStream ) {
		int ReadBufferCount;
		byte [] Buffer;
		try {
			Buffer = new byte[10240];
			File FileInstance = new File( WriteFilePath );
			BufferedInputStream BufferInputStream = new BufferedInputStream( SourceInputStream );
			BufferedOutputStream BufferOutputStream = new BufferedOutputStream( new FileOutputStream( FileInstance ) );
			while( (ReadBufferCount = BufferInputStream.read( Buffer, 0, Buffer.length )) != -1 ) {
				BufferOutputStream.write( Buffer, 0, ReadBufferCount );
			}
			BufferOutputStream.close();
			BufferInputStream.close();
		}
		catch( Exception e ) {
			return false;
		}
		return true;
	}

	public static String GetSHA256Hash( String OriginalString ) {
		String HashResult = null;
		try {
			MessageDigest DigestInstance = MessageDigest.getInstance( "SHA256" );
			DigestInstance.update( OriginalString.getBytes() );
			HashResult = new BigInteger( 1, DigestInstance.digest() ).toString( 16 );
		}
		catch( NoSuchAlgorithmException e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
		}
		return HashResult;
	}

	public static Dialog GetCustomProgressDialog( Activity ActivityInstance ) {
		if( ActivityInstance == null ) return null;
		else {
			ProgressBar ProgressBarInstance = new ProgressBar( ActivityInstance, null, 0, android.R.style.Widget_Holo_Light_ProgressBar );
			Dialog CustomProgressDialog = new Dialog( ActivityInstance, R.style.CustomTransparentProgressDialog );
			CustomProgressDialog.addContentView( ProgressBarInstance, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );
			CustomProgressDialog.setCanceledOnTouchOutside( false );
			CustomProgressDialog.setCancelable( true );
			WindowManager.LayoutParams params = CustomProgressDialog.getWindow().getAttributes();
			params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			params.dimAmount = 0.4f;
			CustomProgressDialog.getWindow().setAttributes( params );
			return CustomProgressDialog;
		}
	}

	public static boolean DeleteFile( String FilePath ) {
		try {
			File FileInstance = new File( FilePath );
			return FileInstance.delete();
		}
		catch( Exception e ) {
			return false;
		}
	}

}