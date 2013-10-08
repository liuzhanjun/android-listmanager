package com.iceheart.listmanager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Task implements Serializable {
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy/MM/dd", Locale.CANADA_FRENCH );
	public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss", Locale.CANADA_FRENCH );
	public static final SimpleDateFormat FUNCTIONALID_FORMAT = new SimpleDateFormat( "yyyyMMddhhmmss", Locale.CANADA_FRENCH );
	
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
	private List<String> tags = new ArrayList<String>();
	private TaskStatus status = TaskStatus.ACTIVE;
	
	public String getFunctionalId() {
		String functionalId = name;
		if ( getCreationDate() != null ) {
			functionalId += "|" + FUNCTIONALID_FORMAT.format( getCreationDate() );
		}
		return functionalId;
	} 
	
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
		this.estimatedPrice = estimatedPrice;
	}
	public BigDecimal getRealPrice() {
		return realPrice;
	}
	public void setRealPrice(BigDecimal realPrice) {
		this.realPrice = realPrice;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public Map<String, String> toMap() {
		Map<String,String> map = new HashMap<String,String>();
		map.put( "id", id.toString());
		map.put( "name", name);
		// TODO: Format price with 2 decimal ?
		map.put( "price", estimatedPrice == null ? "": estimatedPrice.toString() +" $" );
		map.put( "dueDate", dueDate == null ? "": DATE_FORMAT.format(dueDate) );
		return map;
	}
	
	
	public String getTagsAsString() {
		StringBuilder builder = new StringBuilder();
		if ( getTags() != null ) {
			for (  String tag: getTags() ) {
				if ( builder.length() > 0 ) {
					builder.append( "," );
				}
				builder.append( tag );
			}
		}
		return builder.toString();
		
	}
	
	public void setTags(String value) {
		if ( value == null ) {
			tags = new ArrayList<String>();
		} else {
			String[] values = value.split( ",");
			tags = Arrays.asList( values );
			
		}
		
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

		if ( !compareFields( taskToCompare.getTagsAsString(), getTagsAsString() ) ) {
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

}
