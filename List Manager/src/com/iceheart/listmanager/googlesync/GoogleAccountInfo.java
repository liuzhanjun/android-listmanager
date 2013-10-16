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
	private String taskListFeed;
	private String tagsFeed;
	
	public GoogleAccountInfo( SharedPreferences sharedPreferences ) {
		username = sharedPreferences.getString( ApplicationSettings.GOOGLE_ACCOUNT, "" );
		password = sharedPreferences.getString( ApplicationSettings.GOOGLE_PASSWORD, "" );
		taskListFeed = sharedPreferences.getString( ApplicationSettings.GOOGLE_LIST_FEED, "" );
		tagsFeed = sharedPreferences.getString( ApplicationSettings.GOOGLE_TAGS_FEED, "" );		
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
	public String getTaskListFeed() {
		return taskListFeed;
	}
	public void setTaskListFeed(String taskListFeed) {
		this.taskListFeed = taskListFeed;
	}
	public String getTagsFeed() {
		return tagsFeed;
	}
	public void setTagsFeed(String tagsFeed) {
		this.tagsFeed = tagsFeed;
	}

	public void saveToPreferences(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ApplicationSettings.GOOGLE_LIST_FEED, getTaskListFeed() );
        editor.putString(ApplicationSettings.GOOGLE_TAGS_FEED, getTagsFeed() );
        editor.putString(ApplicationSettings.GOOGLE_ACCOUNT, getUsername() );
        editor.putString(ApplicationSettings.GOOGLE_PASSWORD, getPassword() );
        editor.apply();
        editor.commit();
	}
	

}
