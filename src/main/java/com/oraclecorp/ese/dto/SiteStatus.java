package com.oraclecorp.ese.dto;

/**
 * @author mattulur
 *
 */
public class SiteStatus {
	
	private String adeSite;
	private String labelServer;
	private String labelVolume;
	private long timestamp;
	private int pushStatus;	
	
	public SiteStatus(String adeSite, String labelServer, String labelVolume,int pushStatus) {
		super();
		this.adeSite = adeSite;
		this.labelServer = labelServer;
		this.labelVolume = labelVolume;
		this.pushStatus = pushStatus;
		this.timestamp = 0;
		
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getAdeSite() {
		return adeSite;
	}
	public void setAdeSite(String adeSite) {
		this.adeSite = adeSite;
	}
	public String getLabelServer() {
		return labelServer;
	}
	public void setLabelServer(String labelServer) {
		this.labelServer = labelServer;
	}
	public String getLabelVolume() {
		return labelVolume;
	}
	public void setLabelVolume(String labelVolume) {
		this.labelVolume = labelVolume;
	}
	public int getPushStatus() {
		return pushStatus;
	}
	public void setPushStatus(int pushStatus) {
		this.pushStatus = pushStatus;
	}	
}