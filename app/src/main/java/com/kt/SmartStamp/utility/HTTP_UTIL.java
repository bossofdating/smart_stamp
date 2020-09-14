package com.kt.SmartStamp.utility;

import android.util.Log;

import com.kt.SmartStamp.BuildConfig;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HTTP_UTIL {
	private final int DEFINE_HTTP_CONNECTION_TIMEOUT = 											7000;
	private final int DEFINE_HTTP_READ_TIMEOUT = 														7000;

	private ArrayList<NameValuePair> HeaderDataNameValue;

	protected  String REST_API_AUTHORIZE_KEY;
	protected int RESPONSE_CODE;

	protected HTTP_UTIL() {
		HeaderDataNameValue = new ArrayList<>();
	}

	protected HTTP_UTIL(String AuthorizeKey ) {
		REST_API_AUTHORIZE_KEY = AuthorizeKey;
		HeaderDataNameValue = new ArrayList<>();
	}

	public void AddHeaderData( String DataName, String DataValue ) {
		if( BuildConfig.DEBUG ) Log.d( "DEBUG", String.format( "[HTTP Request] - %s : %s / Encode Json true", DataName, DataValue ) );
		HeaderDataNameValue.add( new NameValuePair( DataName, DataValue ) );
	}

	public void AddHeaderData( String DataName, String DataValue, boolean NeedEncodeJson ) {
		if( BuildConfig.DEBUG ) Log.d( "DEBUG", String.format( "[HTTP Request] - %s : %s / Encode Json %s", DataName, DataValue, NeedEncodeJson ? "true" : "false" ) );
		HeaderDataNameValue.add( new NameValuePair( DataName, DataValue, NeedEncodeJson ) );
	}

	public String RequestHttpGetPut( String RequestURL, String Method, String Token ) {
		URL URLInstance;
		HttpURLConnection HTTPURLConnectionInstance;
		InputStream InputStream_HTTPContents;
		String String_HTTPContents;

		BufferedReader BufferedReader_Temp;
		StringBuffer StringBuffer_Temp;

		try {
			StringBuilder StringBuilder_CombinedURL = new StringBuilder( RequestURL );
			for( int i=0; i<HeaderDataNameValue.size(); i++  ) StringBuilder_CombinedURL.append( i == 0 ? "?" : "&" ).append( HeaderDataNameValue.get( i ).getName() ).append( "=" ).append( HeaderDataNameValue.get( i ).getValue() );
			URLInstance = new URL( StringBuilder_CombinedURL.toString() );
			HTTPURLConnectionInstance = (HttpURLConnection ) URLInstance.openConnection();
			HTTPURLConnectionInstance.setDoInput( true );
			HTTPURLConnectionInstance.setDoOutput( false );
			HTTPURLConnectionInstance.setUseCaches( false );
			HTTPURLConnectionInstance.setConnectTimeout( DEFINE_HTTP_CONNECTION_TIMEOUT );
			HTTPURLConnectionInstance.setReadTimeout( DEFINE_HTTP_READ_TIMEOUT );
			HTTPURLConnectionInstance.setRequestMethod( Method );
			HTTPURLConnectionInstance.setRequestProperty( "Accept", "*/*" );
			HTTPURLConnectionInstance.setRequestProperty( "User-Agent", "Mozilla/5.0 ( compatible )" );
			HTTPURLConnectionInstance.setRequestProperty( "Connection", "Keep-Alive" );
			if( REST_API_AUTHORIZE_KEY != null && Token != null ) HTTPURLConnectionInstance.setRequestProperty( REST_API_AUTHORIZE_KEY, Token );

			InputStream_HTTPContents = HTTPURLConnectionInstance.getInputStream();
			BufferedReader_Temp = new BufferedReader( new InputStreamReader( InputStream_HTTPContents, "UTF-8"  ) );
			StringBuffer_Temp = new StringBuffer();
			while( ( String_HTTPContents = BufferedReader_Temp.readLine() ) != null ) {
				StringBuffer_Temp.append( String_HTTPContents );
			}
			String_HTTPContents = StringBuffer_Temp.toString();
			InputStream_HTTPContents.close();
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
			HeaderDataNameValue.clear();
			return null;
		}
		try { RESPONSE_CODE = HTTPURLConnectionInstance.getResponseCode(); }
		catch( IOException e ) { RESPONSE_CODE = 0; }
		HeaderDataNameValue.clear();
		HTTPURLConnectionInstance.disconnect();
		return String_HTTPContents;
	}

	public String RequestHttpGetPutSSL( String RequestURL, String Method, String Token ) {
		URL URLInstance;
		HttpsURLConnection HTTPSURLConnectionInstance = null;
		InputStream InputStream_HTTPContents;
		String String_HTTPContents;

		BufferedReader BufferedReader_Temp;
		StringBuffer StringBuffer_Temp;

		try {
			StringBuilder StringBuilder_CombinedURL = new StringBuilder( RequestURL );
			for( int i=0; i<HeaderDataNameValue.size(); i++  ) StringBuilder_CombinedURL.append( i == 0 ? "?" : "&" ).append( HeaderDataNameValue.get( i ).getName() ).append( "=" ).append( HeaderDataNameValue.get( i ).getValue() );
			URLInstance = new URL( StringBuilder_CombinedURL.toString() );
			KeyStore TrustKeyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
			TrustKeyStore.load( null, null );
			CustomSSLSocketFactory CustomSSLFactory = new CustomSSLSocketFactory( TrustKeyStore );
			HTTPSURLConnectionInstance = (HttpsURLConnection ) URLInstance.openConnection();
			HTTPSURLConnectionInstance.setSSLSocketFactory( CustomSSLFactory );
			HTTPSURLConnectionInstance.setHostnameVerifier( new AllowAllHostnameVerifier() );
			HTTPSURLConnectionInstance.setDoInput( true );
			HTTPSURLConnectionInstance.setDoOutput( false );
			HTTPSURLConnectionInstance.setUseCaches( false );
			HTTPSURLConnectionInstance.setConnectTimeout( DEFINE_HTTP_CONNECTION_TIMEOUT );
			HTTPSURLConnectionInstance.setReadTimeout( DEFINE_HTTP_READ_TIMEOUT );
			HTTPSURLConnectionInstance.setRequestMethod( Method );
			HTTPSURLConnectionInstance.setRequestProperty( "Accept", "*/*" );
			HTTPSURLConnectionInstance.setRequestProperty( "User-Agent", "Mozilla/5.0 ( compatible )" );
			HTTPSURLConnectionInstance.setRequestProperty( "Connection", "Keep-Alive" );
			if( REST_API_AUTHORIZE_KEY != null && Token != null ) HTTPSURLConnectionInstance.setRequestProperty( REST_API_AUTHORIZE_KEY, Token );

			InputStream_HTTPContents = HTTPSURLConnectionInstance.getInputStream();
			BufferedReader_Temp = new BufferedReader( new InputStreamReader( InputStream_HTTPContents, "UTF-8"  ) );
			StringBuffer_Temp = new StringBuffer();
			while( ( String_HTTPContents = BufferedReader_Temp.readLine() ) != null ) { StringBuffer_Temp.append( String_HTTPContents ); }
			String_HTTPContents = StringBuffer_Temp.toString();
			InputStream_HTTPContents.close();
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
			String_HTTPContents = null;
		}
		try { RESPONSE_CODE = HTTPSURLConnectionInstance.getResponseCode(); }
		catch( IOException e ) { RESPONSE_CODE = 0; }
		HeaderDataNameValue.clear();
		HTTPSURLConnectionInstance.disconnect();
		return String_HTTPContents;
	}

	public String RequestHttpPostDeletePatch( String RequestURL, String Method, String Token ) {
		URL URLInstance;
		HttpURLConnection HTTPURLConnectionInstance;
		OutputStream OutputStreamInstance;
		InputStream InputStream_HTTPContents;
		String String_HTTPContents;

		BufferedReader BufferedReader_Temp;
		StringBuffer StringBuffer_Temp;

		try {
			URLInstance = new URL( RequestURL );
			HTTPURLConnectionInstance = (HttpURLConnection ) URLInstance.openConnection();
			HTTPURLConnectionInstance.setDoInput( true );
			HTTPURLConnectionInstance.setDoOutput( true );
			HTTPURLConnectionInstance.setUseCaches( false );
			HTTPURLConnectionInstance.setConnectTimeout( DEFINE_HTTP_CONNECTION_TIMEOUT );
			HTTPURLConnectionInstance.setReadTimeout( DEFINE_HTTP_READ_TIMEOUT );
			HTTPURLConnectionInstance.setRequestMethod( Method );
			HTTPURLConnectionInstance.setRequestProperty( "Accept", "application/json" );
			HTTPURLConnectionInstance.setRequestProperty( "User-Agent", "Mozilla/5.0 ( compatible )" );
			HTTPURLConnectionInstance.setRequestProperty( "Connection", "Keep-Alive" );
			HTTPURLConnectionInstance.setRequestProperty( "Content-Type", "application/json" );
			if( REST_API_AUTHORIZE_KEY != null && Token != null ) HTTPURLConnectionInstance.setRequestProperty( REST_API_AUTHORIZE_KEY, Token );

			OutputStreamInstance = HTTPURLConnectionInstance.getOutputStream();
			boolean IndividualJsonAssigned = false;
			for( int i=0; i<HeaderDataNameValue.size(); i++  ) {
				if( !HeaderDataNameValue.get( i ).NeedEncodeJson() ) {
					OutputStreamInstance.write( HeaderDataNameValue.get( i ).getValue().getBytes( "UTF-8" ) );
					IndividualJsonAssigned = true;
					break;
				}
			}
			if( !IndividualJsonAssigned ) {
				JSONObject JsonObject = new JSONObject();
				for( int i = 0; i < HeaderDataNameValue.size(); i++ ) JsonObject.put( HeaderDataNameValue.get( i ).getName(), HeaderDataNameValue.get( i ).getValue() );
				OutputStreamInstance.write( JsonObject.toString().getBytes( "UTF-8" ) );
			}
			OutputStreamInstance.flush();
			OutputStreamInstance.close();

			InputStream_HTTPContents = HTTPURLConnectionInstance.getInputStream();
            BufferedReader_Temp = new BufferedReader( new InputStreamReader( InputStream_HTTPContents, "UTF-8"  ) );
            StringBuffer_Temp = new StringBuffer();
            while( ( String_HTTPContents = BufferedReader_Temp.readLine() ) != null ) {
            	StringBuffer_Temp.append( String_HTTPContents );
            }
            String_HTTPContents = StringBuffer_Temp.toString();
            InputStream_HTTPContents.close();
            RESPONSE_CODE = HTTPURLConnectionInstance.getResponseCode();
        }
        catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
        	HeaderDataNameValue.clear();
        	return null;
        }
		try { RESPONSE_CODE = HTTPURLConnectionInstance.getResponseCode(); }
		catch( IOException e ) { RESPONSE_CODE = 0; }
        HeaderDataNameValue.clear();
        HTTPURLConnectionInstance.disconnect();
        return String_HTTPContents;
	}

	public String RequestHttpPostDeletePatchSSL( String RequestURL, String Method, String Token ) {
		URL URLInstance;
		HttpsURLConnection HTTPSURLConnectionInstance = null;
		OutputStream OutputStreamInstance;
		InputStream InputStream_HTTPContents;
		String String_HTTPContents;

		BufferedReader BufferedReader_Temp;
		StringBuffer StringBuffer_Temp;

		try {
			URLInstance = new URL( RequestURL );
			KeyStore TrustKeyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
			TrustKeyStore.load( null, null );
			CustomSSLSocketFactory CustomSSLFactory = new CustomSSLSocketFactory( TrustKeyStore );
			HTTPSURLConnectionInstance = (HttpsURLConnection ) URLInstance.openConnection();
			HTTPSURLConnectionInstance.setSSLSocketFactory( CustomSSLFactory );
			HTTPSURLConnectionInstance.setHostnameVerifier( new AllowAllHostnameVerifier() );
			HTTPSURLConnectionInstance.setDoInput( true );
			HTTPSURLConnectionInstance.setDoOutput( true );
			HTTPSURLConnectionInstance.setUseCaches( false );
			HTTPSURLConnectionInstance.setConnectTimeout( DEFINE_HTTP_CONNECTION_TIMEOUT );
			HTTPSURLConnectionInstance.setReadTimeout( DEFINE_HTTP_READ_TIMEOUT );
			HTTPSURLConnectionInstance.setRequestMethod( Method );
			HTTPSURLConnectionInstance.setRequestProperty( "Accept", "application/json" );
			HTTPSURLConnectionInstance.setRequestProperty( "User-Agent", "Mozilla/5.0 ( compatible )" );
			HTTPSURLConnectionInstance.setRequestProperty( "Connection", "Keep-Alive" );
			HTTPSURLConnectionInstance.setRequestProperty( "Content-Type", "application/json" );
			if( REST_API_AUTHORIZE_KEY != null && Token != null ) HTTPSURLConnectionInstance.setRequestProperty( REST_API_AUTHORIZE_KEY, Token );

			OutputStreamInstance = HTTPSURLConnectionInstance.getOutputStream();
			boolean IndividualJsonAssigned = false;
			for( int i=0; i<HeaderDataNameValue.size(); i++  ) {
				if( !HeaderDataNameValue.get( i ).NeedEncodeJson() ) {
					OutputStreamInstance.write( HeaderDataNameValue.get( i ).getValue().getBytes( "UTF-8" ) );
					IndividualJsonAssigned = true;
					break;
				}
			}
			if( !IndividualJsonAssigned ) {
				JSONObject JsonObject = new JSONObject();
				for( int i = 0; i < HeaderDataNameValue.size(); i++ ) JsonObject.put( HeaderDataNameValue.get( i ).getName(), HeaderDataNameValue.get( i ).getValue() );
				OutputStreamInstance.write( JsonObject.toString().getBytes( "UTF-8" ) );
			}
			OutputStreamInstance.flush();
			OutputStreamInstance.close();

			InputStream_HTTPContents = HTTPSURLConnectionInstance.getInputStream();
			BufferedReader_Temp = new BufferedReader( new InputStreamReader( InputStream_HTTPContents, "UTF-8"  ) );
			StringBuffer_Temp = new StringBuffer();
			while( ( String_HTTPContents = BufferedReader_Temp.readLine() ) != null ) { StringBuffer_Temp.append( String_HTTPContents ); }
			String_HTTPContents = StringBuffer_Temp.toString();
			InputStream_HTTPContents.close();
			RESPONSE_CODE = HTTPSURLConnectionInstance.getResponseCode();
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
			String_HTTPContents = null;
		}
		try { RESPONSE_CODE = HTTPSURLConnectionInstance.getResponseCode(); }
		catch( IOException e ) { RESPONSE_CODE = 0; }
		HeaderDataNameValue.clear();
		HTTPSURLConnectionInstance.disconnect();
		return String_HTTPContents;
	}

	public InputStream GetInputStream( String RequestURL ) {
		HttpURLConnection HTTPURLConnectionInstance;
		InputStream InputStream_HTTPContents;

		try {
			URL URLInstance = new URL( RequestURL );
			HTTPURLConnectionInstance = (HttpURLConnection ) URLInstance.openConnection();
			HTTPURLConnectionInstance.setDoInput( true );
			HTTPURLConnectionInstance.setDoOutput( false );
			HTTPURLConnectionInstance.setUseCaches( false );
			HTTPURLConnectionInstance.setConnectTimeout( DEFINE_HTTP_CONNECTION_TIMEOUT );
			HTTPURLConnectionInstance.setReadTimeout( DEFINE_HTTP_READ_TIMEOUT );
			HTTPURLConnectionInstance.setRequestMethod( "GET" );
			HTTPURLConnectionInstance.setRequestProperty( "Accept", "*/*" );
			HTTPURLConnectionInstance.setRequestProperty( "User-Agent", "Mozilla/5.0 ( compatible )" );
			HTTPURLConnectionInstance.setRequestProperty( "Connection", "Keep-Alive" );

			InputStream_HTTPContents = HTTPURLConnectionInstance.getInputStream();
			RESPONSE_CODE = HTTPURLConnectionInstance.getResponseCode();
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
			RESPONSE_CODE = -1;
			InputStream_HTTPContents = null;
		}
		if( RESPONSE_CODE / 100 == 2 ) return InputStream_HTTPContents;
		else return null;
	}

	public String RequestDataUpload( String RequestURL, String Method, String Token, InputStream UploadData, String FileParameterName, String ServerStoreFileName ) {
		// HTTP 프로토콜 사전 정의 문자열
		String HTTP_DEFINE_STRING_BOUNDARY = 				"*****";
		String HTTP_DEFINE_STRING_LINE_END =	 				"\r\n";
		String HTTP_DEFINE_STRING_HYPHEN =	 					"--";

		HttpURLConnection HTTPURLConnectionInstance;
		DataOutputStream DataOutputStreamInstance;
		InputStream InputStreamInstance;
		BufferedReader BufferedReader_Temp;
		StringBuffer StringBuffer_Temp;
		String String_HTTPContents;
		BufferedInputStream BufferInputStream;
		int ReadBufferCount;
		byte [] Buffer;

		try {
			Buffer = new byte[10240];
			RESPONSE_CODE = 0;
			URL URLInstance = new URL( RequestURL );
			HTTPURLConnectionInstance = (HttpURLConnection) URLInstance.openConnection();
			HTTPURLConnectionInstance.setDoInput( true );
			HTTPURLConnectionInstance.setDoOutput( true );
			HTTPURLConnectionInstance.setUseCaches( false );
			HTTPURLConnectionInstance.setRequestMethod( "POST" );
			HTTPURLConnectionInstance.setRequestProperty( "Accept", "*/*" );
			HTTPURLConnectionInstance.setRequestProperty( "User-Agent", "Mozilla/5.0 ( compatible )" );
			HTTPURLConnectionInstance.setRequestProperty( "Connection", "Keep-Alive" );
			if( REST_API_AUTHORIZE_KEY != null && Token != null ) HTTPURLConnectionInstance.setRequestProperty( REST_API_AUTHORIZE_KEY, Token );
			HTTPURLConnectionInstance.setRequestProperty( "Content-Type", "multipart/form-data;boundary=" + HTTP_DEFINE_STRING_BOUNDARY );

			DataOutputStreamInstance = new DataOutputStream( HTTPURLConnectionInstance.getOutputStream() );
			// 1차 멀티파트 설정
			DataOutputStreamInstance.writeBytes( HTTP_DEFINE_STRING_HYPHEN + HTTP_DEFINE_STRING_BOUNDARY + HTTP_DEFINE_STRING_LINE_END );
			DataOutputStreamInstance.writeBytes( "Content-Disposition: form-data; name=\"" + FileParameterName + "\"; filename=\"" + ServerStoreFileName + "\"" + HTTP_DEFINE_STRING_LINE_END );
			DataOutputStreamInstance.writeBytes( HTTP_DEFINE_STRING_LINE_END );
			BufferInputStream = new BufferedInputStream( UploadData );
			while( (ReadBufferCount = BufferInputStream.read( Buffer, 0, Buffer.length )) != -1 ) DataOutputStreamInstance.write( Buffer, 0, ReadBufferCount );
			DataOutputStreamInstance.writeBytes( HTTP_DEFINE_STRING_LINE_END );
			DataOutputStreamInstance.writeBytes( HTTP_DEFINE_STRING_HYPHEN + HTTP_DEFINE_STRING_BOUNDARY + HTTP_DEFINE_STRING_HYPHEN + HTTP_DEFINE_STRING_LINE_END );
			BufferInputStream.close();
			DataOutputStreamInstance.flush();
			DataOutputStreamInstance.close();

			/*
			// 2차 멀티파트 설정
			DataOutputStreamInstance.writeBytes( HTTP_DEFINE_STRING_HYPHEN + HTTP_DEFINE_STRING_BOUNDARY + HTTP_DEFINE_STRING_LINE_END );
//			DataOutputStreamInstance.writeBytes( "Content-Disposition: form-data; name=\"" + HeaderDataNameValue.get( 0 ).getName() + "\"" + HTTP_DEFINE_STRING_LINE_END );
			DataOutputStreamInstance.writeBytes( "Content-Disposition: form-data; name=\"" + HeaderDataNameValue.get( 0 ).getName() + "\"; filename=\"" + "pictureVO.json" + "\"" + HTTP_DEFINE_STRING_LINE_END );
			DataOutputStreamInstance.writeBytes( HTTP_DEFINE_STRING_LINE_END );
			DataOutputStreamInstance.writeBytes( HeaderDataNameValue.get( 0 ).getValue() );
			DataOutputStreamInstance.writeBytes( HTTP_DEFINE_STRING_LINE_END );
			DataOutputStreamInstance.writeBytes( HTTP_DEFINE_STRING_HYPHEN + HTTP_DEFINE_STRING_BOUNDARY + HTTP_DEFINE_STRING_HYPHEN + HTTP_DEFINE_STRING_LINE_END );	// 멀티파트 전송 종료
			*/

			RESPONSE_CODE = HTTPURLConnectionInstance.getResponseCode();
			InputStreamInstance = HTTPURLConnectionInstance.getInputStream();
			BufferedReader_Temp = new BufferedReader( new InputStreamReader( InputStreamInstance, "UTF-8" ) );
			StringBuffer_Temp = new StringBuffer();
			while( ( String_HTTPContents = BufferedReader_Temp.readLine() ) != null ) StringBuffer_Temp.append( String_HTTPContents );
			String_HTTPContents = StringBuffer_Temp.toString();
			InputStreamInstance.close();
		}
		catch( Exception e ) {
			if( BuildConfig.DEBUG ) e.printStackTrace();
			RESPONSE_CODE = -1;
			String_HTTPContents = null;
		}
		return String_HTTPContents;
	}

	private class CustomSSLSocketFactory extends SSLSocketFactory {
		private SSLContext SSLContextInstance = SSLContext.getInstance( "TLS" );

		public CustomSSLSocketFactory( KeyStore TrustKeyStore ) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super();
			TrustManager TrustManagerInstance = new X509TrustManager() {
				@Override
				public void checkClientTrusted( java.security.cert.X509Certificate[] chain, String authType ) throws CertificateException {
				}
				@Override
				public void checkServerTrusted( java.security.cert.X509Certificate[] chain, String authType ) throws CertificateException {
				}
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			SSLContextInstance.init( null, new TrustManager [] { TrustManagerInstance }, null );
		}
		@Override
		public Socket createSocket( Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return SSLContextInstance.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return SSLContextInstance.getSocketFactory().createSocket();
		}
		@Override
		public String[] getDefaultCipherSuites() {
			return null;
		}
		@Override
		public String[] getSupportedCipherSuites() {
			return null;
		}
		@Override
		public Socket createSocket( String host, int port ) throws IOException, UnknownHostException {
			return null;
		}
		@Override
		public Socket createSocket( String host, int port, InetAddress localHost, int localPort ) throws IOException, UnknownHostException {
			return null;
		}
		@Override
		public Socket createSocket( InetAddress host, int port ) throws IOException {
			return null;
		}
		@Override
		public Socket createSocket( InetAddress address, int port, InetAddress localAddress, int localPort ) throws IOException {
			return null;
		}
	}
}
