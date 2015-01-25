package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;

/**
 *
 * @author iconmaster
 */
public class HPPLNaming {
	public static int varsMade = -1;
	
	public static String getNewName() {
		varsMade++;
		return HPPLCharacters.VAR_BEGIN+new String(new char[] {(char) (0xF000 + varsMade)});
	}
	
	public static String formatVarName(String name) {
		name = name.replace(".", "_").replace("?", "_").replace("%", "_");
		if (name.startsWith("_")) {
			name = HPPLCharacters.VAR_BEGIN+name;
		}
		return name;
	}
	
	public static String formatFuncName(Function fn) {
		String name = fn.getName();
		name = name.replace(".", "_").replace("?", "_");
		if (name.startsWith("_")) {
			name = HPPLCharacters.VAR_BEGIN+name;
		}
		if (fn.order!=0) {
			name += "%" + fn.order;
		}
		return name;
	}
}
