package com.iceheart.listmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TagDatasource {

  private SQLiteDatabase database;
  private TaskSQLHelper dbHelper;

  public TagDatasource(Context context) {
    dbHelper = new TaskSQLHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public Tag save( Tag tag ) {
    ContentValues values = new ContentValues();
    values.put(  "name", tag.getName() );
    values.put(  "status", tag.getStatus().name() );
    
    if ( tag.getLastSynchroDate() == null ) {
    	tag.setLastSynchroDate(new Date());
    }
    values.put( "last_synchro_date", tag.getLastSynchroDate().getTime() );
    
    Tag  persistedTag = getTagByName( tag.getName() );
    if ( persistedTag == null ) {
        database.insert(TaskSQLHelper.TABLE_TAG, null, values );
    } else {
    	database.update( "tag", values, "name = '" + tag.getName() + "'", null );
    }
    return tag;
    
  }

  public void delete(Tag tag) {
	    database.delete( TaskSQLHelper.TABLE_TAG,  "name = '" + tag.getName() + "'", null );
  }

  public List<Tag> getAllTags() {
    List<Tag> tags = new ArrayList<Tag>();

    Cursor cursor = database.rawQuery( "select * from tag", null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Tag tag = cursorToTag(cursor);
      tags.add(tag);
      cursor.moveToNext();
    }
    
    // Make sure to close the cursor
    cursor.close();
    return tags;
  }
  
  public List<Tag> getAllActiveTags() {
	    List<Tag> tags = new ArrayList<Tag>();

	    Cursor cursor = database.rawQuery( "select * from tag where status = '"+ TagStatus.ACTIVE+"'", null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Tag tag = cursorToTag(cursor);
	      tags.add(tag);
	      cursor.moveToNext();
	    }
	    
	    // Make sure to close the cursor
	    cursor.close();
	    return tags;
	  }
  
  
  private Tag cursorToTag(Cursor cursor) {
    Tag tag = new Tag();
    tag.setName(cursor.getString(0));

    Long lastSynchroDate = cursor.getLong( 1 );
    if ( lastSynchroDate != null && lastSynchroDate != 0 ) {
        tag.setLastSynchroDate( new Date( lastSynchroDate));
    }
    
    tag.setStatus(TagStatus.valueOf(cursor.getString(2) ));
    
    
    return tag;
  }


	public Tag getTagByName(String name ) {
	    Cursor cursor = database.rawQuery( "select * from tag where name = '" + name + "'", null);
	    
	    Tag tag = null;
	    if ( cursor.getCount() > 0 ) {
		    cursor.moveToFirst();
		    tag = cursorToTag(cursor);
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return tag;
	}

	public void calculateActiveTaskCount(Tag tag) {
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