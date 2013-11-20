package com.iceheart.listmanager.tasklist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;

/**
* Created by nmasse on 11/19/13.
*/
public class TaskListItem_OnItemClickListener implements AdapterView.OnItemClickListener {

    private MainActivity mainActivity;
    private final ListView taskListView;
    private final DrawerLayout mDrawerLayout;

    public TaskListItem_OnItemClickListener(MainActivity mainActivity, ListView taskListView, DrawerLayout mDrawerLayout) {
        this.mainActivity = mainActivity;
        this.taskListView = taskListView;
        this.mDrawerLayout = mDrawerLayout;
    }

    public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {

        MainActivity.selectedList = (TaskList) (taskListView.getItemAtPosition(myItemInt));


        if (MainActivity.selectedList.getType() == TaskListType.SYSTEM_NEW_LIST) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setTitle(R.string.add_list);

            // Set up the input
            final EditText input = new EditText(mainActivity);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(mainActivity.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TaskListDatasource ds = new TaskListDatasource(mainActivity);
                    ds.open();
                    TaskList tlist = new TaskList(input.getText().toString());
                    ds.save(tlist);
                    ds.close();
                    mainActivity.refreshTaskList();
                }
            });
            builder.setNegativeButton(mainActivity.getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            mainActivity.refreshTasks();
            mDrawerLayout.closeDrawers();
        }

    }
}
