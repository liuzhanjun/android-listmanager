package com.iceheart.listmanager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

public class AddTaskActivity extends Activity {
	
	private Task task;
	private List<Tag> tags;
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
			 * If the task list was displaying a specific tag. Default this with this tag.
			 */
			if ( MainActivity.selectedTag != null ) {
				task.setTags( MainActivity.selectedTag.getName() );
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
		tagsEditText.setText( task.getTagsAsString() == null ? "":  task.getTagsAsString() );
		
		tags = new ArrayList<Tag>();
		
		 TagDatasource ds = new TagDatasource( this );
	     ds.open();
	     tags = ds.getTags();
	     ds.close();
		
		for ( Tag tag: tags ) {
			if ( task.getTags().contains( tag.getName() ) ) {
				tag.setSelected( true );
			}
		}

	}
	
	public void chooseTags( View view ) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setCancelable(true);
		final String[] items = new String[ tags.size() ];
		final boolean[] checked = new boolean[ tags.size() ];
		int i = 0;
		for ( Tag tag: tags ) {
			items[ i ] = tag.getName();
			checked[ i ] = tag.isSelected();
			i++;
		}
		
		OnMultiChoiceClickListener listener = new OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				tags.get( which ).setSelected( isChecked );
				String text = "";
				for ( Tag tag: tags ) {
					if ( tag.isSelected() ) {
						if ( !text.isEmpty() ) {
							text += ",";
						}
						text += tag.getName();
					}
				}
				tagsEditText.setText( text );
				
			}
		};
		
		builder.setMultiChoiceItems(items, checked, listener);
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
		EditText itemNameText = (EditText) findViewById(R.id.editItemName);
		task.setName( itemNameText.getText().toString() );
			
		EditText price = (EditText) findViewById(R.id.editPrice);
		String value = price.getText().toString().trim();
		task.setEstimatedPrice( value.isEmpty()? null: new BigDecimal( value ) );
		
		EditText dueDate = (EditText) findViewById(R.id.editDueDate);
		task.setDueDate( dueDate.getText().toString() );
		
		EditText notes = (EditText) findViewById(R.id.editNotes);
		task.setNotes(notes.getText().toString() );

		EditText tags = (EditText) findViewById(R.id.editTags);
		task.setTags( tags.getText().toString() );
		
		task.setLastSynchroDate( new Date() );

		// TODO: open ds on create.
		TaskDatasource ds = new TaskDatasource(this );
		ds.open();
		ds.save( task );
		ds.close();
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent );
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
		dateDialog.show();
		
	}
	
	

	

	

}
