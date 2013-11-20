package com.iceheart.listmanager.tasklistpager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;
import com.iceheart.listmanager.task.Task;
import com.iceheart.listmanager.task.TaskDatasource;
import com.iceheart.listmanager.tasklist.TaskList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* Created by nmasse on 11/19/13.
*/ // Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class TaskListPagerAdapter extends FragmentStatePagerAdapter {

    private static final int INCOMING 	= 0;
    private static final int ALL 		= 1;
    private static final int COMPLETED 	= 2;

    private final MainActivity mainActivity;
    private TaskList taskList;
    private Map<Integer,ArrayList<Task>> tasks = new HashMap<Integer,ArrayList<Task>>();

    public TaskListPagerAdapter(MainActivity mainActivity, TaskList taskList, FragmentManager fm) {
        super(fm);
        this.mainActivity = mainActivity;
        this.taskList = taskList;
    }


    public CharSequence getMainPageTitle(int position) {
        String title = mainActivity.selectedList.getName();

        ArrayList<Task> tasksToDisplay = getTaskListForPage(position);
        if ( tasksToDisplay.size() > 0 ) {

            title += " (" + tasksToDisplay.size() + " ";
            title += tasksToDisplay.size() > 1? mainActivity.getString(R.string.suffix_items): mainActivity.getString(R.string.suffix_item);
            BigDecimal totalPrice = new BigDecimal( 0 );
            for ( Task task: tasksToDisplay ) {
                if ( task.getEstimatedPrice() != null ) {
                    totalPrice = totalPrice.add( task.getEstimatedPrice() );
                }
            }

            if ( totalPrice.compareTo( BigDecimal.ZERO ) > 0 ) {
                title += " , " + totalPrice + "$";
            }
            title += ")";
        }
        return title;
    }


    @Override
    public Fragment getItem(int position) {
        ArrayList<Task> tasksToDisplay = getTaskListForPage( position );
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putSerializable("tasks", tasksToDisplay);
        fragment.setArguments(args);
        return fragment;
    }


    public ArrayList<Task> getTaskListForPage( int pageIndex ) {
        ArrayList<Task> tasksToDisplay = tasks.get( pageIndex );

        if ( tasksToDisplay == null ) {
            TaskDatasource ds = new TaskDatasource( mainActivity );
            ds.open();

            Long listId = (MainActivity.selectedList != null ) ? MainActivity.selectedList.getId(): null;
            switch( pageIndex ) {
            case INCOMING:
                tasksToDisplay = ds.findIncomingTask( listId );
                break;
            case ALL:
                tasksToDisplay = ds.findActiveTasksForList( MainActivity.selectedList.getId() );
                break;
            case COMPLETED:
                tasksToDisplay = ds.getAllCompletedTasks( listId );
                break;
            }
            tasks.put( pageIndex, tasksToDisplay );
            ds.close();
        }
        return tasksToDisplay;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch( position ) {
            default:
            case 0:
                return mainActivity.getString(R.string.tab_incoming, taskList.getName());
            case 1:
                return mainActivity.getString(R.string.tab_all, taskList.getName());
            case 2:
                return mainActivity.getString(R.string.tab_completed, taskList.getName());
        }
    }

}
