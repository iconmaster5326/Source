package com.iconmaster.source.compile;

/**
 *
 * @author iconmaster
 */
public class NameProvider {
	private int lastName = -1;
	
	public String getTempName() {
		lastName++;
		return "$TMP"+Integer.toString(lastName);
	}
}
