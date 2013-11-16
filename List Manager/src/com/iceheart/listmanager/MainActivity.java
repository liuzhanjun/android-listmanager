package com.iceheart.listmanager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ShareActionProvider;

import com.iceheart.listmanager.googlesync.GoogleTaskSynchronizer;
import com.iceheart.listmanager.task.Task;
import com.iceheart.listmanager.task.TaskDatasource;
import com.iceheart.listmanager.task.TaskRowAdapter;
import com.iceheart.listmanager.task.TaskStatus;
import com.iceheart.listmanager.tasklist.TaskList;
import com.iceheart.listmanager.tasklist.TaskListCache;
import com.iceheart.listmanager.tasklist.TaskListDatasource;
import com.iceheart.listmanager.tasklist.TaskListRowAdapter;
import com.iceheart.listmanager.tasklist.TaskListStatus;
import com.iceheart.listmanager.tasklist.TaskListType;

@SuppressLint("UseSparseArrays")
public class MainActivity extends FragmentActivity  {
	
	public static TaskList selectedList = new TaskList( TaskListType.SYSTEM_ALL );
	private static boolean firstLoad = true;
	private ArrayList<Task> tasks;
	private ActionBarDrawerToggle toggle;
	private ShareActionProvider mShareActionProvider;
    private ViewPager mViewPager;
    private TaskListCache taskListsCache;

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
            	GoogleTaskSynchronizer synchronizer = new GoogleTaskSynchronizer( this);
            	synchronizer.execute( this );
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
    	
