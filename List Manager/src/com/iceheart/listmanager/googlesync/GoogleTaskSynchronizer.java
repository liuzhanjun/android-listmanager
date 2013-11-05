package com.iceheart.listmanager.googlesync;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.iceheart.listmanager.ApplicationSettings;
import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;
import com.iceheart.listmanager.task.Task;
import com.iceheart.listmanager.task.TaskDatasource;
import com.iceheart.listmanager.task.TaskStatus;
import com.iceheart.listmanager.tasklist.TaskList;
import com.iceheart.listmanager.tasklist.TaskListCache;
import com.iceheart.listmanager.tasklist.TaskListDatasource;
import com.iceheart.listmanager.tasklist.TaskListStatus;

/**
 * Class responsible to synchronize the task list with the google spreadsheet.
 * This operation is done in background.
 * @author Luc Martineau
 *
 */
public class GoogleTaskSynchronizer extends AsyncTask<Context, Object, Boolean> {
	
	// TODO: Revise for performance improvment (i.e. check last update date for the task and the spread sheet ?)
	
	private ProgressDialog progressDialog;
	private MainActivity taskListActivity;
	private String lastException;
	private SpreadsheetService spreadsheetService;
	private long lastSynchronisation;
	

    public GoogleTaskSynchronizer( MainActivity activity ) {
		this.taskListActivity = activity;
		progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressNumberFormat(null);
	}
	
	/**
	 * @Return a boolean indicating if the synchronization has been completed or not.
	 */
	@Override
	protected Boolean doInBackground(Context... params) {
		
		Context context = params[0];
		
		if ( !haveNetworkConnection() ) {
			lastException = "No Network connection";
			return Boolean.FALSE;
		}
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		lastSynchronisation = sharedPreferences.getLong( ApplicationSettings.LAST_SYNCHRONIZATION, -1 );
		
		
		try {
			synchronizeTaskLists( context );
			TaskListCache.getInstance( taskListActivity ).refreshCache();
			synchronizeTasks(context );
		} catch ( Exception e ) {
			lastException = e.getMessage();
			return false;
		}
		
		/*
		 * Update the synchronization date in the shared preferences.
		 */
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(ApplicationSettings.LAST_SYNCHRONIZATION, System.currentTimeMillis() );
        editor.apply();
        editor.commit();
		
		return Boolean.TRUE;

	}
	
	private SpreadsheetService getSpreadsheetService() {
		
		if ( spreadsheetService == null ) {
			SharedPreferences sharedPreferences = taskListActivity.getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
			GoogleAccountInfo googleAccountInfo = new GoogleAccountInfo( sharedPreferences );
	        SpreadsheetService service = new SpreadsheetService("Testing");
	       	try {
				service.setUserCredentials(googleAccountInfo.getUsername(),  googleAccountInfo.getPassword() );
			} catch (AuthenticationException e) {
				throw new RuntimeException( "Unable to get the spreadshet from google",  e );
			}
	       	spreadsheetService = service;
		}
       	return spreadsheetService;
		
	}
	
	private boolean haveNetworkConnection() {
		ConnectivityManager cm = (ConnectivityManager) taskListActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    for (NetworkInfo ni : cm.getAllNetworkInfo()) {
	        if ( (ni.getTypeName().equalsIgnoreCase("WIFI") || ni.getTypeName().equalsIgnoreCase("MOBILE") ) && ni.isConnected() )
                return true;
	    }
	    return false;
	}	

