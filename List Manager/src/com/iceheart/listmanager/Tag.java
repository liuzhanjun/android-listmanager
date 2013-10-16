package com.iceheart.listmanager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Tag {
	
	private String name;
	
	private Date lastSynchroDate;	
	
	/**
	 * Transient field for the AddTask screen.
	 */
	private boolean selected = false;
	
	/**
	 * Tansient field to store the number of task for this tag.
	 */
	private int taskCount = 0;
    private int resourceId = -1;

    public Tag( String name ) {
		this.name = name;

        // TODO: something better to associate resource icon to tag type
        if ( name.equalsIgnoreCase( "ALL" ) ) {
            resourceId =  R.drawable.ic_tag_all;
        } else if ( name.equalsIgnoreCase( "Coming Soon" ) ) {
            resourceId = R.drawable.ic_coming_soon;
        } else if ( name.equalsIgnoreCase( "New Tag") ) {
            resourceId = R.drawable.ic_tag_add;
        }
	}

    public Tag( String name, int iconResourceId ) {
        this.name = name;
        this.resourceId = iconResourceId;
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
        // TODO: something better to associate resource icon to tag type
        if ( taskCount > 0 ) {
            resourceId = R.drawable.ic_tag;
        }
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


    public int getIconId() {
        return resourceId;
    }

    public void setIconId( int resourceId ) {
        this.resourceId = resourceId;
    }
}
