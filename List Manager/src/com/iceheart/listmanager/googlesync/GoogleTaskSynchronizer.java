package com.iceheart.listmanager.googlesync;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.iceheart.listmanager.Tag;
import com.iceheart.listmanager.TagDatasource;
import com.iceheart.listmanager.TagStatus;
import com.iceheart.listmanager.Task;
import com.iceheart.listmanager.TaskDatasource;
import com.iceheart.listmanager.TaskStatus;

/**
 * Class responsible to synchronize the task list with the google spreadsheet.
 * This operation is done in background.
 * @author Luc Martineau
 *
 */
public class GoogleTaskSynchronizer extends AsyncTask<Context, Void, Boolean> {
	
	private ProgressDialog progressDialog;
	private MainActivity taskListActivity;
	
	public GoogleTaskSynchronizer( MainActivity activity ) {
		this.taskListActivity = activity;
		progressDialog = new ProgressDialog( activity);
	}
	
	/**
	 * @Return a boolean indicating if the synchronization has been completed or not.
	 */
	@Override
	protected Boolean doInBackground(Context... params) {
		
		Context context = params[0];
		
		// TODO: Check if there is an Internet connection available or not.
		boolean internetConnectionAvailable = true;
		if ( !internetConnectionAvailable ) {
			return Boolean.FALSE;
		}
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		long lastSynchronisation = sharedPreferences.getLong( ApplicationSettings.LAST_SYNCHRONIZATION, -1 );
		
		
		synchronizeTaskList(context, lastSynchronisation);
		synchronizeTags( context, lastSynchronisation );

		
		/*
		 * Update the synchronization date in the shared preferences.
		 */
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(ApplicationSettings.LAST_SYNCHRONIZATION, System.currentTimeMillis() );
        editor.apply();
        editor.commit();
		
		return Boolean.TRUE;
		
		
		

	}

	private void synchronizeTags(Context context, long lastSynchronisation) {

		TagDatasource ds = new TagDatasource( context );
		ds.open();
		List<Tag> localTags = ds.getAllTags();
		Map<String, Tag> tagMap = new HashMap<String,Tag>();
		for ( Tag localTag: localTags ) {
			tagMap.put( localTag.getName(), localTag );
		}
		
		ListFeed tagsFromGoogle = readTagsGoogleSpreadsheet(context);
		
		
		for ( ListEntry tagEntry: tagsFromGoogle.getEntries() ) {
			Tag googleTag = listEntryToTag( tagEntry );
			
			Tag localTag = tagMap.remove( googleTag.getName()  );
			
			if ( localTag == null ) {
				googleTag = ds.save( googleTag );
				updateTagEntry(tagEntry, googleTag);
			} else if ( localTag.getStatus() == TagStatus.DELETED ) {
				try {
					tagEntry.delete();
				} catch (Exception e) {
					e.printStackTrace();
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
		
		for ( Tag localTask: tagMap.values() ) {
			
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
						// TODO Exception handling
						e.printStackTrace();
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

		TaskDatasource ds = new TaskDatasource( context);
		ds.open();
		List<Task> localTasks = ds.getAllTasks();
		Map<String, Task> taskMap = new HashMap<String,Task>();
		for ( Task localTask: localTasks ) {
			taskMap.put( localTask.getFunctionalId(), localTask );
		}
		
		ListFeed tasksFromGoogle = readTasksGoogleSpreadsheet(context);
		
		for ( ListEntry taskEntry: tasksFromGoogle.getEntries() ) {
			Task googleTask = listEntryToTask( taskEntry );
			
			Task localTask = taskMap.remove( googleTask.getFunctionalId()  );
			
			if ( localTask == null ) {
				googleTask = ds.save( googleTask );
				updateTaskEntry(taskEntry, googleTask);
			} else if ( localTask.getStatus() == TaskStatus.DELETED ) {
				try {
					taskEntry.delete();
				} catch (Exception e) {
					e.printStackTrace();
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
		
		for ( Task localTask: taskMap.values() ) {
			
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
						// TODO Exception handling
						e.printStackTrace();
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
    	return task;
		
	}
	
	private Tag listEntryToTag( ListEntry entry ) {
    	Tag tag = new Tag();
    	tag.setName(entry.getCustomElements().getValue( "name" ));
    	return tag;
		
	}
	
	
	protected ListFeed readTasksGoogleSpreadsheet( Context context) {
		try {
			
			SharedPreferences sharedPreferences = context.getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
			GoogleAccountInfo googleAccountInfo = new GoogleAccountInfo( sharedPreferences );

            SpreadsheetService service = new SpreadsheetService("Testing");
            service.setUserCredentials(googleAccountInfo.getUsername(),  googleAccountInfo.getPassword() );
            
            URL listFeedURL = null;
            
            if ( googleAccountInfo.getTaskListFeed() == null || googleAccountInfo.getTaskListFeed().isEmpty() ) {
            	SpreadsheetEntry spreadsheet = openSpreadsheet( service );
            	
                WorksheetEntry worksheet = spreadsheet.getWorksheets().get(0);
                listFeedURL = worksheet.getListFeedUrl();
                
                googleAccountInfo.setTaskListFeed( listFeedURL.toString() );
                googleAccountInfo.saveToPreferences( sharedPreferences );
            } else {
            	listFeedURL = new URL( googleAccountInfo.getTaskListFeed() );
            }

            return service.getFeed( listFeedURL, ListFeed.class );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
		try {
			
			SharedPreferences sharedPreferences = context.getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
			GoogleAccountInfo googleAccountInfo = new GoogleAccountInfo( sharedPreferences );

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
            e.printStackTrace();
            return null;
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
		taskListActivity.refreshList();
	}

}