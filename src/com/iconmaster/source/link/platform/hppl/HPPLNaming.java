package com.iconmaster.source.link.platform.hppl;

/**
 *
 * @author iconmaster
 */
public class HPPLNaming {
	public static int varsMade = -1;
	
	public static String getNewName() {
		varsMade++;
		return "%"+varsMade;
	}
	
	public static String formatVarName(String name) {
		name = name.replace(".", "_").replace("?", "_");
		if (name.startsWith("_")) {
			name = "a"+name;
		}
		return name;
	}
	
	public static String formatFuncName(String name) {
		name = name.replace(".", "_").replace("?", "_");
		if (name.startsWith("_")) {
			name = "a"+name;
		}
		return name;
	}
}
