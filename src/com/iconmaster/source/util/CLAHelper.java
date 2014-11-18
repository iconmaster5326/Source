package com.iconmaster.source.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class CLAHelper {
	public static class CLA extends HashMap<String,String> {
		public String[] unmatched;
	}
	
	public static CLA getArgs(String[] input) {
		CLA cla = new CLA();
		String flag = null;
		ArrayList<String> a = new ArrayList<>();
		for (String s : input) {
			if (s.startsWith("-")) {
				if (flag!=null) {
					cla.put(flag, "");
				}
				flag = s.substring(1);
			} else {
				if (flag==null) {
					a.add(s);
				} else {
					cla.put(flag, s);
					flag = null;
				}
			}
		}
		if (flag!=null && !cla.containsKey(flag)) {
			cla.put(flag, "");
		}
		cla.unmatched = a.toArray(new String[0]);
		return cla;
	}
}