	private void synchronizeTaskLists(Context context ) throws Exception {
        publishProgress( "Synchronizing Tasks List", null, -1 );

		TaskListDatasource ds = new TaskListDatasource( context );
		ds.open();
		List<TaskList> localLists = ds.getAll();
		Map<String, TaskList> taskListMap = new HashMap<String,TaskList>();
		for ( TaskList localList: localLists ) {
			taskListMap.put( localList.getGoogleId(), localList );
		}

        publishProgress("Reading task lists from Google Drive");

        SpreadsheetEntry spreadsheet = openSpreadsheet( context );
        List<WorksheetEntry> worksheets = new ArrayList<WorksheetEntry>((spreadsheet.getWorksheets() ));
        publishProgress( "Processing Google Spreadsheet Task lists", 0, worksheets.size() );

        int count = 1;
        for ( int i = worksheets.size() - 1; i >= 0; i-- ) {
            publishProgress( null, count++ );
        	
        	WorksheetEntry worksheet = worksheets.get( i );
        	TaskList localTaskList = taskListMap.remove( worksheet.getId() );
        	if ( localTaskList == null ) {
        		localTaskList = new TaskList( worksheet.getTitle().getPlainText() );
        		localTaskList.setGoogleId( worksheet.getId() );
        		localTaskList = ds.save( localTaskList );
			} else if ( localTaskList.getStatus() == TaskListStatus.DELETED ) {
				try {
					worksheet.delete();
				} catch (Exception e) {
					throw new RuntimeException( "Unable to delete tab from spreadsheet: " + worksheet.getTitle().getPlainText() + " (" + e.getMessage() + ")" );
				}
				worksheets.remove( i );
				ds.delete( localTaskList );
			} else if ( localTaskList.getLastSynchroDate() == null || localTaskList.getLastSynchroDate().getTime() <= lastSynchronisation ) {
				if ( !localTaskList.getName().equals( worksheet.getTitle().getPlainText() ) ) {
					localTaskList.setName( worksheet.getTitle().getPlainText() );
					localTaskList = ds.save( localTaskList );
				}
			} else {
				worksheet.setTitle( new PlainTextConstruct( localTaskList.getName() ));
				worksheet.update();
			}

		}

        Collection<TaskList> newTaskLists = taskListMap.values();
        if (newTaskLists.size() > 0 ) {
            publishProgress( "Synchronizing Local Task List Changes", 0, newTaskLists.size() );
        }

        count = 0;
		for ( TaskList localList: newTaskLists ) {
            count++;
            publishProgress( null, count );

			/*
			 * Determine if this task has been DELETED from the spreadsheet after the last synchronization
			 * OR if it is a new task that has been added since the last synchronization.
			 */
			if ( localList.getLastSynchroDate() == null || localList.getLastSynchroDate().getTime() <= lastSynchronisation || localList.getStatus() == TaskListStatus.DELETED ) {
				ds.delete( localList );
			} else {
				WorksheetEntry entry = new WorksheetEntry( 10, 10);
				
				entry.setTitle( new PlainTextConstruct( localList.getName() ));
				URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
	            try {
	            	
					WorksheetEntry insert = getSpreadsheetService().insert(worksheetFeedUrl, entry);
					
					URL cellFeedUrl = insert.getCellFeedUrl();
                	CellFeed cellFeed = getSpreadsheetService().getFeed(cellFeedUrl, CellFeed.class);
                    CellEntry cellEntry = new CellEntry (1, 1, "Task name");
                	cellFeed.insert (cellEntry);
                    cellEntry = new CellEntry (1, 2, "Due Date");
                	cellFeed.insert (cellEntry);
                    cellEntry = new CellEntry (1, 3, "Estimated Price");
                	cellFeed.insert (cellEntry);
                    cellEntry = new CellEntry (1, 4, "Notes");
                	cellFeed.insert (cellEntry);
                	cellEntry = new CellEntry (1, 5, "Completed Date");
                	cellFeed.insert (cellEntry);                	
                	cellEntry = new CellEntry (1, 6, "Real Price");
                	cellFeed.insert (cellEntry);                	
                	cellEntry = new CellEntry (1, 7, "Creation Date");
                	cellFeed.insert (cellEntry);                	
                	cellEntry = new CellEntry (1, 8, "id");
                	cellFeed.insert (cellEntry);                	
                	
					localList.setGoogleId( insert.getId() );
					ds.save(localList );
				} catch (Exception e) {
					throw new RuntimeException( "Unable to insert the new tab for task list: " + localList.getName() + " (" +e.getMessage() + ")" );
				}
			}
		}
		ds.close();		
	}

	/**
	 * Synchronize the task list with the Google spreadsheet.
	 * 
	 * @param context The context. (Activity)
	 */
	private void synchronizeTasks(Context context ) throws Exception {
        publishProgress( "Synchronizing Tasks", null, -1 );

        SpreadsheetEntry spreadsheet = openSpreadsheet(context);
        
        for ( WorksheetEntry worksheet: spreadsheet.getWorksheets() ) {
        	String listName = worksheet.getTitle().getPlainText();
        	TaskList list = TaskListCache.getInstance().getByName( listName );
        	synchronizeTasksForList( list, getSpreadsheetService().getFeed( worksheet.getListFeedUrl(), ListFeed.class) );
        }
	}
	
