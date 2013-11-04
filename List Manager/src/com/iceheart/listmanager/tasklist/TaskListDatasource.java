package com.iceheart.listmanager.tasklist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.iceheart.listmanager.task.TaskSQLHelper;
import com.iceheart.listmanager.task.TaskStatus;

public class TaskListDatasource {

  private SQLiteDatabase database;
  private TaskSQLHelper dbHelper;

  public TaskListDatasource(Context context) {
    dbHelper = new TaskSQLHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public TaskList save( TaskList taskList ) {
    ContentValues values = new ContentValues();
    values.put(  "name", taskList.getName() );
    values.put(  "status", taskList.getStatus().name() );
    values.put(  "google_id", taskList.getGoogleId() );
    
    if ( taskList.getLastSynchroDate() == null ) {
    	taskList.setLastSynchroDate(new Date());
    }
    values.put( "last_synchro_date", taskList.getLastSynchroDate().getTime() );
    
    if ( taskList.getId() == null ) {
        database.insert(TaskSQLHelper.TABLE_TASK_LIST, null, values );
    } else {
    	database.update( "task_list", values, "id = " + taskList.getId() + "", null );
    }
    return taskList;
    
  }

  public void delete(TaskList taskList) {
	    database.delete( TaskSQLHelper.TABLE_TASK_LIST,  "id = " + taskList.getId() , null );
  }

  public List<TaskList> getAll() {
    List<TaskList> lists = new ArrayList<TaskList>();

    Cursor cursor = database.rawQuery( "select * from task_list", null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      TaskList taskList = cursorToTaskList(cursor);
      lists.add(taskList);
      cursor.moveToNext();
    }
    
    // Make sure to close the cursor
    cursor.close();
    return lists;
  }
  
  public List<TaskList> getAllActive() {
	    List<TaskList> lists = new ArrayList<TaskList>();

	    Cursor cursor = database.rawQuery( "select * from task_list where status = '"+ TaskListStatus.ACTIVE+"'", null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      TaskList list = cursorToTaskList(cursor);
	      lists.add(list);
	      cursor.moveToNext();
	    }
	    
	    // Make sure to close the cursor
	    cursor.close();
	    return lists;
	  }
  
  
  private TaskList cursorToTaskList(Cursor cursor) {
    TaskList list = new TaskList();
    list.setId(cursor.getLong(0));
    list.setName(cursor.getString(1));
    list.setGoogleId(cursor.getString(2));

    Long lastSynchroDate = cursor.getLong( 3 );
    if ( lastSynchroDate != null && lastSynchroDate != 0 ) {
        list.setLastSynchroDate( new Date( lastSynchroDate));
    }
    
    list.setStatus(TaskListStatus.valueOf(cursor.getString(4) ));
    
    
    return list;
  }


	public TaskList getById( Long id ) {
	    Cursor cursor = database.rawQuery( "select * from task_list where id = " + id + "", null);
	    
	    TaskList list = null;
	    if ( cursor.getCount() > 0 ) {
		    cursor.moveToFirst();
		    list = cursorToTaskList(cursor);
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return list;
	}

	public void calculateActiveTaskCount(TaskList list) {
	    Cursor cursor = database.rawQuery( 
	    		"select count(*) " +
	    		"from task " +
	    		"where list_id = " + list.getId() + " " +
	    		"and completed_date is null " +
	    		"and status != '"  + TaskStatus.DELETED + "'", null);
	    cursor.moveToFirst();
	    int taskCount = cursor.getInt( 0 );
	    list.setTaskCount( taskCount );
	    cursor.close();
	}
} 