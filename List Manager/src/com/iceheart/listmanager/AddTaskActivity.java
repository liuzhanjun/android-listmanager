package com.iceheart.listmanager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

public class AddTaskActivity extends Activity {
	
	private Task task;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_task);
		if ( getIntent().getExtras() != null && getIntent().getExtras().containsKey( "task" ) ) {
			this.task = (Task) getIntent().getExtras().get( "task");
		} else {
			task = new Task();
		}
		
		EditText editName = (EditText) findViewById(R.id.editItemName);
		editName.setText( task.getName() == null ? "": task.getName() );
		
		EditText price = (EditText) findViewById(R.id.editPrice);
		price.setText( task.getEstimatedPrice() == null ? "":  task.getEstimatedPrice().toPlainString() );
		
		EditText dueDate = (EditText) findViewById(R.id.editDueDate);
		dueDate.setText( task.getDueDate() == null ? "":  Task.DATE_FORMAT.format( task.getDueDate() ) );

		
		// TODO: Populate the task information on the form.
		
		
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
		
		// TODO: All fields
		
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
