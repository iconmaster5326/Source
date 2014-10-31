package com.iconmaster.source;

/**
 *
 * @author iconmaster
 */
public class ErrorDetails {
	public String errorType;
	public String errorMsg;
	public String phase;

	public ErrorDetails(String errorType, String errorMsg, String phase) {
		this.errorType = errorType;
		this.errorMsg = errorMsg;
		this.phase = phase;
	}
}
