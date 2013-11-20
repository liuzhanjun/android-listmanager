package com.iceheart.listmanager.tasklist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;

/**
* Created by nmasse on 11/19/13.
*/
public class TaskListItem_OnItemLongClickListener implements AdapterView.OnItemLongClickListener {

    private MainActivity mainActivity;
    private final ListView taskListView;

    public TaskListItem_OnItemLongClickListener(MainActivity mainActivity, ListView taskListView) {
        this.mainActivity = mainActivity;
        this.taskListView = taskListView;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int itemPos, long lng) {

        final TaskList tl = (TaskList) taskListView.getItemAtPosition(itemPos);
        if (tl.getType() != TaskListType.USER_DEFINED) {
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(R.string.delete_list);
        builder.setMessage(R.string.delete_list_confirmation);
        builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                TaskListDatasource ds = new TaskListDatasource(mainActivity);
                ds.open();
                tl.setStatus(TaskListStatus.DELETED);
                // TODO: Delete all tasks under this list.
                ds.save(tl);
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

        return true;
    }

}
