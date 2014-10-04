package com.iconmaster.source.util;

/**
 *
 * @author iconmaster
 */
public class Debug {
	public static boolean debugMode = false;
	
	public static void println(Object o) {
		if (debugMode) {
			System.out.println(o);
		}
	}
	
	public static void print(Object o) {
		if (debugMode) {
			System.out.print(o);
		}
	}
}
