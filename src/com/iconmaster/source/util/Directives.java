package com.iconmaster.source.util;

import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Directives {
	public static boolean has(IDirectable obj, String dir) {
		return obj.getDirectives().contains(dir);
	}
	
	public static ArrayList<String> getAll(IDirectable obj) {
		return obj.getDirectives();
	}
}
