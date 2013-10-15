package com.iceheart.listmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity  {
	
	private List<Task> tasks;
	private List<Tag> tags;
	private static boolean firstLoad = true;
	private static String selectedTag;
	private ListView listView;
	private ActionBarDrawerToggle toggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.taskList);
        
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle( this,  drawerLayout, R.drawable.ic_drawer,  0,0 ) {};

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(toggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);  
        
        toggle.syncState();
        
        SharedPreferences sharedPreferences = getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		String googleAccount = sharedPreferences.getString( ApplicationSettings.GOOGLE_ACCOUNT, "" );
		if ( googleAccount.isEmpty() ) {
			openSettings(null);
			return;
		}

		refreshTagList();
        refreshList();
        
        /*
         * Google Synchronization on startup (If settings says so)
         */
        if ( firstLoad ) {
        	if ( sharedPreferences.getBoolean( ApplicationSettings.SYNC_ON_STARTUP, Boolean.TRUE ) ) {
            	GoogleTaskSynchronizer synchronizer = new GoogleTaskSynchronizer( this);
            	synchronizer.execute( this );
        	}
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
    
	public void refreshList() {
		TaskDatasource ds = new TaskDatasource( this);
        ds.open();
        
        if ( selectedTag == null ) {
            tasks = ds.getAllActiveTasks();
        } else {
            tasks = ds.findActiveTasksByTag( selectedTag );
        }
        
        setTitle( selectedTag == null ? "ALL": selectedTag );
        ds.close();
        
        
        List<Map<String, String>> mylist = new ArrayList<Map<String, String>>();
        for ( Task task: tasks ) {
            mylist.add(task.toMap());
        }

        SimpleAdapter adapter = new SimpleAdapter(this, mylist, R.layout.row,
                new String[] {"name", "price", "dueDate" }, new int[] {R.id.rowItemName, R.id.rowItemPrice, R.id.rowItemDate}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Map<String, String> entry = (Map<String, String>) this.getItem(position);
                String itemDueDate = entry.get("dueDate");
                if ( itemDueDate != null && itemDueDate.length() > 0 ) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy/MM/dd");

                    try {
                        Date date = simpleDateFormat.parse(itemDueDate);

                        if ( date.before( new Date()) ) {
                            ImageView itemImageView = (ImageView) view.findViewById(R.id.rowFlag);
                            itemImageView.setImageResource(R.drawable.calendar);
                        }
                    } catch (ParseException e) {
                    }
                }

                String itemPrice = entry.get("price");
                if ( itemPrice != null && itemPrice.length() > 0 ) {
                    TextView itemTextView = (TextView) view.findViewById(R.id.rowItemPrice);
                    itemTextView.setBackgroundResource(R.drawable.row_price_background );
                }

                return view;
            }
        };

        listView.setAdapter( adapter );
	}
	
	public void refreshTagList() {
		
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		TagDatasource ds = new TagDatasource( this );
        ds.open();
        tags = ds.getTags();
        
        /*
         * For each tag, get the number of task
         * TODO: optimize this: Cache this info, or one request for all
         */
        for ( Tag tag: tags ) {
        	int taskCount = ds.findTaskCount( tag.getName() );
        	tag.setTaskCount( taskCount );
        }       
        ds.close();
        
        
        
        List<Map<String, String>> mylist = new ArrayList<Map<String, String>>();
        
        mylist.add( new Tag( "ALL" ).toMap() );
        for ( Tag tag: tags ) {
            mylist.add(tag.toMap());
        }
        mylist.add( new Tag( "New Tag" ).toMap() );        
        
        final ListView tagListView = (ListView) findViewById(R.id.tagsList);
        tagListView.setAdapter(new SimpleAdapter(this, mylist, R.layout.tag_row,
                new String[] {"name", "taskCount" }, new int[] {R.id.rowTagName, R.id.rowTagTaskCount}));
        
        
        tagListView.setOnItemLongClickListener( new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int itemPos, long lng) {

				 AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
           	  	 builder.setTitle(R.string.delete_tag);
           	  	 builder.setMessage( R.string.delete_tag_confirmation );
           	  	 builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { 
           	      
           	  		 @Override
           	  		 public void onClick(DialogInterface dialog, int which) {
           	  			 TagDatasource ds = new TagDatasource( MainActivity.this );
           	  			 ds.open();
           	  			 Map<String,String> selectedTag = (Map<String,String>)tagListView.getItemAtPosition( itemPos );
           	  			 ds.delete( new Tag( selectedTag.get( "name" ) ) );
           	  			 ds.close();
           	  			 refreshTagList();
           	  		 }
           	  	 });
           	  	 
	           	 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
              Map<String,String> selectedTagObj = (Map<String,String>) (tagListView.getItemAtPosition(myItemInt));
              selectedTag = selectedTagObj.get( "name" );
              
              
              if ( selectedTag.equalsIgnoreCase( "New Tag" ) ) {
            	  AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            	  builder.setTitle(R.string.add_Tag);

            	  // Set up the input
            	  final EditText input = new EditText(MainActivity.this);
            	  input.setInputType(InputType.TYPE_CLASS_TEXT);
            	  builder.setView(input);

            	  // Set up the buttons
            	  builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
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
            	  builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            	      @Override
            	      public void onClick(DialogInterface dialog, int which) {
            	          dialog.cancel();
            	      }
            	  });

            	  builder.show();
              } else {
                  if ( selectedTag.equalsIgnoreCase( "ALL" ) ) {
                	  selectedTag = null;
                  }
                  
                  
                  refreshList();
                  mDrawerLayout.closeDrawers();
              }
              
            }
        });        

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
    	builder.setMessage( task.getNotes());
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
    	builder.setTitle(task.getName());
    	builder.setCancelable(true);
    	AlertDialog dialog = builder.create();
    	dialog.show();
    			 
	}
	
	public void openSettings( MenuItem menuItem ) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity( intent );
	}

}
