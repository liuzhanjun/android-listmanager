package com.iceheart.listmanager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.iceheart.listmanager.task.Task;
import com.iceheart.listmanager.task.TaskDatasource;
import com.iceheart.listmanager.tasklist.TaskList;
import com.iceheart.listmanager.tasklist.TaskListCache;
import com.iceheart.listmanager.tasklist.TaskListType;

public class AddTaskActivity extends Activity {
	
	private Task task;
	private TaskList selectedList;
	private EditText tagsEditText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_task);
		getActionBar().setDisplayHomeAsUpEnabled( true );
		if ( getIntent().getExtras() != null && getIntent().getExtras().containsKey( "task" ) ) {
			this.task = (Task) getIntent().getExtras().get( "task");
		} else {
			task = new Task();
			
			/*
			 * If the task list was displaying a specific task list. Default this with this list.
			 */
			if ( MainActivity.selectedList != null && MainActivity.selectedList.getType() == TaskListType.USER_DEFINED ) {
				task.setListId( MainActivity.selectedList.getId() );
			}
		}
		
		EditText editName = (EditText) findViewById(R.id.editItemName);
		editName.setText( task.getName() == null ? "": task.getName() );
		editName.requestFocus();
		
		EditText price = (EditText) findViewById(R.id.editPrice);
		price.setText( task.getEstimatedPrice() == null ? "":  task.getEstimatedPrice().toPlainString() );
		
		EditText dueDate = (EditText) findViewById(R.id.editDueDate);
		dueDate.setText( task.getDueDate() == null ? "":  Task.DATE_FORMAT.format( task.getDueDate() ) );

		EditText notes = (EditText) findViewById(R.id.editNotes);
		notes.setText( task.getNotes() == null ? "":  task.getNotes() );
		
		tagsEditText = (EditText) findViewById(R.id.editTags);
		selectedList = TaskListCache.getInstance().getById(( task.getListId() ) );
		tagsEditText.setText( selectedList == null ? "":  selectedList.getName() );

	}
	
	public void chooseTags( View view ) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setCancelable(true);
		
		final String[] items = new String[ TaskListCache.getInstance().getTaskLists().size() ];
		
		int selectedIndex = -1;
		int i = 0;
		for ( TaskList taskList: TaskListCache.getInstance().getTaskLists() ) {
			items[ i ] = taskList.getName();
			if ( taskList == selectedList ) {
				selectedIndex = i; 
			}
			i++;
		}

		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which ) {
				selectedList = TaskListCache.getInstance().getTaskLists().get( which );
				tagsEditText.setText( selectedList.getName() );
				dialog.dismiss();
			}
		};
		
		builder.setSingleChoiceItems(items, selectedIndex, listener );
		AlertDialog dialog = builder.create();
		dialog.show();
   			 
	}	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.task = null;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_task, menu);
		return true;
	}
	
	public void save(View view ) {
		
		
		if ( !validate() ) {
			return;
		} 
		
		EditText itemNameText = (EditText) findViewById(R.id.editItemName);
		task.setName( itemNameText.getText().toString() );
			
		EditText price = (EditText) findViewById(R.id.editPrice);
		String value = price.getText().toString().trim();
		task.setEstimatedPrice( value.isEmpty()? null: new BigDecimal( value ) );
		
		EditText dueDate = (EditText) findViewById(R.id.editDueDate);
		task.setDueDate( dueDate.getText().toString() );
		
		EditText notes = (EditText) findViewById(R.id.editNotes);
		task.setNotes(notes.getText().toString() );

		task.setListId(  selectedList.getId() );
		
		task.setLastSynchroDate( new Date() );

		TaskDatasource ds = new TaskDatasource(this );
		ds.open();
		ds.save( task );
		ds.close();
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent );
	}
	
	private boolean validate() {
		
		/*
		 * Make sure the user have entered a task name
		 */
		EditText itemNameText = (EditText) findViewById(R.id.editItemName);
		String itemName = itemNameText.getText().toString();
		if ( itemName == null || itemName.isEmpty() ) {
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
       	  	 builder.setTitle(R.string.validation_failed);
       	  	 builder.setMessage( R.string.task_name_mandatory );
       	  	 builder.setCancelable(true);
       	  	 builder.show();
			return false;
		}
		
		
		/*
		 * Make sure the user have entered a tag.
		 */
		if ( selectedList == null ) {
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
       	  	 builder.setTitle(R.string.validation_failed);
       	  	 builder.setMessage( R.string.lists_must_be_specified );
       	  	 builder.setCancelable(true);
       	  	 builder.show();
			return false;
		}
		return true;
	}

	public void cancel(View view ) {
		this.task = null;
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent );
	}
	
	public void changeDueDate( View view )  {
		OnDateSetListener listener = new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				EditText dueDate = (EditText) findViewById(R.id.editDueDate);
				Calendar cal = Calendar.getInstance();
				cal.set( year, monthOfYear, dayOfMonth);
				dueDate.setText( Task.DATE_FORMAT.format( cal.getTime() ) );
			}
			
		};
		Date oldDueDate = task.getDueDate() == null ? new Date(): task.getDueDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime( oldDueDate );
		
		DatePickerDialog dateDialog = new DatePickerDialog(this, listener, cal.get(Calendar.YEAR), cal.get( Calendar.MONTH), cal.get( Calendar.DAY_OF_MONTH) );
        dateDialog.setCancelable(true);
		dateDialog.show();
		
	}
	
	public void deleteDueDate( View view ) {
		EditText dueDate = (EditText) findViewById(R.id.editDueDate);
		dueDate.setText( "" );
	}
	
	

}
