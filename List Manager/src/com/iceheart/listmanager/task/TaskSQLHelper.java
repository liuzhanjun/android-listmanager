package com.iceheart.listmanager.task;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Class responsible to create and maintain the 
 * database for the List Management application.
 * 
 * @author Luc Martineau
 *
 */
public class TaskSQLHelper extends SQLiteOpenHelper {
	
	public static final String TABLE_TASK = "task";
	public static final String TABLE_TASK_LIST = "task_list";
	

	private static final String DATABASE_NAME = "listmanagement.db";
	private static final int DATABASE_VERSION = 8;

	private static final String CREATE_TASK = 
			"create table task ( " +
			" id integer primary key autoincrement, " +
			" name text, " +
			" due_date numeric , " +
			" completed_date numeric, " +
			" last_synchro_date numeric, " +			
			" estimated_price decimal(10,2), " +
			" final_price decimal(10,2), " +
			" notes text," +
			" list_id numeric," +
			" status text," +
			" creation_date numeric " +
			");";
	
	private static final String CREATE_TASK_LIST = 
			"create table task_list ( " +
	        " id integer primary key autoincrement, " +
			" name text, " +
	        " google_id text, "  +
			" last_synchro_date numeric, " +
			" status text " + 
			");";
	

      

  public TaskSQLHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(CREATE_TASK);
    database.execSQL(CREATE_TASK_LIST);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(TaskSQLHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion );
    db.execSQL("DROP TABLE IF EXISTS task"  );
    db.execSQL("DROP TABLE IF EXISTS tag"  );
    db.execSQL("DROP TABLE IF EXISTS task_list"  );
    onCreate(db);
  }

} 