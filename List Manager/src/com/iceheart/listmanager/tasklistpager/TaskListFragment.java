package com.iceheart.listmanager.tasklistpager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.iceheart.listmanager.MainActivity;
import com.iceheart.listmanager.R;
import com.iceheart.listmanager.task.Task;
import com.iceheart.listmanager.task.TaskRowAdapter;

import java.util.ArrayList;
import java.util.List;

/**
* Created by nmasse on 11/19/13.
*/ // Instances of this class are fragments representing a single
// object in our collection.
@SuppressLint("ValidFragment")
public class TaskListFragment extends Fragment {

    public TaskListFragment() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final ListView listView = (ListView) inflater.inflate( R.layout.task_list, container, false );

        // Add the click listener to view/edit the taks
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                    Task selectedTask = (Task) (listView.getItemAtPosition(myItemInt));
                    ((MainActivity)getActivity()).showTaskDialog(selectedTask);

                }
            });
        }

        // Convert the allTasksForTaskList to maps for the list view
        List<Task> adapterList = new ArrayList<Task>();
        for ( Task task: (ArrayList<Task>)getArguments().getSerializable("tasks") ) {
            adapterList.add(task);
        }

        // Create the adapter for the list view
        TaskRowAdapter adapter = new TaskRowAdapter(((MainActivity)getActivity()), adapterList);

        listView.setAdapter( adapter );

        return listView;

    }

}
