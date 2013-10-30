package com.iceheart.listmanager;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
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
import android.widget.SimpleAdapter;

import com.iceheart.listmanager.googlesync.GoogleTaskSynchronizer;
import com.iceheart.listmanager.tag.Tag;
import com.iceheart.listmanager.tag.TagDatasource;
import com.iceheart.listmanager.tag.TagRowAdapter;
import com.iceheart.listmanager.tag.TagStatus;
import com.iceheart.listmanager.tag.TagType;
import com.iceheart.listmanager.task.Task;
import com.iceheart.listmanager.task.TaskDatasource;
import com.iceheart.listmanager.task.TaskRowAdapter;
import com.iceheart.listmanager.task.TaskStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity  {
	
	public static Tag selectedTag = new Tag( TagType.SYSTEM_COMING_SOON );
	private static boolean firstLoad = true;
	private List<Task> tasks;
	private List<Tag> tags;
	private ActionBarDrawerToggle toggle;
	private ShareActionProvider mShareActionProvider;
    private ViewPager mViewPager;
//    private ActionBar.Tab allTasksTab;

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
/*
            // Specify that tabs should be displayed in the action bar.
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            // Create a tab listener that is called when the user changes tabs.
            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // When the tab is selected, switch to the
                    // corresponding page in the ViewPager.
                    mViewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

                }

                @Override
                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

                }
            };

            // Add 3 tabs, specifying the tab's text and TabListener
            allTasksTab = actionBar.newTab();
            allTasksTab.setText(getString(R.string.tab_all, selectedTag.getName()));
            allTasksTab.setTabListener(tabListener);
            actionBar.addTab(allTasksTab);

            ActionBar.Tab activeTasksTab = actionBar.newTab();
            activeTasksTab.setText(getString(R.string.tab_active, selectedTag.getName()));
            activeTasksTab.setTabListener(tabListener);
            actionBar.addTab(activeTasksTab);

            ActionBar.Tab completedTasksTab = actionBar.newTab();
            completedTasksTab.setText(getString(R.string.tab_completed, selectedTag.getName()));
            completedTasksTab.setTabListener(tabListener);
            actionBar.addTab(completedTasksTab);
*/
        }


        // Create a swipe listener to change the tab selected on swipe
