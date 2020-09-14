package com.kt.SmartStamp.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kt.SmartStamp.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BitmapDiskCache {
	public static final int CACHE_IMAGE_TYPE_JPG = 														1;
	public static final int CACHE_IMAGE_TYPE_PNG = 														2;

	private String InternalCacheDir;

	public BitmapDiskCache(Context context ) {
		InternalCacheDir = context.getCacheDir().getPath();
	}

	public void WriteBitmapImageCache( final String HttpRequestURL, final Bitmap BitmapSource, final int ImageType, final String InstantCacheFilePrefix ) {
		final String ImageFilePath = ConvertURLtoCacheFilePath( HttpRequestURL, InstantCacheFilePrefix );
		final String WritingTemporaryFileName = ImageFilePath + "_www";
		File FileInstance = new File( WritingTemporaryFileName );

		if( FileInstance.exists() == false ) {
			new Thread( new Runnable() {
				public void run() {
					File FileInstance = new File( WritingTemporaryFileName );
					FileOutputStream WriteCacheFile = null;
					try {
						FileInstance.delete();
						FileInstance.createNewFile();
						WriteCacheFile = new FileOutputStream( FileInstance );
						if( ImageType == CACHE_IMAGE_TYPE_JPG ) BitmapSource.compress( Bitmap.CompressFormat.JPEG, 100, WriteCacheFile );
						else BitmapSource.compress( Bitmap.CompressFormat.PNG, 100, WriteCacheFile );
						WriteCacheFile.close();
						DeleteFile( ImageFilePath );
						if( RenameFile( WritingTemporaryFileName, ImageFilePath ) == false ) {
							DeleteFile( WritingTemporaryFileName );
							throw new Exception( "Rename Error " + ImageFilePath );
						}
					}
					catch( Exception e ) {
						if( BuildConfig.DEBUG ) e.printStackTrace();
						try {
							if( WriteCacheFile != null ) WriteCacheFile.close();
							if( FileInstance.exists() ) FileInstance.delete();
						}
						catch( IOException e2 ) { }
					}
				}
			} ).start();
		}
	}

	public Bitmap LoadBitmapImageCache( String HttpRequestURL, String InstantCacheFilePrefix ) {
		if( HttpRequestURL == null ) return null;
		String ImageFilePath = ConvertURLtoCacheFilePath( HttpRequestURL, InstantCacheFilePrefix );
		return GetBitmapFromCacheFile( ImageFilePath );
	}

	private String ConvertURLtoCacheFilePath( String HttpRequestURL, String InstantCacheFilePrefix ) {
		return InternalCacheDir + "/" + GetMD5Hash( HttpRequestURL ) + GetFileNameFromFilePath( HttpRequestURL ) + (InstantCacheFilePrefix == null ? "" : InstantCacheFilePrefix);
	}

	private boolean DeleteFile( String FilePath ) {
		File FileInstance = new File( FilePath );
		return FileInstance.delete();
	}

	private boolean RenameFile( String OriginalFilePath, String MoveFilePath ) {
		File From = new File( OriginalFilePath );
		File To = new File( MoveFilePath );
		return From.renameTo( To );
	}

	private String GetFileNameFromFilePath( String FilePath ) {
		if( FilePath == null ) return "";
		int StringIndex = FilePath.lastIndexOf( File.separatorChar );
		if( StringIndex == -1 ) return FilePath;
		String FileName = FilePath.substring( StringIndex+1, FilePath.length() );
		if( FileName.length() > 0 ) return FileName;
		else return "";
	}

	public static String GetMD5Hash( String OriginalString ) {
		String HashResult = null;
		try {
			MessageDigest DigestInstance = MessageDigest.getInstance( "MD5" );
			DigestInstance.update( OriginalString.getBytes() );
			HashResult = new BigInteger( 1, DigestInstance.digest() ).toString( 16 );
		}
		catch( NoSuchAlgorithmException e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
		}
		return HashResult;
	}
	
	private Bitmap GetBitmapFromCacheFile( String ImageFilePath ) {
		File FileInstance;
		Bitmap CacheBitmap;
		BitmapFactory.Options BitmapOptions;
		try {
			FileInstance = new File( ImageFilePath );
			if( FileInstance.exists() == false ) return null;
			BitmapOptions = new BitmapFactory.Options();
			BitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
			BitmapOptions.inDither = false;
			CacheBitmap = BitmapFactory.decodeFile( ImageFilePath, BitmapOptions );
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
			return null;
		}
		return CacheBitmap;
	}
}
