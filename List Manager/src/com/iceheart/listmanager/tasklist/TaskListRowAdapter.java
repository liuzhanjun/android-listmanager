package com.iceheart.listmanager.tasklist;

import java.util.List;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;

/**
* Created by nmasse on 10/22/13.
*/
public class TaskListRowAdapter extends BaseAdapter {

    private MainActivity mainActivity;
    private List<TaskList> taskList;

    public TaskListRowAdapter(MainActivity mainActivity, List<TaskList> mylist) {
    	super();
        this.mainActivity = mainActivity;
        this.taskList = mylist;
    }

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mainActivity.getLayoutInflater().inflate(R.layout.tag_row, null);

        ImageView iconView = (ImageView) view.findViewById(R.id.rowTagIcon);
        TextView tagCountView = (TextView) view.findViewById(R.id.rowTagTaskCount);
        TaskList tlist = taskList.get( position );
        TextView tagName = (TextView) view.findViewById(R.id.rowTagName);
        
        tagCountView.setText( String.valueOf(tlist.getTaskCount()) );
        tagName.setText( tlist.getName() );

        if ( tlist != null ) {
            int resourceId = tlist.getIconId();
            iconView.setImageResource(resourceId);

            GradientDrawable back = (GradientDrawable) tagCountView.getBackground();

            back.setColor(mainActivity.getResources().getColor(tlist.getTagColor()));
        }

        return view;
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
	public long getItemId(int index) {
		if ( taskList.size() <= index ) {
			return -1;
		}
		TaskList list = taskList.get( index );
		return list.getId() == null ? -index: list.getId();
	}

}
