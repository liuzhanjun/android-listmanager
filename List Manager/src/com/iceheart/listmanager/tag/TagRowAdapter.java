package com.iceheart.listmanager.tag;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;

import java.util.List;
import java.util.Map;

/**
* Created by nmasse on 10/22/13.
*/
public class TagRowAdapter extends SimpleAdapter {

    private MainActivity mainActivity;

    public TagRowAdapter(MainActivity mainActivity, List<Map<String, Object>> mylist) {
        super(mainActivity, mylist, R.layout.tag_row, new String[]{"name", "taskCount"}, new int[]{R.id.rowTagName, R.id.rowTagTaskCount});
        this.mainActivity = mainActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageView iconView = (ImageView) view.findViewById(R.id.rowTagIcon);
        TextView tagCountView = (TextView) view.findViewById(R.id.rowTagTaskCount);

        Tag tag = (Tag) ((Map<String, Object>)this.getItem(position)).get("tag");

        if ( tag != null ) {
            int resourceId = tag.getIconId();
            iconView.setImageResource(resourceId);

            GradientDrawable back = (GradientDrawable) tagCountView.getBackground();

            back.setColor(mainActivity.getResources().getColor(tag.getTagColor()));
        }


        return view;
    }

}
