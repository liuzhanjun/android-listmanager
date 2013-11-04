package com.iceheart.listmanager.googlesync;

import com.iceheart.listmanager.ApplicationSettings;

import android.content.SharedPreferences;

/**
 *  Bean to store the google account information.
 *  
 * @author Luc Martineau
 *
 */
public class GoogleAccountInfo {
	
	private String username;
	private String password;
	private String spreadsheetFeed;
	
	public GoogleAccountInfo( SharedPreferences sharedPreferences ) {
		username = sharedPreferences.getString( ApplicationSettings.GOOGLE_ACCOUNT, "" );
		password = sharedPreferences.getString( ApplicationSettings.GOOGLE_PASSWORD, "" );
		setSpreadsheetFeed(sharedPreferences.getString( ApplicationSettings.GOOGLE_LIST_FEED, "" ));
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}


	public void saveToPreferences(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ApplicationSettings.GOOGLE_LIST_FEED, getSpreadsheetFeed() );
        editor.putString(ApplicationSettings.GOOGLE_ACCOUNT, getUsername() );
        editor.putString(ApplicationSettings.GOOGLE_PASSWORD, getPassword() );
        editor.apply();
        editor.commit();
	}

	public String getSpreadsheetFeed() {
		return spreadsheetFeed;
	}

	public void setSpreadsheetFeed(String spreadsheetFeed) {
		this.spreadsheetFeed = spreadsheetFeed;
	}
	

}
