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

  public TaskList save( TaskList tag ) {
    ContentValues values = new ContentValues();
    values.put(  "name", tag.getName() );
    values.put(  "status", tag.getStatus().name() );
    
    if ( tag.getLastSynchroDate() == null ) {
    	tag.setLastSynchroDate(new Date());
    }
    values.put( "last_synchro_date", tag.getLastSynchroDate().getTime() );
    
    TaskList  persistedTag = getTagByName( tag.getName() );
    if ( persistedTag == null ) {
        database.insert(TaskSQLHelper.TABLE_TAG, null, values );
    } else {
    	database.update( "tag", values, "name = '" + tag.getName() + "'", null );
    }
    return tag;
    
  }

  public void delete(TaskList tag) {
	    database.delete( TaskSQLHelper.TABLE_TAG,  "name = '" + tag.getName() + "'", null );
  }

  public List<TaskList> getAllTags() {
    List<TaskList> tags = new ArrayList<TaskList>();

    Cursor cursor = database.rawQuery( "select * from tag", null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      TaskList tag = cursorToTag(cursor);
      tags.add(tag);
      cursor.moveToNext();
    }
    
    // Make sure to close the cursor
    cursor.close();
    return tags;
  }
  
  public List<TaskList> getAllActiveTags() {
	    List<TaskList> tags = new ArrayList<TaskList>();

	    Cursor cursor = database.rawQuery( "select * from tag where status = '"+ TaskListStatus.ACTIVE+"'", null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      TaskList tag = cursorToTag(cursor);
	      tags.add(tag);
	      cursor.moveToNext();
	    }
	    
	    // Make sure to close the cursor
	    cursor.close();
	    return tags;
	  }
  
  
  private TaskList cursorToTag(Cursor cursor) {
    TaskList tag = new TaskList();
    tag.setName(cursor.getString(0));

    Long lastSynchroDate = cursor.getLong( 1 );
    if ( lastSynchroDate != null && lastSynchroDate != 0 ) {
        tag.setLastSynchroDate( new Date( lastSynchroDate));
    }
    
    tag.setStatus(TaskListStatus.valueOf(cursor.getString(2) ));
    
    
    return tag;
  }


	public TaskList getTagByName(String name ) {
	    Cursor cursor = database.rawQuery( "select * from tag where name = '" + name + "'", null);
	    
	    TaskList tag = null;
	    if ( cursor.getCount() > 0 ) {
		    cursor.moveToFirst();
		    tag = cursorToTag(cursor);
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return tag;
	}

	public void calculateActiveTaskCount(TaskList tag) {
	    Cursor cursor = database.rawQuery( 
	    		"select count(*) " +
	    		"from task " +
	    		"where tags like '%" + tag.getName() + "%' " +
	    		"and completed_date is null " +
	    		"and status != '"  + TaskStatus.DELETED + "'", null);
	    cursor.moveToFirst();
	    int taskCount = cursor.getInt( 0 );
	    tag.setTaskCount( taskCount );
	    cursor.close();
	}
} 