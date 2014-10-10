package com.iconmaster.source.compile;

/**
 *
 * @author iconmaster
 */
public class NameProvider {
	private int lastName = -1;
	
	public String getNewName() {
		lastName++;
		return "TMP_"+Integer.toString(lastName);
	}
}