    	GoogleTaskSynchronizer synchronizer = new GoogleTaskSynchronizer( this );
         synchronizer.execute( this );
    }
    
	public void refreshTasks() {
        // Create a new set of pages for the tabs based on the new TaskList
        final CollectionPagerAdapter pagerAdapter = new CollectionPagerAdapter(this, selectedList, getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);
        setTitle( pagerAdapter.getMainPageTitle( 0 ) );
        mViewPager.setOnPageChangeListener( new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				setTitle( pagerAdapter.getMainPageTitle( position ) );
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
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
        
        final ListView taskListListView = (ListView) findViewById(R.id.taskListListView);
        taskListListView.setAdapter(new TaskListRowAdapter(this, taskLists));
        
        taskListListView.setOnItemLongClickListener( new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int itemPos, long lng) {
				
  	  			 final TaskList tl = (TaskList) taskListListView.getItemAtPosition( itemPos );
  	  			 if ( tl.getType() != TaskListType.USER_DEFINED ) {
  	  				 return false;
  	  			 }

				 AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
           	  	 builder.setTitle(R.string.delete_list);
           	  	 builder.setMessage( R.string.delete_list_confirmation );
           	  	 builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
           	      
           	  		 @Override
           	  		 public void onClick(DialogInterface dialog, int which) {
           	  			 TaskListDatasource ds = new TaskListDatasource( MainActivity.this );
           	  			 ds.open();
           	  			 tl.setStatus( TaskListStatus.DELETED );
           	  			 // TODO: Delete all tasks under this list.
           	  			 ds.save( tl );
           	  			 ds.close();
           	  			 refreshTaskList();
           	  		 }
           	  	 });
           	  	 
	           	 builder.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
	           	      @Override
	           	      public void onClick(DialogInterface dialog, int which) {
	           	          dialog.cancel();
	           	      }
	           	  });

	           	  builder.show();
	           	  
	           	  return true;
			}
			
        });
        
        
        taskListListView.setOnItemClickListener(new OnItemClickListener() {
        	
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
              
              selectedList = (TaskList) (taskListListView.getItemAtPosition(myItemInt));
              
              
              if ( selectedList.getType() == TaskListType.SYSTEM_NEW_LIST ) {
            	  AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            	  builder.setTitle(R.string.add_list);

            	  // Set up the input
            	  final EditText input = new EditText(MainActivity.this);
            	  input.setInputType(InputType.TYPE_CLASS_TEXT);
            	  builder.setView(input);

            	  // Set up the buttons
            	  builder.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            	      @Override
            	      public void onClick(DialogInterface dialog, int which) {
            	  		TaskListDatasource ds = new TaskListDatasource( MainActivity.this );
            	        ds.open();
            	        TaskList tlist = new TaskList( input.getText().toString() );
            	        ds.save( tlist );
            	        ds.close();
            	        refreshTaskList();
            	      }
            	  });
            	  builder.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
            	      @Override
            	      public void onClick(DialogInterface dialog, int which) {
            	          dialog.cancel();
            	      }
            	  });

            	  builder.show();
              } else {
                  refreshTasks();
                  mDrawerLayout.closeDrawers();
              }
              
            }
        });        

	}	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        MenuItem item = menu.findItem(R.id.share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareListContent(tasks) );
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
    	builder.setMessage( task.getNotes());
    	builder.setNeutralButton(R.string.btn_markComplete,
    			new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialog, int id) {
            			task.setCompletedDate( new Date() );
            			// TODO: Ask for final price if any
            			TaskDatasource ds = new TaskDatasource(MainActivity.this );
            			 ds.open();
            			 ds.save( task );
            		     ds.close();
           	   		     refreshTasks();
            		     
                    }
    			}
        );
    	
    	
    	builder.setNegativeButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int id) {
    			task.setStatus(TaskStatus.DELETED);
    			task.setLastSynchroDate( new Date() );
    			TaskDatasource ds = new TaskDatasource(MainActivity.this );
	   			 ds.open();
	   			 ds.save( task );
	   		     ds.close();         
   	   		     refreshTasks();

    		}
		}
    	);
    	
    	builder.setPositiveButton(R.string.btn_showDetails,new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int id) {
    			Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
    			intent.putExtra("task", task );
    			startActivity( intent );
    		}
		}
    	); 
    	builder.setTitle(task.getName());
    	builder.setCancelable(true);
    	AlertDialog dialog = builder.create();
    	dialog.show();
    			 
	}
	
	public void openSettings( MenuItem menuItem ) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity( intent );
	}
	
	public void setShareContent( List<Task> tasks) {
		
		if ( mShareActionProvider == null ) {
			return;
		}
		
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, getShareListContent(tasks) );
		sendIntent.setType("text/plain");
		mShareActionProvider.setShareIntent( sendIntent );
	}
	
	private String getShareListContent(List<Task> tasks) {
		
		StringBuffer buffer = new StringBuffer();
		
		if ( selectedList != null ) {
			buffer.append( selectedList.getName() + " (" + selectedList.getTaskCount() + getString(R.string.suffix_items) + ")" );
		} else {
			buffer.append( getString(R.string.title_allTasks));
		}
		buffer.append( "\n\n" );
		
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

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    public static class CollectionPagerAdapter extends FragmentStatePagerAdapter {
    	
    	private static final int INCOMING 	= 0;
    	private static final int ALL 		= 1;
    	private static final int COMPLETED 	= 2;
    	
        private final MainActivity mainActivity;
        private TaskList taskList;
        private Map<Integer,ArrayList<Task>> tasks = new HashMap<Integer,ArrayList<Task>>();

        public CollectionPagerAdapter(MainActivity mainActivity, TaskList taskList, FragmentManager fm) {
            super(fm);
            this.mainActivity = mainActivity;
            this.taskList = taskList;
        }
        

        public CharSequence getMainPageTitle(int position) {
            String title = selectedList.getName();
            
            ArrayList<Task> tasksToDisplay = getTaskListForPage(position);
            if ( tasksToDisplay.size() > 0 ) {
            	
                title += " (" + tasksToDisplay.size() + " ";
                title += tasksToDisplay.size() > 1? mainActivity.getString(R.string.suffix_items): mainActivity.getString(R.string.suffix_item);
                BigDecimal totalPrice = new BigDecimal( 0 );
                for ( Task task: tasksToDisplay ) {
                	if ( task.getEstimatedPrice() != null ) {
                		totalPrice = totalPrice.add( task.getEstimatedPrice() );
                	}
                }
                
                if ( totalPrice.compareTo( BigDecimal.ZERO ) > 0 ) {
                	title += " , " + totalPrice + "$";
                }
                title += ")";
            }
            return title;
		}


		@Override
        public Fragment getItem(int position) {
			ArrayList<Task> tasksToDisplay = getTaskListForPage( position );
			mainActivity.setShareContent( tasksToDisplay ); // TODO: Determine if this is correct or if we should move this elswhere.
            ListFragment fragment = new ListFragment();
            Bundle args = new Bundle();
            args.putSerializable("tasks", tasksToDisplay);
            fragment.setArguments(args);
            return fragment;
        }
		
		
		public ArrayList<Task> getTaskListForPage( int pageIndex ) {
			ArrayList<Task> tasksToDisplay = tasks.get( pageIndex );
    	
			if ( tasksToDisplay == null ) {
				TaskDatasource ds = new TaskDatasource( mainActivity );
				ds.open();

				Long listId = (selectedList != null ) ? selectedList.getId(): null;
				switch( pageIndex ) {
				case INCOMING:
					tasksToDisplay = ds.findIncomingTask( listId );
					break;
				case ALL:
					tasksToDisplay = ds.findActiveTasksForList( selectedList.getId() );
					break;
				case COMPLETED:
					tasksToDisplay = ds.getAllCompletedTasks( listId );
					break;
				}
				tasks.put( pageIndex, tasksToDisplay );
				ds.close();
			}
			return tasksToDisplay;
		}

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch( position ) {
                default:
                case 0:
                    return mainActivity.getString(R.string.tab_incoming, taskList.getName());
                case 1:
                    return mainActivity.getString(R.string.tab_all, taskList.getName());
                case 2:
                    return mainActivity.getString(R.string.tab_completed, taskList.getName());
            }
        }

    }
    

    // Instances of this class are fragments representing a single
    // object in our collection.
    @SuppressLint("ValidFragment")
	public static class ListFragment extends Fragment {

        public ListFragment() {
        }
        
        @SuppressWarnings("unchecked")
		@Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
        	
            final ListView listView = (ListView) inflater.inflate( R.layout.task_list, container, false );

            // Add the click listener to view/edit the taks
            if (listView != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                        Task selectedTask = (Task) (listView.getItemAtPosition(myItemInt));
                        ((MainActivity)getActivity()).showTaskDialog(selectedTask);

                    }
                });
            }

            // Convert the allTasksForTaskList to maps for the list view
            List<Task> adapterList = new ArrayList<Task>();
            for ( Task task: (ArrayList<Task>)getArguments().getSerializable("tasks") ) {
                adapterList.add(task);
            }

            // Create the adapter for the list view
            TaskRowAdapter adapter = new TaskRowAdapter(((MainActivity)getActivity()), adapterList);

            listView.setAdapter( adapter );
            
            return listView;

        }

    }
}
