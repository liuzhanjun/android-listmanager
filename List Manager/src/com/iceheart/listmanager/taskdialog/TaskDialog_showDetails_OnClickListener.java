package com.iceheart.listmanager.taskdialog;

import android.content.DialogInterface;
import android.content.Intent;

import com.iceheart.listmanager.AddTaskActivity;
import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.task.Task;

/**
* Created by nmasse on 11/19/13.
*/
public class TaskDialog_showDetails_OnClickListener implements DialogInterface.OnClickListener {
    private MainActivity mainActivity;
    private final Task task;

    public TaskDialog_showDetails_OnClickListener(MainActivity mainActivity, Task task) {
        this.mainActivity = mainActivity;
        this.task = task;
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        Intent intent = new Intent(mainActivity, AddTaskActivity.class);
        intent.putExtra("task", task);
        mainActivity.startActivity(intent);
    }
}
