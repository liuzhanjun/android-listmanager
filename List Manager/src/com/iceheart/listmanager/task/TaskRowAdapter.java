package com.iceheart.listmanager.task;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;
import com.iceheart.listmanager.tasklist.TaskList;
import com.iceheart.listmanager.tasklist.TaskListCache;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* Created by nmasse on 10/22/13.
*/
public class TaskRowAdapter extends SimpleAdapter {

    private MainActivity mainActivity;
    private Calendar today;

    public TaskRowAdapter(MainActivity mainActivity, List<Map<String, Object>> mylist) {
        super(mainActivity, mylist, R.layout.task_list_row, new String[]{"name", "price", "formattedDueDate"}, new int[]{R.id.rowItemName, R.id.rowItemPrice, R.id.rowItemDate});
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
	@SuppressWarnings("unchecked")
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Map<String, Object> entry = (Map<String, Object>) this.getItem(position);
        String itemDueDate = (String)entry.get("dueDate");
        if ( itemDueDate != null && itemDueDate.length() > 0 ) {
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

        String itemPrice = (String)entry.get("price");
        TextView itemTextView = (TextView) view.findViewById(R.id.rowItemPrice);
        if ( itemPrice != null && itemPrice.length() > 0 ) {
            itemTextView.setBackgroundResource(R.drawable.task_row_price_background);
        } else {
            itemTextView.setBackgroundResource(0);
        }

        Task task = (Task)entry.get("task");
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
}