	/**
	 * Synchronize the tasks for a specific task list. (Each task list is stored in its own
	 * worksheet inside the google spreadsheet.
	 * 
	 * @param list The task list.
	 * @param listFeed the List Feed to connect to.
	 */
	private void synchronizeTasksForList( TaskList list, ListFeed listFeed ) {
		
		TaskDatasource ds = new TaskDatasource( taskListActivity );
		ds.open();
		
		/*
		 * First, load a map of the local tasks, existing in the database.
		 */
		List<Task> localTasks = ds.getAllTasksForList( list.getId() );
		Map<Long, Task> taskMap = new HashMap<Long,Task>();
		for ( Task localTask: localTasks ) {
			taskMap.put( localTask.getId(), localTask );
		}

		List<ListEntry> googleEntries = listFeed.getEntries();
		
        if ( googleEntries.size() > 0 ) {
            publishProgress( "Processing Google Spreadsheet Tasks (" + list.getName() +")", 0, googleEntries.size() );
        }

        int count = 0;
		for ( ListEntry taskEntry: googleEntries ) {

            count++;
            publishProgress( null, count );

			Task googleTask = listEntryToTask( taskEntry, list );
			
			Task localTask = taskMap.remove( googleTask.getId()  );
			
			if ( localTask == null ) {
				googleTask.setId( null );
				googleTask = ds.save( googleTask );
				updateTaskEntry(taskEntry, googleTask);
			} else if ( localTask.getStatus() == TaskStatus.DELETED ) {
				try {
					taskEntry.delete();
				} catch (Exception e) {
					throw new RuntimeException( "Unable to delete task: " + taskEntry.getId() + "(" + e.getMessage() + ")" );
				}
				ds.delete( localTask );
			} else if ( localTask.getLastSynchroDate() == null || localTask.getLastSynchroDate().getTime() <= lastSynchronisation ) {
				if ( !localTask.equals( googleTask ) ) {
					googleTask.setId( localTask.getId() );
					googleTask = ds.save( googleTask );
				}
			} else {
				updateTaskEntry(taskEntry, localTask);
			}

		}

        Collection<Task> taskValues = taskMap.values();
        if (taskValues.size() > 0 ) {
            publishProgress( "Synchronizing Local Task Changes", 0, taskValues.size() );
        }

        count = 0;
		for ( Task localTask: taskValues ) {
			count++;
            publishProgress( null, count );
			/*
			 * Determine if this task has been DELETED from the spreadsheet after the last synchronization
			 * OR if it is a new task that has been added since the last synchronization.
			 */
			if ( localTask.getLastSynchroDate() == null || localTask.getLastSynchroDate().getTime() <= lastSynchronisation ) {
				ds.delete( localTask );
			} else {
				ListEntry entry = listFeed.createEntry();
				updateTaskEntry( entry, localTask );
		            try {
						listFeed.insert( entry );
					} catch (Exception e) {
						throw new RuntimeException( "unable to insert task '" + localTask.getName() + "' to the google Spreadsheet ( " + e.getMessage() + ")" );
					}
				
			}
		}
		ds.close();

	}

    /**
	 * Update the spreadsheet entry with the task information.
	 * @param entry The Spreadsheet entry to update.
	 * @param localTask The task information 
	 */
	private void updateTaskEntry(ListEntry entry, Task localTask) {
		entry.getCustomElements().setValueLocal("taskname", localTask.getName() );
		entry.getCustomElements().setValueLocal("completeddate", localTask.getCompletedDate() == null ? "": Task.DATE_FORMAT.format( localTask.getCompletedDate() ) );
		entry.getCustomElements().setValueLocal("creationdate", localTask.getCreationDate() == null ? "": Task.DATETIME_FORMAT.format( localTask.getCreationDate() ) );
		entry.getCustomElements().setValueLocal("duedate", localTask.getDueDate() == null ? "": Task.DATE_FORMAT.format( localTask.getDueDate() ) );
		entry.getCustomElements().setValueLocal("estimatedprice", localTask.getEstimatedPrice() == null ? "": localTask.getEstimatedPrice().toPlainString() );
		entry.getCustomElements().setValueLocal("notes", localTask.getNotes() == null ? "": localTask.getNotes() );
		entry.getCustomElements().setValueLocal("reaprice", localTask.getRealPrice() == null ? "": localTask.getRealPrice().toPlainString() );
		entry.getCustomElements().setValueLocal("id", String.valueOf( localTask.getId() ) );
		try {
			entry.update();
		} catch (Exception e) {
		}
		
	}
	
