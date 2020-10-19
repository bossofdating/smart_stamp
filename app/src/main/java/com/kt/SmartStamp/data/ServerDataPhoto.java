package com.kt.SmartStamp.data;

public class ServerDataPhoto {
	public int picture_idx;
	public String picture_name;
	public String mod_fl;

	public boolean IsLocalFile;

	public ServerDataPhoto(int PictureIndex, String ImagePath, boolean IsLocalFile, String ModifiedFlag ) {
		this.picture_idx = PictureIndex;
		this.picture_name = ImagePath;
		this.IsLocalFile = IsLocalFile;
		this.mod_fl = ModifiedFlag;
	}
}
