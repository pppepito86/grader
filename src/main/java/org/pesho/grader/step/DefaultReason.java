package org.pesho.grader.step;

public class DefaultReason implements Reason {

	private String reason;
	
	public DefaultReason(String reason) {
		this.reason = reason;
	}
	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
