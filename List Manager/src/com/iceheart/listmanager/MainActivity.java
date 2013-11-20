package com.iceheart.listmanager;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ShareActionProvider;

import com.iceheart.listmanager.googlesync.GoogleTaskSynchronizer;
import com.iceheart.listmanager.task.Task;
import com.iceheart.listmanager.taskdialog.TaskDialog_delete_OnClickListener;
import com.iceheart.listmanager.taskdialog.TaskDialog_markComplete_OnClickListener;
import com.iceheart.listmanager.taskdialog.TaskDialog_showDetails_OnClickListener;
import com.iceheart.listmanager.tasklist.TaskListItem_OnItemClickListener;
import com.iceheart.listmanager.tasklist.TaskList;
import com.iceheart.listmanager.tasklist.TaskListCache;
import com.iceheart.listmanager.tasklist.TaskListItem_OnItemLongClickListener;
import com.iceheart.listmanager.tasklist.TaskListRowAdapter;
import com.iceheart.listmanager.tasklist.TaskListType;
import com.iceheart.listmanager.tasklistpager.TaskListPagerAdapter;
import com.iceheart.listmanager.tasklistpager.TaskListPagerOnPageChangeListener;

@SuppressLint("UseSparseArrays")
public class MainActivity extends FragmentActivity  {
	
	public static TaskList selectedList = new TaskList( TaskListType.SYSTEM_ALL );
	private static boolean firstLoad = true;
	private ArrayList<Task> tasks;
	private ActionBarDrawerToggle toggle;
	private ShareActionProvider mShareActionProvider;
    private ViewPager mViewPager;
    private TaskListCache taskListsCache;
    private GoogleTaskSynchronizer googleTaskSynchronizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle( this,  drawerLayout, R.drawable.ic_drawer,  0,0 ) {};

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(toggle);

        // ViewPager and its adapters use support library fragments, so use getSupportFragmentManager.
        mViewPager = (ViewPager) findViewById(R.id.pager);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }


        SharedPreferences sharedPreferences = getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		String googleAccount = sharedPreferences.getString( ApplicationSettings.GOOGLE_ACCOUNT, "" );
		if ( googleAccount.isEmpty() ) {
			openSettings(null);
			return;
		}

        refreshTasks();
		refreshTaskList();

        /*
         * Google Synchronization on startup (If settings says so)
         */
        if ( firstLoad ) {
        	if ( sharedPreferences.getBoolean( ApplicationSettings.SYNC_ON_STARTUP, Boolean.TRUE ) ) {
                googleTaskSynchronizer = new GoogleTaskSynchronizer( this);
            	googleTaskSynchronizer.execute(this);
        	}
            firstLoad = false;
            toggle.syncState();
        }
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (toggle.onOptionsItemSelected(item)) {
          return true;
        }
        
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Synchronize The Task list with the google account.
     * 
     * @param menuItem The menu item calling this action.
     */
    public void synchronizeWithGoogle( MenuItem menuItem ) {

    	googleTaskSynchronizer = new GoogleTaskSynchronizer( this );
        googleTaskSynchronizer.execute(this);
    }
    
	public void refreshTasks() {
        // Create a new set of pages for the tabs based on the new TaskList
        final TaskListPagerAdapter pagerAdapter = new TaskListPagerAdapter(this, selectedList, getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);
        OnPageChangeListener onPageChangeListener = new TaskListPagerOnPageChangeListener(this, pagerAdapter);
		
		// Force the selection of the first page (to trigger event)
		onPageChangeListener.onPageSelected( 0 );
		
        mViewPager.setOnPageChangeListener( onPageChangeListener );
        
	}
	
	public void refreshTaskList() {
		
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        if ( taskListsCache == null ) {
            taskListsCache = TaskListCache.getInstance( this );
        } else {
            taskListsCache.refreshCache();
        }
        
        List<TaskList> taskLists = new ArrayList<TaskList>();
        
        taskLists.add(new TaskList(TaskListType.SYSTEM_ALL));
        taskLists.addAll( taskListsCache.getTaskLists() );
        taskLists.add(new TaskList(TaskListType.SYSTEM_NEW_LIST));
        
        final ListView taskListView = (ListView) findViewById(R.id.taskListListView);
        taskListView.setAdapter(new TaskListRowAdapter(this, taskLists));
        
        taskListView.setOnItemLongClickListener(new TaskListItem_OnItemLongClickListener(this, taskListView));
        
        
        taskListView.setOnItemClickListener(new TaskListItem_OnItemClickListener(this, taskListView, mDrawerLayout));

	}	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        MenuItem item = menu.findItem(R.id.share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareListContent(tasks));
        shareIntent.setType("text/plain");
        mShareActionProvider.setShareIntent(shareIntent);
        
        return true;
    }
    
	public void openAddTask( View view ) {
		Intent intent = new Intent(this, AddTaskActivity.class);
		startActivity(intent);
	}
	
	public void openAddTask( MenuItem item ) {
		Intent intent = new Intent(this, AddTaskActivity.class);
		startActivity( intent );
	}
	
	public void showTaskDialog( final Task task ) {
		
    	AlertDialog.Builder builder = new AlertDialog.Builder( this );
    	builder.setMessage(task.getNotes());
    	builder.setNeutralButton(R.string.btn_markComplete,
                new TaskDialog_markComplete_OnClickListener(this, task)
        );
    	
    	
    	builder.setNegativeButton(R.string.btn_delete, new TaskDialog_delete_OnClickListener(this, task)
    	);
    	
    	builder.setPositiveButton(R.string.btn_showDetails, new TaskDialog_showDetails_OnClickListener(this, task)
    	); 
    	builder.setTitle(task.getName());
    	builder.setCancelable(true);
    	AlertDialog dialog = builder.create();
    	dialog.show();
    			 
	}
	
	public void openSettings( MenuItem menuItem ) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void setShareContent( List<Task> tasks) {
		
		if ( mShareActionProvider == null ) {
			return;
		}
		
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, getShareListContent(tasks) );
		sendIntent.setType("text/plain");
		mShareActionProvider.setShareIntent(sendIntent);
	}
	
	private String getShareListContent(List<Task> tasks) {
		
		// TODO: HTML CONTENT ?
		
		StringBuffer buffer = new StringBuffer();
		int taskCount = tasks == null ? 0: tasks.size();
		
		if ( selectedList != null ) {
			buffer.append( selectedList.getName() + " (" + taskCount + " " + getString(R.string.suffix_items) + ")" );
		} else {
			buffer.append( getString(R.string.title_allTasks));
		}
		buffer.append("\n\n");
		
		if ( tasks != null ) {
			for ( Task task: tasks ) {
				if ( buffer.length() > 0 ) {
					buffer.append( "\n" );
				}
				buffer.append( "- ");
				buffer.append( task.getName() );
			}
			
		}
		return buffer.toString();
		
	}

    public TaskList getUserDefinedTaskListWithName( String name) {
        for( TaskList tlist : taskListsCache.getTaskLists() ) {
            if (tlist.getType() == TaskListType.USER_DEFINED && tlist.getName().equalsIgnoreCase( name ) ) {
                Log.d("getUserDefinedTaskListWithName", "found taskList with name:" + name );
                return tlist;
            }
        }
        Log.d("getUserDefinedTaskListWithName", "did NOT find taskList with name:" + name );
        return null;
    }


    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (googleTaskSynchronizer != null) {
//            googleTaskSynchronizer.onActivityResult(requestCode, resultCode, data);
        }

    }
}