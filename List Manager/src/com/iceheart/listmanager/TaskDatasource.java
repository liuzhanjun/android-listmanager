package com.iceheart.listmanager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TaskDatasource {

  private SQLiteDatabase database;
  private TaskSQLHelper dbHelper;

  public TaskDatasource(Context context) {
    dbHelper = new TaskSQLHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public Task save( Task task ) {
    ContentValues values = new ContentValues();
    values.put(  "name", task.getName() );
    
    if ( task.getDueDate() != null ) {
    	values.put( "due_date", task.getDueDate().getTime() );
  	} else {
    	values.put( "due_date", (Long)null );
  	}
    
    if ( task.getCreationDate() == null ) {
    	task.setCreationDate( new Date() );
    }
    values.put( "creation_date", task.getCreationDate().getTime() );
    
    if ( task.getLastSynchroDate() == null ) {
    	task.setLastSynchroDate(new Date());
    }
    values.put( "last_synchro_date", task.getLastSynchroDate().getTime() );
    
    
    if ( task.getCompletedDate() != null ) {
    	values.put( "completed_date", task.getCompletedDate().getTime() );
  	} else {
    	values.put( "completed_date", (Long)null );
  		
  	}
    
    if ( task.getEstimatedPrice() != null ) {
        values.put( "estimated_price", task.getEstimatedPrice().toPlainString() );
  	} else {
    	values.put( "estimated_price", (String)null );
    }
    
    if ( task.getRealPrice() != null ) {
        values.put( "final_price", task.getRealPrice().toPlainString() );
  	} else {
    	values.put( "final_price", (String)null );
    }
    
    values.put( "notes", task.getNotes() );
    values.put( "tags", task.getTagsAsString() );
    values.put( "status", task.getStatus().name() );
    
    if ( task.getId() == null ) {
        long insertId = database.insert(TaskSQLHelper.TABLE_TASK, null, values );
        task.setId( insertId );
    } else {
    	database.update( "task", values, "id = " + task.getId(), null );
    }
    return task;
    
  }

  public void delete(Task task) {
    delete( task.getId() );
  }
  
  public void delete( Long id ) {
    database.delete( TaskSQLHelper.TABLE_TASK,  "id = " + id, null );
  }

  public List<Task> getAllTasks() {
    List<Task> tasks = new ArrayList<Task>();

    Cursor cursor = database.rawQuery( "select * from task", null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Task task = cursorToTask(cursor);
      tasks.add(task);
      cursor.moveToNext();
    }
    // Make sure to close the cursor
    cursor.close();
    return tasks;
  }
  
  public List<Task> getAllActiveTasks() {
	    List<Task> tasks = new ArrayList<Task>();

	    Cursor cursor = database.rawQuery( "select * from task where status = '" + TaskStatus.ACTIVE.name() + "'", null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Task task = cursorToTask(cursor);
	      tasks.add(task);
	      cursor.moveToNext();
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return tasks;
	  }
  

  private Task cursorToTask(Cursor cursor) {
    Task task = new Task();
    task.setId(cursor.getLong(0));
    task.setName(cursor.getString( 1 ));
    
    Long dueDate = cursor.getLong( 2 );
    if ( dueDate != null && dueDate != 0 ) {
        task.setDueDate( new Date( dueDate));
    }
    
    Long completedDate = cursor.getLong( 3 );
    if ( completedDate != null && completedDate != 0 ) {
        task.setCompletedDate( new Date( completedDate));
    }
    
    Long lastSynchroDate = cursor.getLong( 4 );
    if ( lastSynchroDate != null && lastSynchroDate != 0 ) {
        task.setLastSynchroDate( new Date( lastSynchroDate));
    }
    
    String estimatedPrice = cursor.getString( 5 );
    if ( estimatedPrice != null ) {
        task.setEstimatedPrice( new BigDecimal( estimatedPrice ) );
    	
    }
    String realPrice = cursor.getString( 6 );
    if ( realPrice != null ) {
        task.setRealPrice( new BigDecimal( realPrice ) );
    	
    }
    task.setNotes(cursor.getString( 7 ));
    task.setTags( cursor.getString( 8 ) );
    task.setStatus( TaskStatus.valueOf( cursor.getString( 9 ) ));

    Long creationDate = cursor.getLong( 10 );
    if ( creationDate != null && creationDate != 0 ) {
        task.setCreationDate( new Date( creationDate));
    }

    return task;
  }


	public Task getTaskById(Long taskId ) {
	    Cursor cursor = database.rawQuery( "select * from task where id = " + taskId, null);
	
	    Task task = null;
	    if ( cursor.getCount() > 0 ) {
		    cursor.moveToFirst();
		    task = cursorToTask(cursor);
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return task;
	}
} 