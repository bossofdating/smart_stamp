package com.kt.SmartStamp.utility;

public class NameValuePair {
	private String Name;
	private String Value;

	private boolean EncodeJson;

	public NameValuePair(String Name, String Value ) {
		this.Name = Name;
		this.Value = Value;
		this.EncodeJson = true;
	}

	public NameValuePair(String Name, String Value, boolean NeedEncodeJson ) {
		this.Name = Name;
		this.Value = Value;
		this.EncodeJson = NeedEncodeJson;
	}

	public String getName() {
		return Name;
	}

	public String getValue() {
		return Value;
	}

	public boolean NeedEncodeJson() {
		return EncodeJson;
	}
}
