package com.iconmaster.source.util;

/**
 *
 * @author iconmaster
 */
public class Directives {
	public static boolean has(IDirectable obj, String dir) {
		return obj.getDirectives().contains(dir);
	}
}
