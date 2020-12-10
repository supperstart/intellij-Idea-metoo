package com.metoo.app.pojo;

public class Ddu {

	private String status;
	private String time;	
	private String location;
	private String details;
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Ddu() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Ddu(String location, String details, String time, String status) {
		super();
		this.location = location;
		this.details = details;
		this.time = time;
		this.status = status;
	}
	@Override
	public String toString() {
		return "Ddu [location=" + location + ", details=" + details + ", time=" + time + ", status=" + status + "]";
	}
	
	
	
}
