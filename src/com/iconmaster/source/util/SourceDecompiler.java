package com.iconmaster.source.util;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.tokenize.TokenRule;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class SourceDecompiler {
	public static String elementsToString(ArrayList<Element> es) {
		StringBuilder sb = new StringBuilder();
		for (Element e : es) {
			sb.append(elementToString(e));
		}
		return sb.toString();
	}
	
	public static String elementToString(Element e) {
		StringBuilder sb = new StringBuilder();
		for (String dir : e.directives) {
			sb.append("@");
			sb.append(dir);
			sb.append("\n");
		}
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case COMMENT:
					sb.append("//");
					sb.append(e.args[0]);
					sb.append("\n");
					break;
				case DIRECTIVE:
					sb.append("@");
					sb.append(e.args[0]);
					break;
				case STRING:
					sb.append("\"");
					sb.append(e.args[0]);
					sb.append("\"");
					break;
				case NUMBER:
				case RESWORD:
				case SYMBOL:
				case WORD:
					sb.append(e.args[0]);
					break;
			}
		} else {
			switch ((Rule)e.type) {
				case PAREN:
					sb.append("(");
					sb.append(elementsToString((ArrayList<Element>) e.args[0]));
					sb.append(")");
					break;
				case INDEX:
					sb.append("[");
					sb.append(elementsToString((ArrayList<Element>) e.args[0]));
					sb.append("]");
					break;
				case CODE:
					sb.append("{\n\t");
					sb.append(elementsToString((ArrayList<Element>) e.args[0]).replace("\n", "\n\t"));
					sb.append("}\n");
					break;
				case GLOBAL_DIR:
					sb.append("@@");
					sb.append(e.args[0]);
					break;
				case FCALL:
					sb.append(e.args[0]);
					sb.append("(");
					sb.append(elementsToString((ArrayList<Element>) e.args[0]));
					sb.append(")");
					break;
				case ICALL:
					sb.append(e.args[0]);
					sb.append("[");
					sb.append(elementsToString((ArrayList<Element>) e.args[1]));
					sb.append("]");
					break;
				case TO:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" to ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case EXTEND:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" extends ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case CHAIN:
					for (Element e2 :(ArrayList<Element>)e.args[0]) {
						sb.append(elementToString(e2));
						sb.append(".");
					}
					sb.deleteCharAt(sb.length()-1);
					break;
				case TRUE:
					sb.append("true");
					break;
				case FALSE:
					sb.append("false");
					break;
				case NOT:
					sb.append("not ");
					sb.append(elementToString((Element) e.args[0]));
					break;
				case BIT_NOT:
					sb.append("!");
					sb.append(elementToString((Element) e.args[0]));
					break;
				case POW:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("^");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case NEG:
					sb.append("-");
					sb.append(elementToString((Element) e.args[0]));
					break;
				case MUL:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("*");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case DIV:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("/");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case MOD:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("%");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case ADD:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" + ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case SUB:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("-");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case CONCAT:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("~");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case BIT_AND:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("&");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case BIT_OR:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("|");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case EQ:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("==");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case NEQ:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("!=");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case LT:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("<");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case GT:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(">");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case LTE:
					sb.append(elementToString((Element) e.args[0]));
					sb.append("<=");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case GTE:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(">=");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case AND:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" and ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case OR:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" or ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case TUPLE:
					sb.append(tupleToString((ArrayList<Element>) e.args[0]));
					break;
				case LOCAL:
					sb.append("local ");
					sb.append(tupleToString((ArrayList<Element>) e.args[0]));
					sb.append("\n");
					break;
				case LOCAL_ASN:
					sb.append("local ");
					sb.append(tupleToString((ArrayList<Element>) e.args[0]));
					sb.append(" = ");
					sb.append(tupleToString((ArrayList<Element>) e.args[1]));
					sb.append("\n");
					break;
				case FIELD:
					sb.append("field ");
					sb.append(tupleToString((ArrayList<Element>) e.args[0]));
					sb.append("\n\n");
					break;
				case FIELD_ASN:
					sb.append("field ");
					sb.append(tupleToString((ArrayList<Element>) e.args[0]));
					sb.append(" = ");
					sb.append(tupleToString((ArrayList<Element>) e.args[1]));
					sb.append("\n\n");
					break;
//				case REF_INDEX:
//					break;
//				case REF_ICALL:
//					break;
				case ADD_ASN:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" += ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case SUB_ASN:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" -= ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case MUL_ASN:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" *= ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case DIV_ASN:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" /= ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case ASSIGN:
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" = ");
					sb.append(elementToString((Element) e.args[1]));
					break;
				case IF:
					sb.append("if ");
					sb.append(elementToString((Element) e.args[0]));
					sb.append("{\n\t");
					sb.append(elementsToString((ArrayList<Element>) e.args[2]).replace("\n", "\n\t"));
					sb.append("\n}\n");
					break;
				case ELSEIF:
					sb.append("elseif ");
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" {\n\t");
					sb.append(elementsToString((ArrayList<Element>) e.args[2]).replace("\n", "\n\t"));
					sb.append("\n}\n");
					break;
				case ELSE:
					sb.append("else {\n\t");
					sb.append(elementsToString((ArrayList<Element>) e.args[2]).replace("\n", "\n\t"));
					sb.append("\n}\n");
					break;
				case FOR:
					sb.append("for ");
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" in ");
					sb.append(elementToString((Element) e.args[1]));
					sb.append(" {\n\t");
					sb.append(elementsToString((ArrayList<Element>) e.args[2]).replace("\n", "\n\t"));
					sb.append("\n}\n");
					break;
				case WHILE:
					sb.append("while ");
					sb.append(elementToString((Element) e.args[0]));
					sb.append(" {\n\t");
					sb.append(elementsToString((ArrayList<Element>) e.args[2]).replace("\n", "\n\t"));
					sb.append("\n}\n");
					break;
				case REPEAT:
					sb.append("");
					sb.append("repeat {\n\t");
					sb.append(elementsToString((ArrayList<Element>) e.args[2]).replace("\n", "\n\t"));
					sb.append("\n} until ");
					sb.append(elementToString((Element) e.args[0]));
					sb.append("\n");
				case RETURN_NULL:
					sb.append("return");
					break;
				case RETURN:
					sb.append("return ");
					sb.append(elementToString((Element) e.args[0]));
					sb.append("\n");
					break;
				case BREAK:
					sb.append("break");
					break;
				case PACKAGE:
					sb.append("package ");
					sb.append(elementToString((Element) e.args[0]));
					sb.append("\n\n");
					break;
				case IMPORT:
					sb.append("import ");
					sb.append(elementToString((Element) e.args[0]));
					sb.append("\n\n");
					break;
				case FUNC:
					sb.append("function ");
					sb.append(e.args[0]);
					sb.append("(");
					sb.append(elementsToString((ArrayList<Element>) e.args[1]));
					sb.append(")");
					if (e.dataType!=null) {
						sb.append(" as ").append(elementToString(e.dataType));
					}
					sb.append(" {\n\t");
					sb.append(elementsToString((ArrayList<Element>) e.args[2]).replace("\n", "\n\t"));
					sb.append("\n}\n\n");
					break;
//				case ITERATOR:
//					break;
//				case STRUCT:
//					break;
//				case STRUCT_EXT:
//					break;
//				case ENUM:
//					break;
			}
		}
		if (e.dataType!=null && e.type!=Rule.FUNC) {
			sb.append(" as ").append(elementToString(e.dataType));
		}
		return sb.toString();
	}

	private static String tupleToString(ArrayList<Element> es) {
		StringBuilder sb = new StringBuilder();
		for (Element e2 : es) {
			sb.append(elementToString(e2));
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
}
