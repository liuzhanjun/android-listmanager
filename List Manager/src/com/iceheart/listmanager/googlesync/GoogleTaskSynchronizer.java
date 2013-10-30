package com.iceheart.listmanager.googlesync;

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
import com.iceheart.listmanager.ApplicationSettings;
import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;
import com.iceheart.listmanager.tag.Tag;
import com.iceheart.listmanager.tag.TagDatasource;
import com.iceheart.listmanager.tag.TagStatus;
import com.iceheart.listmanager.task.Task;
import com.iceheart.listmanager.task.TaskDatasource;
import com.iceheart.listmanager.task.TaskStatus;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible to synchronize the task list with the google spreadsheet.
 * This operation is done in background.
 * @author Luc Martineau
 *
 */
public class GoogleTaskSynchronizer extends AsyncTask<Context, Object, Boolean> {
	
	private ProgressDialog progressDialog;
	private MainActivity taskListActivity;
	private String lastException;

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
		long lastSynchronisation = sharedPreferences.getLong( ApplicationSettings.LAST_SYNCHRONIZATION, -1 );
		
		try {
		
			synchronizeTaskList(context, lastSynchronisation);
			synchronizeTags( context, lastSynchronisation );
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
	
	private boolean haveNetworkConnection() {
		ConnectivityManager cm = (ConnectivityManager) taskListActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    for (NetworkInfo ni : cm.getAllNetworkInfo()) {
	        if ( (ni.getTypeName().equalsIgnoreCase("WIFI") || ni.getTypeName().equalsIgnoreCase("MOBILE") ) && ni.isConnected() )
                return true;
	    }
	    return false;
	}	

	private void synchronizeTags(Context context, long lastSynchronisation) {
        publishProgress( "Synchronizing Tags List", null, -1 );

		TagDatasource ds = new TagDatasource( context );
		ds.open();
		List<Tag> localTags = ds.getAllTags();
		Map<String, Tag> tagMap = new HashMap<String,Tag>();
		for ( Tag localTag: localTags ) {
			tagMap.put( localTag.getName(), localTag );
		}

        publishProgress("Reading tags from Google Drive");

		ListFeed tagsFromGoogle = readTagsGoogleSpreadsheet(context);
        List<ListEntry> googleEntries = tagsFromGoogle.getEntries();
        if ( googleEntries.size() > 0 ) {
            publishProgress( "Processing Google Spreadsheet Tags", 0, googleEntries.size() );
        }

        int count = 0;
		for ( ListEntry tagEntry: tagsFromGoogle.getEntries() ) {
            count++;
            publishProgress( null, count );

			Tag googleTag = listEntryToTag( tagEntry );
			
			Tag localTag = tagMap.remove( googleTag.getName()  );
			
			if ( localTag == null ) {
				googleTag = ds.save( googleTag );
				updateTagEntry(tagEntry, googleTag);
			} else if ( localTag.getStatus() == TagStatus.DELETED ) {
				try {
					tagEntry.delete();
				} catch (Exception e) {
					throw new RuntimeException( "Unable to delete task: " + tagEntry.getId() + " (" + e.getMessage() + ")" );
				}
				ds.delete( localTag );
			} else if ( localTag.getLastSynchroDate() == null || localTag.getLastSynchroDate().getTime() <= lastSynchronisation ) {
				if ( !localTag.equals( googleTag ) ) {
					googleTag = ds.save( googleTag );
				}
			} else {
				updateTagEntry(tagEntry, localTag);
			}
		}

        Collection<Tag> tagValues = tagMap.values();
        if (tagValues.size() > 0 ) {
            publishProgress( "Synchronizing Local Tag Changes", 0, tagValues.size() );
        }

        count = 0;
		for ( Tag localTask: tagValues ) {
            count++;
            publishProgress( null, count );

			/*
			 * Determine if this task has been DELETED from the spreadsheet after the last synchronization
			 * OR if it is a new task that has been added since the last synchronization.
			 */
			if ( localTask.getLastSynchroDate() == null || localTask.getLastSynchroDate().getTime() <= lastSynchronisation ) {
				ds.delete( localTask );
			} else {
				ListEntry entry = tagsFromGoogle.createEntry();
				updateTagEntry( entry, localTask );
		            try {
						tagsFromGoogle.insert( entry );
					} catch (Exception e) {
						throw new RuntimeException( "Unable to insert task '" + localTask.getName() + "' in the google spreadsheet (" + e.getMessage() + ")" );
					}
				
			}
		}
		ds.close();		
	}

	/**
	 * Synchronize the task list with the Google spreadsheet.
	 * 
	 * @param context The context. (Activity)
	 * @param lastSynchronisation The last synchronization timestamp.
	 */
	private void synchronizeTaskList(Context context, long lastSynchronisation) {
        publishProgress( "Synchronizing Task List", null, -1 );

		TaskDatasource ds = new TaskDatasource( context);
		ds.open();
		List<Task> localTasks = ds.getAllTasks();
		Map<Long, Task> taskMap = new HashMap<Long,Task>();
		for ( Task localTask: localTasks ) {
			taskMap.put( localTask.getId(), localTask );
		}

        publishProgress("Reading tasks from Google Spreadsheet");

        ListFeed tasksFromGoogle = readTasksGoogleSpreadsheet(context);

        List<ListEntry> googleEntries = tasksFromGoogle.getEntries();
        if ( googleEntries.size() > 0 ) {
            publishProgress( "Processing Google Spreadsheet Tasks", 0, googleEntries.size() );
        }

        int count = 0;
		for ( ListEntry taskEntry: googleEntries ) {

            count++;
            publishProgress( null, count );

			Task googleTask = listEntryToTask( taskEntry );
			
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
				ListEntry entry = tasksFromGoogle.createEntry();
				updateTaskEntry( entry, localTask );
		            try {
						tasksFromGoogle.insert( entry );
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
		entry.getCustomElements().setValueLocal("tags", localTask.getTagsAsString() );
		entry.getCustomElements().setValueLocal("id", String.valueOf( localTask.getId() ) );
		try {
			entry.update();
		} catch (Exception e) {
		}
		
	}
	
	private void updateTagEntry(ListEntry entry, Tag localTag) {
		entry.getCustomElements().setValueLocal("name", localTag.getName() );
		try {
			entry.update();
		} catch (Exception e) {
		}
		
	}	

	private Task listEntryToTask( ListEntry entry ) {
    	Task task = new Task();
    	task.setName(entry.getCustomElements().getValue( "taskname" ));
		task.setCompletedDate( entry.getCustomElements().getValue( "completeddate") );
		task.setCreationDate( entry.getCustomElements().getValue( "creationDate") );
		task.setDueDate( entry.getCustomElements().getValue( "duedate") );
		task.setEstimatedPrice( entry.getCustomElements().getValue( "estimatedprice") );
		task.setNotes(entry.getCustomElements().getValue( "notes") );
		task.setRealPrice( entry.getCustomElements().getValue( "realPrice")  );
    	task.setTags( entry.getCustomElements().getValue( "tags" ));
    	String taskId = entry.getCustomElements().getValue( "id" );
    	if ( taskId != null && !taskId.isEmpty() ) {
        	task.setId( Long.parseLong( taskId ) );
    		
    	}
    	return task;
		
	}
	
	private Tag listEntryToTag( ListEntry entry ) {
    	Tag tag = new Tag();
    	tag.setName(entry.getCustomElements().getValue( "name" ));
    	return tag;
		
	}
	
	
	protected ListFeed readTasksGoogleSpreadsheet( Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		GoogleAccountInfo googleAccountInfo = new GoogleAccountInfo( sharedPreferences );

		try {
            SpreadsheetService service = new SpreadsheetService("Testing");
            service.setUserCredentials(googleAccountInfo.getUsername(),  googleAccountInfo.getPassword() );
            
            URL listFeedURL = null;
            
            if ( googleAccountInfo.getTaskListFeed() == null || googleAccountInfo.getTaskListFeed().isEmpty() ) {
            	SpreadsheetEntry spreadsheet = openSpreadsheet( service );
            	
                WorksheetEntry worksheet = spreadsheet.getWorksheets().get( 0 );
                listFeedURL = worksheet.getListFeedUrl();
                
                googleAccountInfo.setTaskListFeed( listFeedURL.toString() );
                googleAccountInfo.saveToPreferences( sharedPreferences );
            } else {
            	listFeedURL = new URL( googleAccountInfo.getTaskListFeed() );
            }

            return service.getFeed( listFeedURL, ListFeed.class );
        } catch (Exception e) {
        	/*
        	 * Reset the List Feed caching, just in case it was the cause of the exception.
        	 */
            googleAccountInfo.setTaskListFeed( null );
            googleAccountInfo.saveToPreferences( sharedPreferences );
        	throw new RuntimeException ( e );
        }		
	}
	
	private SpreadsheetEntry openSpreadsheet( SpreadsheetService service ) throws Exception {
        URL SPREADSHEET_FEED_URL = new URL( "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

        // Make a request to the API and get all spreadsheets.
        SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
        List<SpreadsheetEntry> spreadsheets = feed.getEntries();

        SpreadsheetEntry spreadsheet = null;
        for ( SpreadsheetEntry ss: spreadsheets ) {
        	if ( ss.getTitle().getPlainText().equals( "todo") ) {
        		spreadsheet = ss;
        		break;
        	}
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

	protected ListFeed readTagsGoogleSpreadsheet( Context context) {

		SharedPreferences sharedPreferences = context.getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		GoogleAccountInfo googleAccountInfo = new GoogleAccountInfo( sharedPreferences );
		
		try {

            SpreadsheetService service = new SpreadsheetService("Testing");
            service.setUserCredentials(googleAccountInfo.getUsername(),  googleAccountInfo.getPassword() );
            
            URL listFeedURL = null;
            
            if ( googleAccountInfo.getTagsFeed() == null || googleAccountInfo.getTagsFeed().isEmpty() ) {
            	SpreadsheetEntry spreadsheet = openSpreadsheet( service );
            	
                /*
                 * Create the tags worksheet if it doesn't exists. And the first header line
                 */
            	if ( spreadsheet.getWorksheets().size() < 2 ) {
                	WorksheetEntry tagWorksheet = new WorksheetEntry();
                	tagWorksheet.setTitle( new PlainTextConstruct( "Tags" ) );
                	tagWorksheet.setRowCount( 15 );
                	tagWorksheet.setColCount( 15 );
                	tagWorksheet = service.insert( spreadsheet.getWorksheetFeedUrl(), tagWorksheet );
                	
                	URL cellFeedUrl= tagWorksheet.getCellFeedUrl();
                	CellFeed cellFeed= service.getFeed (cellFeedUrl, CellFeed.class);
                    CellEntry cellEntry= new CellEntry (1, 1, "name");
                	cellFeed.insert (cellEntry);
                }

            	WorksheetEntry worksheet = spreadsheet.getWorksheets().get(1);
                listFeedURL = worksheet.getListFeedUrl();
                
                googleAccountInfo.setTagsFeed( listFeedURL.toString() );
                googleAccountInfo.saveToPreferences( sharedPreferences );
            } else {
            	listFeedURL = new URL( googleAccountInfo.getTagsFeed() );
            }

            return service.getFeed( listFeedURL, ListFeed.class );
        } catch (Exception e) {
        	/*
        	 * Reset the List Feed caching, just in case it was the cause of the exception.
        	 */
            googleAccountInfo.setTagsFeed( null );
            googleAccountInfo.saveToPreferences( sharedPreferences );
        	throw new RuntimeException( e );
        }		
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
