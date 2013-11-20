package com.iceheart.listmanager.taskdialog;

import android.content.DialogInterface;

import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.task.Task;
import com.iceheart.listmanager.task.TaskDatasource;
import com.iceheart.listmanager.task.TaskStatus;

import java.util.Date;

/**
* Created by nmasse on 11/19/13.
*/
public class TaskDialog_delete_OnClickListener implements DialogInterface.OnClickListener {
    private MainActivity mainActivity;
    private final Task task;

    public TaskDialog_delete_OnClickListener(MainActivity mainActivity, Task task) {
        this.mainActivity = mainActivity;
        this.task = task;
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        task.setStatus(TaskStatus.DELETED);
        task.setLastSynchroDate( new Date() );
        TaskDatasource ds = new TaskDatasource(mainActivity);
            ds.open();
            ds.save(task);
            ds.close();
               mainActivity.refreshTasks();

    }
}