	private Task listEntryToTask( ListEntry entry, TaskList taskList ) {
    	Task task = new Task();
    	task.setListId( taskList.getId() );
    	task.setName(entry.getCustomElements().getValue( "taskname" ));
		task.setCompletedDate( entry.getCustomElements().getValue( "completeddate") );
		task.setCreationDate( entry.getCustomElements().getValue( "creationDate") );
		task.setDueDate( entry.getCustomElements().getValue( "duedate") );
		task.setEstimatedPrice( entry.getCustomElements().getValue( "estimatedprice") );
		task.setNotes(entry.getCustomElements().getValue( "notes") );
		task.setRealPrice( entry.getCustomElements().getValue( "realPrice")  );
    	String taskId = entry.getCustomElements().getValue( "id" );
    	if ( taskId != null && !taskId.isEmpty() ) {
        	task.setId( Long.parseLong( taskId ) );
    	}
    	return task;
		
	}
	
	private SpreadsheetEntry openSpreadsheet( Context context ) {
		
        SpreadsheetEntry spreadsheet = null;

        try {
			URL SPREADSHEET_FEED_URL = new URL( "https://spreadsheets.google.com/feeds/spreadsheets/private/full");
	
	        // Make a request to the API and get all spreadsheets.
	        SpreadsheetFeed feed = getSpreadsheetService().getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
	        List<SpreadsheetEntry> spreadsheets = feed.getEntries();
	
	        for ( SpreadsheetEntry ss: spreadsheets ) {
	        	if ( ss.getTitle().getPlainText().equals( "todo") ) {
	        		spreadsheet = ss;
	        		break;
	        	}
	        }
        } catch ( Exception e ) {
        	throw new RuntimeException( "Unable to read the spreadsheet from google account: " + e.getMessage() );
        }
        
        
        /*
         * 'todo' spreadsheet not found on the google drive.
         */
        if ( spreadsheet == null ) {
        	throw new RuntimeException( "Google Spreadsheet not found. You need to create a spreadsheet called 'todo' in your Google Drive." );
        	
        	// TODO: Add a new spreadsheet on the google drive.
//        	 com.google.api.services.drive.model.File  file = new com.google.api.services.drive.model.File();
//        	  file.setTitle("test");       
//        	  file.setMimeType("application/vnd.google-apps.spreadsheet");
//        	  Insert insert = this.drive.files().insert(file);
//        	  file = insert.execute();
        	
//        	  SpreadsheetService s = googleConn.getSpreadSheetService();
//        	  String spreadsheetURL = "https://spreadsheets.google.com/feeds/spreadsheets/" + file.getId();
//        	  SpreadsheetEntry spreadsheet = s.getEntry(new URL(spreadsheetURL), SpreadsheetEntry.class);

//        	  WorksheetFeed worksheetFeed = s.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
//        	  List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
//        	  WorksheetEntry worksheet = worksheets.get(0);

        }
        return spreadsheet;
	}

	@Override
	protected void onPreExecute() {
		progressDialog.setTitle( "Synchronization");
		progressDialog.setMessage( "Synchronizing with google drive...");
		progressDialog.show();

	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		progressDialog.dismiss();
		taskListActivity.refreshTaskList();
		
		if ( !result ) {
			 AlertDialog.Builder builder = new AlertDialog.Builder(taskListActivity );
       	  	 builder.setTitle(R.string.synchronization_failed);
       	  	 builder.setMessage( lastException );
       	  	 AlertDialog dlg= builder.create();
       	  	 dlg.show();
		}
	}

    @Override
    protected void onProgressUpdate(Object... values) {

        if ( values.length > 0 && values[0] != null ) {
            progressDialog.setMessage( (String)values[0] );
        }
        if ( values.length > 1 && values[1] != null ) {
            progressDialog.setProgress( (Integer)values[1]);
        }
        if ( values.length > 2 && values[2] != null ) {
            int max = (Integer)values[2];

            if ( max == -1 ) {
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressNumberFormat(null);
            } else {
                progressDialog.setIndeterminate(false);
                progressDialog.setMax( max );
                progressDialog.setProgressNumberFormat("%1d/%2d");
            }
        }

    }
}
