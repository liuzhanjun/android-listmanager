package com.iceheart.listmanager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.gdata.data.DateTime;

public class MainActivity extends Activity  {
	
	private List<Task> tasks;
	private static boolean firstLoad = true;
	private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.taskList);
        
        TagListSpinnerAdaptor spinner = new TagListSpinnerAdaptor( this );
       	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
       	getActionBar().setListNavigationCallbacks(spinner, spinner);
       	getActionBar().setDisplayShowTitleEnabled( false );
        
        SharedPreferences sharedPreferences = getSharedPreferences(ApplicationSettings.SETTINGS_LIST,  0 );
		String googleAccount = sharedPreferences.getString( ApplicationSettings.GOOGLE_ACCOUNT, "" );
		if ( googleAccount.isEmpty() ) {
			openSettings(null);
			return;
		}

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
        } );


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
		refreshList( null );
	}
    

	public void refreshList( String tagName ) {
		TaskDatasource ds = new TaskDatasource( this);
        ds.open();
        
        if ( tagName == null ) {
            tasks = ds.getAllActiveTasks();
        } else {
            tasks = ds.findActiveTasksByTag( tagName );
        }
        ds.close();
        
        
        List<Map<String, String>> mylist = new ArrayList<Map<String, String>>();
        for ( Task task: tasks ) {
            mylist.add(task.toMap());
        }

        SimpleAdapter adapter = new SimpleAdapter(this, mylist, R.layout.row,
                new String[] {"name", "price", "dueDate" }, new int[] {R.id.rowItemName, R.id.rowItemPrice, R.id.rowItemDate}) {

            private ArrayList<ItemDrawable> itemBackgroundDrawables = new ArrayList<ItemDrawable>();

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                while ( position >= itemBackgroundDrawables.size() ) {
                    itemBackgroundDrawables.add( null );
                }

                convertView = super.getView(position, convertView, parent);

                ItemDrawable itemBackgroundDrawable = itemBackgroundDrawables.get(position);
                if ( itemBackgroundDrawable == null ) {
                    itemBackgroundDrawable = new ItemDrawable((Map<String, String>) this.getItem( position ));
                    itemBackgroundDrawables.set( position, itemBackgroundDrawable );
                }

                convertView.setBackground( itemBackgroundDrawable );

                return convertView;
            }
        };

        listView.setAdapter( adapter );
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

    public class ItemDrawable extends ShapeDrawable {
        private ItemShape itemShape;

        public ItemDrawable( Map<String,String> item ) {
            super();

            this.itemShape = new ItemShape( item );
            setShape(this.itemShape);
        }

        @Override
        public Shape getShape() {
            return itemShape;
        }
    }

    public class ItemShape extends Shape {
        private Map<String, String> item;

        public ItemShape( Map<String, String> item ) {
            super();
            this.item = item;
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            Log.v(this.getClass().getSimpleName(), "draw.item=" +  item );
            Log.v(this.getClass().getSimpleName(), "draw.item=" + item.get("dueDate"));

            boolean passed = false;
            try {
                SimpleDateFormat format =
                        new SimpleDateFormat("yyyy/MM/dd");
                Date date = format.parse( item.get("dueDate") );

                if ( DateTime.now().compareTo( date ) > 0 ) {
                    passed = true;
                }
            } catch (Exception ex) {
                Log.v(this.getClass().getSimpleName(), "Exception:" + ex );
            }
//            if ( position % 3 == 0 ) {
//                strokePaint.setARGB( 255, 255, 255, 0);
//            } else if ( position %3 == 1 ) {
//                strokePaint.setARGB( 255, 0, 255, 255);
//            } else {
//                // Do nothing
//            }

            // Draw default background
            //white background
//            canvas.drawRGB(255, 255, 255);
            // Box's properties
            Rect rect = new Rect( 0, 0, canvas.getWidth(), canvas.getHeight());
            RectF rectF = new RectF(rect);

            // Draw white background
            paint.setARGB(0xFF,0xff, 0xff, 0xff);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rectF, 10.0f, 10.0f, paint);

            // Draw rounded border
            paint.setARGB(0xFF,0x00, 0, 0);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
            canvas.drawRoundRect(rectF, 10.0f, 10.0f, paint);

            // draw the flag
            if ( passed && false) {
                paint.setARGB( 255, 255, 0, 0);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStrokeWidth(1);

                Rect rectFlag = new Rect( 0, 0, 4, canvas.getHeight() );

                canvas.drawRect( rectFlag, paint);
            }
        }

    }
}
