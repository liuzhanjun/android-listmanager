package com.iceheart.listmanager.tasklist;

import com.iceheart.listmanager.R;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskList {
	
	private Long id;
	
	private String name;
	
	private Date lastSynchroDate;	
	
	private TaskListStatus status = TaskListStatus.ACTIVE;
	
	
	/**
	 * Tansient field to store the number of task for this list.
	 */
	private int taskCount = 0;
    private int iconId = -1;
    private TaskListType type = TaskListType.USER_DEFINED;
    private int listColor = android.R.color.transparent;

    /**
     * Constructor to build a System predefined list.
     */
    public TaskList( TaskListType type ) {
    	this.type = type;
    	
    	switch ( type ) {
    		case  SYSTEM_ALL:
    			// TODO Resource label
    			this.name = "ALL";
    		    iconId =  R.drawable.ic_tag_all;
    			break;
    		case  SYSTEM_NEW_LIST:
    			// TODO Resource label
    			this.name = "New List";
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
    public TaskList( String name ) {
		this.name = name;
		this.type = TaskListType.USER_DEFINED;
        this.iconId = R.drawable.ic_tag_add;
        this.listColor = generateColorId(name);
    }
	
	public TaskList() {
        this.type = TaskListType.USER_DEFINED;
        this.iconId = R.drawable.ic_tag;
        this.listColor = android.R.color.transparent;
	}

    public TaskList(String name, int iconId ) {
        this.name = name;
        this.iconId = iconId;

        this.listColor = generateColorId(name);
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {

        this.name = name;
        this.listColor = generateColorId(name);
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
		if ( !( o instanceof TaskList )  ) {
			return false;
		}
		TaskList tag = (TaskList) o;
		return tag.getName().equals( this.getName() );
		
	}

	public Map<String, Object> toMap() {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put( "name", name);
		if ( getTaskCount() > 0 ) {
			map.put( "taskCount", String.valueOf( getTaskCount() ));
		} else if ( getType() == TaskListType.USER_DEFINED ) {
            map.put( "taskCount", String.valueOf( 0 ));
        }
		
		map.put( "list", this );
		return map;
	}

	public TaskListStatus getStatus() {
		return status;
	}

	public void setStatus(TaskListStatus status) {
		this.status = status;
	}

    public int getIconId() {
        return iconId;
    }

    public void setIconId( int resourceId ) {
        this.iconId = resourceId;
    }

	public TaskListType getType() {
		return type;
	}

	public void setType(TaskListType type) {
		this.type = type;
	}

    public int getTagColor() {
        return listColor;
    }

    private int generateColorId(String name) {
        Character c = name.charAt(0);

        return colors[ c.hashCode() % colors.length];
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private static int[] colors = new int [] {
            android.R.color.holo_orange_dark,
            android.R.color.holo_orange_light,
            android.R.color.holo_blue_dark,
            android.R.color.holo_blue_light,
            android.R.color.holo_blue_bright,
            android.R.color.holo_purple,
            android.R.color.holo_red_dark,
            android.R.color.holo_red_light,
            android.R.color.holo_green_dark,
            android.R.color.holo_green_light,
            android.R.color.darker_gray,
            android.R.color.black
    };
}
