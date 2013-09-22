package com.iceheart.listmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		/*
		 * Load the information from the SharedPreferences.
		 */
		SharedPreferences sharedPreferences = getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		String googleAccount = sharedPreferences.getString( ApplicationSettings.GOOGLE_ACCOUNT,  "" );
		EditText googleAccountText = (EditText) findViewById(R.id.txtGoogleAccount);
		googleAccountText.setText( googleAccount );
		googleAccountText.requestFocus();
		String googlePassword = sharedPreferences.getString( ApplicationSettings.GOOGLE_PASSWORD,  "" );
		EditText googlePasswordText = (EditText) findViewById(R.id.txtPassword);
		googlePasswordText.setText( googlePassword );
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	public void saveSettings( View view ) {
		
		if ( !validate() ) {
			return;
		}
		
		
		SharedPreferences sharedPreferences = getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		EditText googleAccountText = (EditText) findViewById(R.id.txtGoogleAccount);
		String googleAccount =  googleAccountText.getText().toString();
		EditText passwordText = (EditText) findViewById(R.id.txtPassword);
		String googlePassword =  passwordText.getText().toString();
		
        editor.putString( ApplicationSettings.GOOGLE_ACCOUNT, googleAccount );
        editor.putString( ApplicationSettings.GOOGLE_PASSWORD, googlePassword );
        editor.apply();
        editor.commit();

		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent );
	}
	
	private void showErrorDialog(String message) {
		new AlertDialog.Builder(this).setTitle("Error").setMessage(message).
	    setNeutralButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {}
	     }).show();		
	}
		
	public void cancelSettings( View view ) {
		
		if ( !validate() ) {
			return;
		}
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent );
	}

	/**
	 * Make sure the user does not leave the page before setting the essential information.
	 * 
	 * @return false if the validation failed.
	 */
	private boolean validate() {
		EditText googleAccountText = (EditText) findViewById(R.id.txtGoogleAccount);
		String googleAccount =  googleAccountText.getText().toString();
		if ( googleAccount == null || googleAccount.isEmpty() ) {
			showErrorDialog( "Vous devez saisir votre google account");
			return false;
		}
			
		EditText passwordText = (EditText) findViewById(R.id.txtPassword);
		String googlePassword =  passwordText.getText().toString();
		if ( googlePassword == null || googlePassword.isEmpty() ) {
			showErrorDialog( "Vous devez saisir votre google password");
			return false;
		}
		return true;
	}
	

}
