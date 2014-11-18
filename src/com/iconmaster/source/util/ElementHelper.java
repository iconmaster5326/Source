package com.iconmaster.source.util;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.tokenize.TokenRule;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class ElementHelper {
	public static String nameString(Element e) {
		if (e==null) {
			return null;
		} else if (e.type==TokenRule.WORD || e.type==TokenRule.STRING) {
			return (String) e.args[0];
		} else if (e.type == Rule.CHAIN) {
			String str = null;
			for (Element e2 : (ArrayList<Element>)e.args[0]) {
				if (e2.type==TokenRule.WORD) {
					if (str==null) {
						str = (String) e2.args[0];
					} else {
						str += "." + (String) e2.args[0];
					}
				}
			}
			return str;
		}
		return null;
	}
	
	public static boolean isReal(String n) {
		return n.contains(".");
	}
}
