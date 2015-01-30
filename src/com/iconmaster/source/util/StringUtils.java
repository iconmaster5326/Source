package com.iconmaster.source.util;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.tokenize.TokenRule;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class StringUtils {
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
	
	public static String unescape(String s) {
		String out = "";
		boolean esc = false;
		for (char c : s.toCharArray()) {
			if (esc) {
				if (c=='n') {
					out += '\n';
				} else if (c=='t') {
					out += '\t';
				} else {
					out += c;
				}
				esc = false;
			} else {
				if (c=='\\') {
					esc = true;
				} else {
					out += c;
				}
			}
		}
		return out;
	}
	
	public static String mathElementToString(Element e) {
		if (e.type instanceof Rule) {
			switch ((Rule)e.type) {
				case ADD:
					return "_add";
				case SUB:
					return "_sub";
				case MUL:
					return "_mul";
				case DIV:
					return "_div";
				case POW:
					return "_pow";
				case MOD:
					return "_mod";
				case CONCAT:
					return "_concat";
				case EQ:
					return "_eq";
				case NEQ:
					return "_neq";
				case LT:
					return "_lt";
				case GT:
					return "_gt";
				case LTE:
					return "_lte";
				case GTE:
					return "_gte";
				case AND:
					return "_and";
				case OR:
					return "_or";
				case BIT_AND:
					return "_band";
				case BIT_OR:
					return "_bor";
				case SLL:
					return "_sll";
				case SRL:
					return "_srl";
				case SRA:
					return "_sra";
			}
		}
		return null;
	}
}
