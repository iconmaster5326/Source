package com.iconmaster.source.compile;

/**
 *
 * @author iconmaster
 */
public class NameProvider {
	private static int lastName = -1;
	
	public static final NameProvider instance = new NameProvider();
	
	public String getTempName() {
		lastName++;
		return "%"+Integer.toString(lastName);
	}
}
