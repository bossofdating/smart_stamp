package com.kt.SmartStamp.listener;

public interface LIST_ITEM_LISTENER {
	void onItemClick(Object CallerObject, int ClickType, int Position);
	void onReachedLastItem(Object CallerObject);
}
