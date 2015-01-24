package com.iconmaster.source.link.platform.hppl;

import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class FunctionRegistry {
	public static interface CustomFunction {
		public String assemble();
	}
	
	public static CustomFunction defaultFunction;
	
	public static HashMap<String,CustomFunction> functions;
	
	public static void addFunction(String fn, CustomFunction exec) {
		functions.put(fn, exec);
	}
	
	public static boolean isFunctionCustom(String fn) {
		return functions.containsKey(fn);
	}
	
	public static String assembleFunction(String name) {
		return isFunctionCustom(name) ? functions.get(name).assemble() : defaultFunction.assemble();
	}
	
	public static String assembleDefault(String name) {
		return "";
	}
	
	public static void addSimpleFunction(String fn, String name) {
		addFunction(fn, () -> {
			return assembleDefault(name);
		});
	}
}
