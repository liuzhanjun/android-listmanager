package com.iceheart.listmanager.task;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task implements Serializable {
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy/MM/dd", Locale.CANADA_FRENCH );
	public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss", Locale.CANADA_FRENCH );
	public static final SimpleDateFormat FUNCTIONALID_FORMAT = new SimpleDateFormat( "yyyyMMddhhmmss", Locale.CANADA_FRENCH );
	
	private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String name;
	private String notes;
	private Date dueDate;
	private Date completedDate;
	private Date creationDate;
	private Date lastSynchroDate;
	private BigDecimal estimatedPrice;
	private BigDecimal realPrice;
	private Long listId = null;
	private TaskStatus status = TaskStatus.ACTIVE;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public Date getCompletedDate() {
		return completedDate;
	}
	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
	}
	public BigDecimal getEstimatedPrice() {
		return estimatedPrice;
	}
	public void setEstimatedPrice(BigDecimal estimatedPrice) {
        if ( estimatedPrice != null ) {
            this.estimatedPrice = estimatedPrice.setScale(2);
        } else {
		    this.estimatedPrice = null;
        }
	}
	public BigDecimal getRealPrice() {
		return realPrice;
	}
	public void setRealPrice(BigDecimal realPrice) {
		this.realPrice = realPrice;
	}
	public Long getListId() {
		return listId;
	}
	public void setListId(Long listId) {
		this.listId = listId;
	}
	
	public String getFormattedDueDate() {
		if ( dueDate == null ) {
			return "";
		}
		
		 String formattedDate = DATE_FORMAT.format(dueDate);
		 
		 if ( isCompleted() ) {
			 return formattedDate;
		 }

		long today = System.currentTimeMillis();
		long dueDateTime = dueDate.getTime();
		long diff = dueDateTime - today;
		
		if ( diff < 0 && diff > -(ONE_DAY_MS) ) {
			// TODO: String resource
			return formattedDate + " (Today)";
		}
		
		if ( diff < 0 && diff > -(ONE_DAY_MS*2) ) {
			// TODO: String resource
			return formattedDate + " (Yesterday)";
		}

		if ( diff > 0 && diff < (ONE_DAY_MS) ) {
			// TODO: String resource
			return formattedDate + " (Tomorrow)";

		}
		
		return formattedDate;
		
		 
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getLastSynchroDate() {
		return lastSynchroDate;
	}
	public void setLastSynchroDate(Date lastSynchroDate) {
		this.lastSynchroDate = lastSynchroDate;
	}
	
	@Override
	public boolean equals( Object object ) {
		if (!( object instanceof Task)) {
			return false;
		}
		
		Task taskToCompare = (Task) object;
		if ( !compareFields( taskToCompare.getName(), getName() ) ) {
			return false;
		}
		
		if ( !compareFields( taskToCompare.getNotes(), getNotes() ) ) {
			return false;
		}

		if ( !compareFields( taskToCompare.getListId(), getListId() ) ) {
			return false;
		}

		if ( !compareFields( taskToCompare.getCompletedDate(), getCompletedDate() ) ) {
			return false;
		}

		if ( !compareFields( taskToCompare.getCreationDate(), getCreationDate() ) ) {
			return false;
		}

		if ( !compareFields( taskToCompare.getDueDate(), getDueDate() ) ) {
			return false;
		}

		if ( !compareFields( taskToCompare.getEstimatedPrice(), getEstimatedPrice() ) ) {
			return false;
		}

		if ( !compareFields( taskToCompare.getRealPrice(), getRealPrice() ) ) {
			return false;
		}
		
		return true;
	}
	private boolean compareFields(Object value1, Object value2) {
		
		if ( value1 == value2 ) {
			return true;
		}
		
		if ( value1 == null && value2 != null ) {
			return false;
		}
		
		if ( value2 == null && value1 != null ) {
			return false;
		}
		
		return value1.equals( value2 );
		
		
		
	}

	public TaskStatus getStatus() {
		return status;
	}
	
	public void setStatus(TaskStatus status) {
		this.status = status;
	}
	
	public void setDueDate(String value) {
		try {
			setDueDate( DATE_FORMAT.parse( value ) ); 
		} catch ( Exception e ) {
			setDueDate( (Date) null );
		}
		
	}
	
	public void setCompletedDate(String value) {
		try {
			setCompletedDate( DATE_FORMAT.parse( value ) ); 
		} catch ( Exception e ) {
			setCompletedDate( (Date) null );
		}
		
	}
	
	public void setCreationDate(String value) {
		try {
			setCreationDate( DATETIME_FORMAT.parse( value ) ); 
		} catch ( Exception e ) {
			setCreationDate( (Date) null );
		}
		
	}	
	

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public void setEstimatedPrice(String value) {
		BigDecimal bd = null;
		try {
			bd = new BigDecimal( value );
		} catch ( Exception e ) {
		}
		setEstimatedPrice( bd );
		
	}

	public void setRealPrice(String value) {
		BigDecimal bd = null;
		try {
			bd = new BigDecimal( value );
		} catch ( Exception e ) {
		}
		setRealPrice( bd );
	}

    public boolean isCompleted() {
    	return completedDate != null;
    }
    
	public boolean isComingSoon() {
		if ( isCompleted() || dueDate == null ) {
			return false;
		}
		
		long today = System.currentTimeMillis();
		long dueDateTime = dueDate.getTime();
		long diff = dueDateTime - today;
		return diff < 7 * 24 * 60 * 60 * 1000;
	}
}
