package com.kt.SmartStamp.utility;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONService {
	private JSONObject RootJSONObject;
	private JSONArray RootJSONArray;
	private JSONObject CurrentNodeJSONObject;
	private JSONArray CurrentNodeJSONArray;

	private int CurrentArrayDataIndex;

	public JSONService() {
		CurrentArrayDataIndex = 0;
	}

	public JSONService(JSONObject JsonObject ) {
		RootJSONArray = null;
		CurrentNodeJSONArray = null;
		RootJSONObject = JsonObject;
		CurrentNodeJSONObject = JsonObject;
		CurrentArrayDataIndex = 0;
	}

	public JSONService(JSONArray JsonArray ) {
			RootJSONArray = JsonArray;
			CurrentNodeJSONArray = RootJSONArray;
			RootJSONObject = null;
			CurrentNodeJSONObject = null;
			CurrentArrayDataIndex = 0;
	}

	public boolean CreateJSONObject( String JsonData ) {
		try {
			RootJSONObject = new JSONObject( JsonData );
			CurrentNodeJSONObject = RootJSONObject;
			RootJSONArray = null;
			CurrentNodeJSONArray = null;
			CurrentArrayDataIndex = 0;
		}
		catch( Exception e ) {
			RootJSONObject = null;
			CurrentNodeJSONObject = null;
			return false;
		}
		return true;
	}

	public boolean CreateJSONObject( JSONObject JsonObject ) {
		try {
			RootJSONObject = JsonObject;
			CurrentNodeJSONObject = RootJSONObject;
			RootJSONArray = null;
			CurrentNodeJSONArray = null;
			CurrentArrayDataIndex = 0;
		}
		catch( Exception e ) {
			RootJSONObject = null;
			CurrentNodeJSONObject = null;
			return false;
		}
		return true;
	}

	public boolean CreateJSONArray( String JsonData ) {
		try {
			RootJSONArray = new JSONArray( JsonData );
			CurrentNodeJSONArray = RootJSONArray;
			RootJSONObject = null;
			CurrentNodeJSONObject = null;
			CurrentArrayDataIndex = 0;
		}
		catch( Exception e ) {
			RootJSONArray = null;
			CurrentNodeJSONArray = null;
			return false;
		}
		return true;
	}

	public boolean SetNextNode() {
		try {
			if( CurrentNodeJSONArray.length() > CurrentArrayDataIndex ) {
				CurrentNodeJSONObject = CurrentNodeJSONArray.getJSONObject( CurrentArrayDataIndex );
				CurrentArrayDataIndex++;
			}
			else {
				return false;
			}
		}
		catch( Exception e ) {
			return false;
		}
		return true;
	}

	public boolean SetNextNode( int NodeIndex ) {
		try {
			CurrentNodeJSONObject = CurrentNodeJSONArray.getJSONObject( NodeIndex );
		}
		catch( Exception e ) {
			return false;
		}
		return true;
	}

	public String GetString( String KeyName ) {
		String StringData;
		try {
			StringData = CurrentNodeJSONObject.getString( KeyName );
			if( StringData.equalsIgnoreCase( "null" ) || StringData.isEmpty() ) return null;
		}
		catch( Exception e ) {
			return null;
		}
		return StringData;
	}

	public String GetString( String KeyName, String DefaultData ) {
		String StringData;
		try {
			StringData = CurrentNodeJSONObject.getString( KeyName );
			if( StringData.equalsIgnoreCase( "null" ) || StringData.isEmpty() ) return DefaultData;
		}
		catch( Exception e ) {
			return DefaultData;
		}
		return StringData;
	}

	public int GetInt( String KeyName ) {
		int IntegerData;
		try {
			IntegerData = CurrentNodeJSONObject.getInt( KeyName );
		}
		catch( Exception e ) {
			return 0;
		}
		return IntegerData;
	}

	public int GetInt( String KeyName, int DefaultData ) {
		int IntegerData;
		try {
			IntegerData = CurrentNodeJSONObject.getInt( KeyName );
		}
		catch( Exception e ) {
			return DefaultData;
		}
		return IntegerData;
	}

	public long GetLong( String KeyName, long DefaultData ) {
		long LongData;
		try {
			LongData = CurrentNodeJSONObject.getLong( KeyName );
		}
		catch( Exception e ) {
			return DefaultData;
		}
		return LongData;
	}

	public <T> T GetClass( Class<T> ClassType ) {
		try {
			return new Gson().fromJson( CurrentNodeJSONObject.toString( 0 ), ClassType );
		}
		catch( Exception e ) {
		}
		return null;
	}

	public <T> T GetClass( String JsonString, Class<T> ClassType ) {
		try {
			return new Gson().fromJson( new JsonParser().parse( JsonString ), ClassType );
		}
		catch( Exception e ) {
		}
		return null;
	}

	public <T> ArrayList<T> GetClassArrayList( Class<T> ClassType ) {
		ArrayList<T> ClassArrayList = new ArrayList<T>();
		try {
			for( int i=0; i<CurrentNodeJSONArray.length(); i++ ) {
				ClassArrayList.add( new Gson().fromJson( CurrentNodeJSONArray.getJSONObject( i ).toString( 0 ), ClassType ) );
			}
		}
		catch( Exception e ) {
		}
		return ClassArrayList;
	}

	public int GetArrayLength() {
		if( CurrentNodeJSONArray == null ) return 0;
		else return CurrentNodeJSONArray.length();
	}
}
