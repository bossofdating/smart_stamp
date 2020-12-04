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
	public static String GetFileExtension( @NonNull String FileName ) {
		int StringIndex = FileName.lastIndexOf( '.' );
		if( StringIndex == -1 ) return "";
		else return FileName.subSequence( StringIndex, FileName.length() ).toString();
	}

	public static Bitmap GetResizedBitmap( String BitmapFilePath, int ResizedWidth, int ResizedHeight ) {
		Bitmap ScalingBitmap;
		Bitmap ResizeImage = null;
		BitmapFactory.Options BitmapInfo = new BitmapFactory.Options();
		BitmapFactory.Options BitmapOptions  = new BitmapFactory.Options();

		try {
			BitmapInfo.inJustDecodeBounds = true;
			BitmapFactory.decodeFile( BitmapFilePath, BitmapInfo );
			if( ResizedWidth == 0 && ResizedHeight == 0 ) return null;
			if( ResizedWidth == 0 ) BitmapOptions.outWidth = (int) (BitmapInfo.outWidth * ((float) ResizedHeight / BitmapInfo.outHeight));
			else BitmapOptions.outWidth = ResizedWidth;
			if( ResizedHeight == 0 ) BitmapOptions.outHeight = (int) (BitmapInfo.outHeight * ((float) ResizedWidth / BitmapInfo.outWidth));
			else BitmapOptions.outHeight = ResizedHeight;
			ScalingBitmap = BitmapFactory.decodeFile( BitmapFilePath );
			ResizeImage = Bitmap.createScaledBitmap( ScalingBitmap, BitmapOptions.outWidth, BitmapOptions.outHeight, true );
			if( ScalingBitmap.hashCode() != ResizeImage.hashCode() ) ScalingBitmap.recycle();
		}
		catch( Exception e ) {
		}
		return ResizeImage;
	}

	public static int GetRotationDegree( String BitmapFilePath ) {
		int RotationDegree = 0;
		try {
			ExifInterface Exif = new ExifInterface( BitmapFilePath );
			int ExifOrientation = Exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );
			switch( ExifOrientation ) {
				case ExifInterface.ORIENTATION_ROTATE_90 :
					RotationDegree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180 :
					RotationDegree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270 :
					RotationDegree = 270;
					break;
				default :
					RotationDegree = 0;
					break;
			}
		}
		catch( IOException e ) {
		}

		return RotationDegree;
	}

	public static Bitmap GetRotateBitmap( Bitmap SourceBitmap, int RotateDegree, boolean SourceBitmapRecycle ) {
		if( SourceBitmap == null ) return null;
		Matrix MatrixInstance = new Matrix();
		MatrixInstance.postRotate( RotateDegree );
		Bitmap RotateBitmap = Bitmap.createBitmap( SourceBitmap, 0, 0, SourceBitmap.getWidth(), SourceBitmap.getHeight(), MatrixInstance, true );
		if( SourceBitmapRecycle && SourceBitmap.hashCode() != RotateBitmap.hashCode() ) SourceBitmap.recycle();
		return RotateBitmap;
	}

	public static boolean WriteBitmapToFile( Bitmap SourceBitmap, String SaveFileFullPath, int CompressQuality, boolean SourceBitmapRecycle ) {
		try {
			Bitmap.CompressFormat BitmapFormat = Bitmap.CompressFormat.JPEG;
			FileOutputStream FileOutputStreamInstance = new FileOutputStream( SaveFileFullPath );
			if( SourceBitmap.compress( BitmapFormat, CompressQuality, FileOutputStreamInstance ) == false ) return false;
			FileOutputStreamInstance.close();
			if( SourceBitmapRecycle ) SourceBitmap.recycle();
		}
		catch( Exception e ) {
			return false;
		}
		return true;
	}

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

	public static String GetNoneOverlapFileName( String FileName ) {
		int StringIndex;
		String RandomFileName;
		String SystemClockString;
		String FileNameExtension;

		if( FileName == null || FileName.length() == 0 ) return FileName;
		SystemClockString = Long.toString( SystemClock.uptimeMillis() );
		SystemClockString = SystemClockString.substring( SystemClockString.length()-6 );
		RandomFileName = GetFileNameFromFilePath( FileName );
		if( RandomFileName.length() == 0 ) return RandomFileName;

		StringIndex = RandomFileName.lastIndexOf( '.' );
		if( StringIndex == -1 ) RandomFileName = RandomFileName + SystemClockString;
		else {
			FileNameExtension = RandomFileName.substring( StringIndex, RandomFileName.length() );
			RandomFileName = RandomFileName.substring( 0, StringIndex ) + SystemClockString + FileNameExtension;
		}
		return RandomFileName;
	}

	public static String GetFileNameFromFilePath( @NonNull String FilePath ) {
		int StringIndex = FilePath.lastIndexOf( File.separatorChar );
		if( StringIndex == -1 ) return FilePath;
		String FileName = FilePath.substring( StringIndex+1, FilePath.length() );
		if( FileName.length() > 0 ) return FileName;
		else return null;
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