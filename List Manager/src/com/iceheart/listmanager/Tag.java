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
    private int iconId = -1;
    private TagType type = TagType.USER_DEFINED;
    
    /**
     * Constructor to build a System predefined list.
     */
    public Tag( TagType type ) {
    	this.type = type;
    	
    	switch ( type ) {
    		case  SYSTEM_ALL:
    			// TODO Resource label
    			this.name = "ALL";
    		    iconId =  R.drawable.ic_tag_all;
    			break;
    		case  SYSTEM_COMING_SOON:
    			// TODO Resource label
    			this.name = "Coming Soon";
                iconId = R.drawable.ic_coming_soon;
                break;
    		case  SYSTEM_NEW_TAG:
    			// TODO Resource label
    			this.name = "New Tag";
                iconId = R.drawable.ic_tag_add;
                break;
                
            default:
            	throw new IllegalStateException( "Please use the other constructor to build a user-defined list." );

    			
    	}
    }

    /**
     * Constructor to build a user-defined list.
     * @param name
     */
    public Tag( String name ) {
		this.name = name;
		this.type = TagType.USER_DEFINED;
        iconId = R.drawable.ic_tag_add;
	}
	
	public Tag() {
        iconId = R.drawable.ic_tag;
		this.type = TagType.USER_DEFINED;
	}

    public Tag(String name, int iconId ) {
        this.name = name;
        this.iconId = iconId;
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

    public int getIconId() {
        return iconId;
    }

    public void setIconId( int resourceId ) {
        this.iconId = resourceId;
    }

	public TagType getType() {
		return type;
	}

	public void setType(TagType type) {
		this.type = type;
	}
}
