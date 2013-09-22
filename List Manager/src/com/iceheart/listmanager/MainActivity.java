package com.iceheart.listmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {
	
	private List<Task> tasks;
	private static boolean firstLoad = true;
	private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.taskList);
        
        
        
		SharedPreferences sharedPreferences = getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		String googleAccount = sharedPreferences.getString( ApplicationSettings.GOOGLE_ACCOUNT, "" );
		if ( googleAccount.isEmpty() ) {
			openSettings(null);
			return;
		}

        
        refreshList();
        
        // TODO: Settings to synchronize or not.
        if ( firstLoad ) {
        	GoogleTaskSynchronizer synchronizer = new GoogleTaskSynchronizer( this);
        	synchronizer.execute( this );
            firstLoad = false;
        }
        
        listView.setOnItemClickListener(new OnItemClickListener() {
        	
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
              @SuppressWarnings("unchecked")
			Map<String,String> selectedTask = (Map<String,String>) (listView.getItemAtPosition(myItemInt));
              showTaskDialog( selectedTask );

            }
        });
        
    }
    
    /**
     * Synchronize The Task list with the google account.
     * 
     * @param menuItem The menu item calling this action.
     */
    public void synchronizeWithGoogle( MenuItem menuItem ) {
    	
    	GoogleTaskSynchronizer synchronizer = new GoogleTaskSynchronizer( this );
         synchronizer.execute( this );
    }

	public void refreshList() {
		TaskDatasource ds = new TaskDatasource( this);
        ds.open();
        tasks = ds.getAllActiveTasks();
        ds.close();
        
        
        List<Map<String, String>> mylist = new ArrayList<Map<String, String>>();
        for ( Task task: tasks ) {
            mylist.add(task.toMap());
        }
        
        listView.setAdapter(new SimpleAdapter(this, mylist, R.layout.row,
                new String[] {"name", "price", "dueDate" }, new int[] {R.id.rowItemName, R.id.rowItemPrice, R.id.rowItemDate}));
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    

	public void openAddTask( View view ) {
		Intent intent = new Intent(this, AddTaskActivity.class);
		startActivity( intent );
	}
	
	public void openAddTask( MenuItem item ) {
		Intent intent = new Intent(this, AddTaskActivity.class);
		startActivity( intent );
	}
	
	public void showTaskDialog(Map<String,String> taskInfo ) {
		
		 final TaskDatasource ds = new TaskDatasource( this);
	      ds.open();
	     final Task task = ds.getTaskById( Long.valueOf( taskInfo.get( "id" ) ) );
	     ds.close();
		
    	AlertDialog.Builder builder = new AlertDialog.Builder( this );
    	builder.setMessage(taskInfo.get( "name" ));
    	builder.setNeutralButton(R.string.dlgTask_markComplete, 
    			new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialog, int id) {
            			task.setCompletedDate( new Date() );
            			// TODO: Ask for final price if any
            			 ds.open();
            			 ds.save( task );
            		     ds.close();
           	   		     refreshList();
            		     
            		}
    			}
            	);
    	
    	
    	builder.setNegativeButton(R.string.dlgTask_delete, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int id) {
    			task.setStatus(TaskStatus.DELETED);
    			task.setLastSynchroDate( new Date() );
	   			 ds.open();
	   			 ds.save( task );
	   		     ds.close();         
   	   		     refreshList();

    		}
		}
    	);
    	
    	builder.setPositiveButton(R.string.showDetails,new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int id) {
    			Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
    			intent.putExtra("task", task );
    			startActivity( intent );
    		}
		}
    	); 
    	builder.setTitle(R.string.dlgTask_title);
    	builder.setCancelable(true);
    	AlertDialog dialog = builder.create();
    	dialog.show();
    			 
	}
	
	public void openSettings( MenuItem menuItem ) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity( intent );
	}
    
    
}
