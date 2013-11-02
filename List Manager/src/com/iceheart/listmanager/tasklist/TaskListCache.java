package com.iceheart.listmanager.tasklist;

import java.util.List;

import android.content.Context;

public class TaskListCache {
	
	private static TaskListCache instance;
	
	private List<TaskList> taskLists;
	private Context context;
	private Long nextId = 1L;
	
	public static TaskListCache getInstance() {
		if ( instance == null ) {
			throw new RuntimeException( "Must be instantiate first with getInstance( context).");
		}
		return instance;
	}
	
	public static TaskListCache getInstance( Context context ) {
		if ( instance == null ) {
			instance= new TaskListCache( context );
		}
		return instance;
	}
	
	private TaskListCache( Context context ) {
		this.context = context;
		refreshCache();
		
	}
	
	public List<TaskList> getTaskLists() {
		return taskLists;
	}
	
	public void refreshCache() {
		TaskListDatasource ds = new TaskListDatasource( context );
        ds.open();
        taskLists =  ds.getAllActive();
        
        long maxId = 0;
        for ( TaskList taskList: taskLists  ) {
        	if ( taskList.getId().longValue() > maxId ) {
        		maxId = taskList.getId();
        	}
        }
        nextId = maxId + 1;
        
        ds.close();		
	}
	
	public TaskList getById( Long id ) {
		for ( TaskList taskList: taskLists ) {
			if ( taskList.getId().equals( id ) ) {
				return taskList;
			}
		}
		return null;
	}

	public TaskList getByName(String listName) {
		for ( TaskList taskList: taskLists ) {
			if ( taskList.getName().equals( listName ) ) {
				return taskList;
			}
		}
		return null;
	} 
	
	public Long getNextId() {
		return nextId++;
	}
	

}
