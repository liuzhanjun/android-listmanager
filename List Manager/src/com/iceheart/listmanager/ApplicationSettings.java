package com.iceheart.listmanager;

public interface ApplicationSettings {
	
	/*
	 * The setting group (In the sharedPreference)
	 */
	String SETTINGS_LIST = "listManager";
	
	/**
	 * The google account information (username and password)
	 */
	String GOOGLE_ACCOUNT = "googleAccount";
	String GOOGLE_PASSWORD = "googlePassword";
	
	/**
	 * The google spreadsheet feed cached.
	 * For performance.
	 */
	String GOOGLE_LIST_FEED = "googleListFeed";
	String GOOGLE_TAGS_FEED = "googleTagsFeed";
	
	/**
	 * The timestamp of the last synchronization (google)
	 */
	String LAST_SYNCHRONIZATION = "lastSynchronization";
	
	/**
	 * Setting to force a synchronization at startup.
	 */
	String SYNC_ON_STARTUP = "syncOnStartup"; 

}
