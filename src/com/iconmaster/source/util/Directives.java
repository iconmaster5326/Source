package com.iconmaster.source.util;

import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Directives {
	public static boolean has(IDirectable obj, String dir) {
		boolean neg = false;
		if (dir.startsWith("!")) {
			dir = dir.substring(1);
			neg = true;
		}
		int count = 0;
		for (String dir2 : obj.getDirectives()) {
			if (dir2.startsWith(dir)) {
				count++;
			} else if (dir2.startsWith("!"+dir)) {
				count--;
			}
		}
		return neg?count<=0:count>0;
	}
	
	public static ArrayList<String> getAll(IDirectable obj) {
		return obj.getDirectives();
	}
}