/*
        mViewPager.setOnPageChangeListener(
            new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    // When swiping between pages, select the
                    // corresponding tab.
                    getActionBar().setSelectedNavigationItem(position);
                }
            });
*/
        SharedPreferences sharedPreferences = getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		String googleAccount = sharedPreferences.getString( ApplicationSettings.GOOGLE_ACCOUNT, "" );
		if ( googleAccount.isEmpty() ) {
			openSettings(null);
			return;
		}

        // Refresh task List
        refreshTaskList();
        // Refresh Tag List
		refreshTagList();

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
    
	public void refreshTaskList() {
		TaskDatasource ds = new TaskDatasource( this);
        ds.open();

        if ( selectedTag == null || selectedTag.getType() == TagType.SYSTEM_ALL ) {
            tasks = ds.getAllActiveTasks();
        } else if ( selectedTag.getType() == TagType.SYSTEM_COMING_SOON) {
            tasks = ds.findIncomingTask();
        } else {
            tasks = ds.findActiveTasksByTag( selectedTag.getName() );
        }
        ds.close();

        
        String title = selectedTag.getName();
        if ( tasks.size() > 0 ) {
        	
            title += " (" + tasks.size();
            title += tasks.size() > 1? getString(R.string.suffix_items):getString(R.string.suffix_item);
            BigDecimal totalPrice = new BigDecimal( 0 );
            for ( Task task: tasks ) {
            	if ( task.getEstimatedPrice() != null ) {
            		totalPrice = totalPrice.add( task.getEstimatedPrice() );
            	}
            }
            
            if ( totalPrice.compareTo( BigDecimal.ZERO ) > 0 ) {
            	title += " , " + totalPrice + "$";
            }
            title += ")";
        }
        setTitle( title );
        setShareContent( tasks );

        // Create a new set of pages for the tabs based on the new allTasksForTag list
        CollectionPagerAdapter pagerAdapter = new CollectionPagerAdapter(this, selectedTag, tasks, getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);

        // Set the actionBar item to the correct selected tab
/*
        allTasksTab.setText( selectedTag.getName() );
        getActionBar().selectTab(allTasksTab);
*/
	}
	
	public void refreshTagList() {
		
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		TagDatasource ds = new TagDatasource( this );
        ds.open();
        tags = ds.getAllActiveTags();
        
        /*
         * For each tag, get the number of task
         */
        for ( Tag tag: tags ) {
        	ds.calculateActiveTaskCount(tag);
        }       
        ds.close();
        
        List<Map<String, Object>> tags = new ArrayList<Map<String, Object>>();
        
        tags.add(new Tag(TagType.SYSTEM_ALL).toMap());
        tags.add(new Tag(TagType.SYSTEM_COMING_SOON).toMap());
        for ( Tag tag: this.tags) {
            tags.add(tag.toMap());
        }
        tags.add(new Tag(TagType.SYSTEM_NEW_TAG).toMap());
        
        final ListView tagListView = (ListView) findViewById(R.id.tagsList);
        tagListView.setAdapter(new TagRowAdapter(this, tags));
        
        
        tagListView.setOnItemLongClickListener( new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int itemPos, long lng) {
				
  	  			 final Map<String,Object> selectedTag = (Map<String,Object>)tagListView.getItemAtPosition( itemPos );
  	  			 
  	  			 Tag tag = (Tag) selectedTag.get( "tag" );
  	  			 if ( tag.getType() != TagType.USER_DEFINED ) {
  	  				 return false;
  	  			 }
  	  			 

				 AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
           	  	 builder.setTitle(R.string.delete_tag);
           	  	 builder.setMessage( R.string.delete_tag_confirmation );
           	  	 builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
           	      
           	  		 @Override
           	  		 public void onClick(DialogInterface dialog, int which) {
           	  			 TagDatasource ds = new TagDatasource( MainActivity.this );
           	  			 ds.open();
           	  			 Tag tagToDelete = ds.getTagByName( (String)selectedTag.get("name") );
           	  			 tagToDelete.setStatus( TagStatus.DELETED );
           	  			 ds.save( tagToDelete );
           	  			 ds.close();
           	  			 refreshTagList();
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
        
        
        tagListView.setOnItemClickListener(new OnItemClickListener() {
        	
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
              @SuppressWarnings("unchecked")
              Map<String,Object> selectedTagObj = (Map<String,Object>) (tagListView.getItemAtPosition(myItemInt));
              selectedTag = (Tag) selectedTagObj.get("tag");
              
              
              if ( selectedTag.getType() == TagType.SYSTEM_NEW_TAG ) {
            	  AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            	  builder.setTitle(R.string.add_Tag);

            	  // Set up the input
            	  final EditText input = new EditText(MainActivity.this);
            	  input.setInputType(InputType.TYPE_CLASS_TEXT);
            	  builder.setView(input);

            	  // Set up the buttons
            	  builder.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            	      @Override
            	      public void onClick(DialogInterface dialog, int which) {
            	  		TagDatasource ds = new TagDatasource( MainActivity.this );
            	        ds.open();
            	        Tag tag = new Tag( input.getText().toString() );
            	        ds.save( tag );
            	        ds.close();
            	        refreshTagList();
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
                  refreshTaskList();
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
	
	public void openCompletedTasks( MenuItem view ) {
		Intent intent = new Intent(this, CompletedTaskActivity.class);
		startActivity(intent);
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
    	builder.setMessage( task.getNotes());
    	builder.setNeutralButton(R.string.btn_markComplete,
    			new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialog, int id) {
            			task.setCompletedDate( new Date() );
            			// TODO: Ask for final price if any
            			 ds.open();
            			 ds.save( task );
            		     ds.close();
           	   		     refreshTaskList();
            		     
                    }
    			}
        );
    	
    	
    	builder.setNegativeButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int id) {
    			task.setStatus(TaskStatus.DELETED);
    			task.setLastSynchroDate( new Date() );
	   			 ds.open();
	   			 ds.save( task );
	   		     ds.close();         
   	   		     refreshTaskList();

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
		
		if ( selectedTag != null ) {
			buffer.append( selectedTag.getName() + " (" + selectedTag.getTaskCount() + getString(R.string.suffix_items) + ")" );
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

    public Tag getUserDefinedTagWithName( String name) {
        for( Tag tag : tags ) {
            if (tag.getType() == TagType.USER_DEFINED && tag.getName().equalsIgnoreCase( name ) ) {
                Log.d("getUserDefinedTagWithName", "found tag with name:" + name );
                return tag;
            }
        }
        Log.d("getUserDefinedTagWithName", "did NOT find tag with name:" + name );
        return null;
    }

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    public static class CollectionPagerAdapter extends FragmentStatePagerAdapter {
        private final MainActivity mainActivity;
        private Tag tag;
        private List<Task> tasksForTag;
        private List<Task> incompleteTasksForTag;
        private List<Task> completedTasksForTag;


        public CollectionPagerAdapter(MainActivity mainActivity, Tag tag, List<Task> allTasksForTag, FragmentManager fm) {
            super(fm);

            this.mainActivity = mainActivity;
            this.tag = tag;
            setTaskList(allTasksForTag);
        }

        @Override
        public Fragment getItem(int position) {
            ListFragment fragment = new ListFragment( mainActivity );
            switch( position ) {
                default:
                case 0:
                    fragment.setArgObject(tasksForTag);
                    break;
                case 1:
                    fragment.setArgObject(incompleteTasksForTag);
                    break;
                case 2:
                    fragment.setArgObject(completedTasksForTag);
                    break;
            }
            return fragment;
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
                    return mainActivity.getString(R.string.tab_all, tag.getName());
                case 1:
                    return mainActivity.getString(R.string.tab_active, tag.getName());
                case 2:
                    return mainActivity.getString(R.string.tab_completed, tag.getName());
            }
        }

        private void setTaskList( List<Task> tasks ) {
            this.tasksForTag = tasks;

            // Create filtered lists active (incompleteTasksForTag)
            // Create filtered lists completedTasksForTag
            this.incompleteTasksForTag = new ArrayList<Task>();
            this.completedTasksForTag = new ArrayList<Task>();
            for( Task task : tasks ) {
                if ( !task.isCompleted() ) {
                    incompleteTasksForTag.add(task);
                } else {
                    completedTasksForTag.add(task);
                }
            }

        }
    }

    // Instances of this class are fragments representing a single
    // object in our collection.
    public static class ListFragment extends Fragment {
        private final MainActivity mainActivity;
        private List<Task> tasks;

        public ListFragment(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        public void setArgObject( List<Task> tasks ) {
            this.tasks = tasks;
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {

            final ListView listView = (ListView) inflater.inflate( R.layout.task_list, container, false );

            // Add the click listener to view/edit the taks
            if (listView != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                        @SuppressWarnings("unchecked")
                        Map<String,String> selectedTask = (Map<String,String>) (listView.getItemAtPosition(myItemInt));
                        mainActivity.showTaskDialog(selectedTask);

                    }
                });
            }

            // Convert the allTasksForTag to maps for the list view
            List<Map<String, Object>> adapterList = new ArrayList<Map<String,Object>>();
            for ( Task task: tasks ) {
                adapterList.add(task.toMap());
            }

            // Create the adapter for the list view
            SimpleAdapter adapter = new TaskRowAdapter(mainActivity, adapterList);

            listView.setAdapter( adapter );

            return listView;

        }

    }
}
