package com.iconmaster.source.util;

import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Directives {
	public static boolean has(ArrayList<String> dirs, String dir) {
		boolean neg = false;
		if (dir.startsWith("!")) {
			dir = dir.substring(1);
			neg = true;
		}
		int count = 0;
		for (String dir2 : dirs) {
			if (dir2.startsWith(dir)) {
				count++;
			} else if (dir2.startsWith("!"+dir)) {
				count--;
			}
		}
		return neg?count<0:count>0;
	}
	
	public static boolean has(IDirectable obj, String dir) {
		return has(obj.getDirectives(),dir);
	}
	
	public static ArrayList<String> getAll(IDirectable obj) {
		return obj.getDirectives();
	}
	
	public static ArrayList<String> getValues(IDirectable obj, String dir) {
		return getValues(obj.getDirectives(),dir);
	}
	
	public static ArrayList<String> getValues(ArrayList<String> dirs, String dir) {
		ArrayList<String> a = new ArrayList<>();
		for (String dir2 : dirs) {
			if (dir2.startsWith(dir+"=")) {
				a.add(dir2.substring(dir2.indexOf("=")+1));
			}
		}
		return a;
	}
}
