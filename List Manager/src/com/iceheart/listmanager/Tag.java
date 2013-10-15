package com.iceheart.listmanager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Tag {
	
	private String name;
	
	private Date lastSynchroDate;	
	
	private TagStatus status = TagStatus.ACTIVE;
	
	/**
	 * Transient field for the AddTask screen.
	 */
	private boolean selected = false;
	
	/**
	 * Tansient field to store the number of task for this tag.
	 */
	private int taskCount = 0;
	
	public Tag( String name ) {
		this.name = name;
	}
	
	public Tag() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Date getLastSynchroDate() {
		return lastSynchroDate;
	}

	public void setLastSynchroDate(Date lastSynchroDate) {
		this.lastSynchroDate = lastSynchroDate;
	}
	
	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( !( o instanceof Tag )  ) {
			return false;
		}
		Tag tag = (Tag) o;
		return tag.getName().equals( this.getName() );
		
	}

	public Map<String, Object> toMap() {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put( "name", name);
		if ( getTaskCount() > 0 ) {
			map.put( "taskCount", String.valueOf( getTaskCount() ));
		}
		
		map.put( "tag", this );
		return map;
	}

	public TagStatus getStatus() {
		return status;
	}

	public void setStatus(TagStatus status) {
		this.status = status;
	}

	
	

}
