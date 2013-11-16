package com.iceheart.listmanager.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;
import com.iceheart.listmanager.tasklist.TaskList;
import com.iceheart.listmanager.tasklist.TaskListCache;

/**
* Created by nmasse on 10/22/13.
*/
public class TaskRowAdapter extends BaseAdapter {

    private MainActivity mainActivity;
    private Calendar today;
    private List<Task> taskList;

    public TaskRowAdapter(MainActivity mainActivity, List<Task> taskList) {
    	this.taskList = taskList;
        this.mainActivity = mainActivity;
        today = Calendar.getInstance();
        today.setTime(new Date());
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
    }

    @SuppressLint("SimpleDateFormat")
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	View view = mainActivity.getLayoutInflater().inflate(R.layout.task_list_row, null);

        Task task = (Task) this.getItem( position);
        
        String itemDueDate = task.getDueDate() == null ? null: Task.DATE_FORMAT.format( task.getDueDate() );
        if ( itemDueDate != null && itemDueDate.length() > 0 && !task.isCompleted() ) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy/MM/dd" );

            ImageView itemImageView = (ImageView) view.findViewById(R.id.rowFlag);
            try {
                Calendar date = Calendar.getInstance();
                date.setTime( simpleDateFormat.parse(itemDueDate) );
                date.set( Calendar.HOUR_OF_DAY, 0 );
                date.set( Calendar.MINUTE, 0 );
                date.set( Calendar.SECOND, 0 );
                date.set( Calendar.MILLISECOND, 0 );

                if ( date.before(today) ) {
                    itemImageView.setImageResource(R.drawable.ic_overdue);
                } else if ( date.equals(today)) {
                    itemImageView.setImageResource(R.drawable.ic_due_today);
                } else {
                    itemImageView.setImageResource(0);
                }
            } catch (Exception e) {
                itemImageView.setImageResource(0);
            }
        }
        
        TextView itemName = (TextView) view.findViewById(R.id.rowItemName );
        itemName.setText( task.getName() );
        
        TextView dateField = (TextView) view.findViewById( R.id.rowItemDate );
        dateField.setText( task.getFormattedDueDate());

        String itemPrice = task.getEstimatedPrice() == null ? "": task.getEstimatedPrice().toString() + " $";
        TextView itemTextView = (TextView) view.findViewById(R.id.rowItemPrice);
        if ( itemPrice != null && itemPrice.length() > 0 ) {
            itemTextView.setBackgroundResource(R.drawable.task_row_price_background);
            itemTextView.setText( itemPrice );
        } else {
            itemTextView.setBackgroundResource(0);
        }

        Long taskListId = task.getListId();
        ListView tagsListView = (ListView) view.findViewById(R.id.tagColorView);
        if ( taskListId != null ) {
            if ( tagsListView != null ) {
            	// TODO: To revise (simplify since no more multiple task list.
            	List<TaskList> taskLists = new ArrayList<TaskList>();
            	taskLists.add( TaskListCache.getInstance().getById(taskListId));
                tagsListView.setAdapter(new TagColorViewAdapter(taskLists));
            }
        } else {
            tagsListView.setBackgroundResource(android.R.color.transparent);
        }

        return view;
    }

    private class TagColorViewAdapter extends ArrayAdapter<TaskList> {
        public TagColorViewAdapter(List<TaskList> tags) {
            super(TaskRowAdapter.this.mainActivity, R.layout.row_color, tags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = mainActivity.getLayoutInflater();
                convertView = inflater.inflate(R.layout.row_color, null);
            }

            int count = this.getCount();
            int height = parent.getHeight() / Math.min(count, 5);
            convertView.setMinimumHeight( height );
            convertView.setMinimumWidth( 5 );

            // Fetch the tag color
            GradientDrawable back = (GradientDrawable) convertView.getBackground();

            TaskList list = this.getItem(position);
            if ( this.getItem(position) != null ) {
                back.setColor(mainActivity.getResources().getColor(list.getTagColor()));
            } else {
                back.setColor(mainActivity.getResources().getColor(android.R.color.transparent));
            }

            return convertView;
        }
    }

	@Override
	public int getCount() {
		return taskList.size();
	}

	@Override
	public Object getItem(int index ) {
		return taskList.get( index );
	}

	@Override
	public long getItemId(int index ) {
		return  ((Task) taskList.get(index)).getId();
	}
}
