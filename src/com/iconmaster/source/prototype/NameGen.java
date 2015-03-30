package com.iconmaster.source.prototype;

/**
 *
 * @author iconmaster
 */
public class NameGen {
	public static int i = 0;
	
	public static String name() {
		return "%"+(i++);
	}
}
