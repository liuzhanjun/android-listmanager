package com.iceheart.listmanager;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ActionBar.OnNavigationListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TagListSpinnerAdaptor extends BaseAdapter implements OnNavigationListener {
	
	// TODO: Implement the tags management screen to make it dynamic.
	
	private Activity activity;
	private List<String> tags = Arrays.asList(new String[] { "ALL Tasks", "TODO", "Maison", "List Manager"});
	
	public TagListSpinnerAdaptor( Activity activity) {
		super();
		this.activity = activity;
	}
	
	@Override
	public int getCount() {
		return tags.size();
	}

	@Override
	public Object getItem(int index ) {
		return tags.get( index );
	}

	@Override
	public long getItemId(int index ) {
		return index;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LayoutInflater inflater = activity.getLayoutInflater();

		View convertView = inflater.inflate(R.layout.spinner_item, null);
		TextView spinnerItemView = (TextView) convertView.findViewById( R.id.lblSpinnerItem);
		spinnerItemView.setText( tags.get( arg0) );
		
		return convertView;

	}

	@Override
	public boolean onNavigationItemSelected(int index, long itemId ) {
		if ( index == 0 ) {
			((MainActivity)activity).refreshList();
			
		} else {
			String tagName = tags.get( index );
			((MainActivity)activity).refreshList( tagName );
			
		}
		return true;
	}
}
