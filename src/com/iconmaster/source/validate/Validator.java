package com.iconmaster.source.validate;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.tokenize.TokenRule;
import com.iconmaster.source.util.Debug;
import com.iconmaster.source.util.Directives;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Validator {
	public static enum Scope {
		GLOBAL,STRUCT,CODE,LVALUE,RVALUE
	}
	
	public static ArrayList<SourceException> validate(ArrayList<Element> input) {
		return validate(input,Scope.GLOBAL);
	}
	
	public static ArrayList<SourceException> validate(ArrayList<Element> input, Scope scope) {
		ArrayList<SourceException> a = new ArrayList<>();
		for (Element e : input) {
			ArrayList<SourceException> err = validateElement(e,scope);
				a.addAll(err);
		}
		return a;
	}
	
	public static ArrayList<SourceException> validateElement(Element e, Scope scope) {
		Debug.println("Validating "+e+" in scope "+scope);
		ArrayList<SourceException> a = new ArrayList<>();
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case NUMBER:
					ensureScope(a,e,scope,Scope.RVALUE);
					break;
				case STRING:
					if (!Directives.has(e, "lang")) {
						ensureScope(a,e,scope,Scope.RVALUE);
					}
					break;
				case RESWORD:
					a.add(new SourceException(e.range,"invalid keyword "+e.args[0]));
					break;
				case WORD:
					ensureScope(a,e,scope,Scope.LVALUE,Scope.RVALUE);
					break;
				case SYMBOL:
					a.add(new SourceException(e.range,"invalid symbol "+e.args[0]));
					break;
				default:
					a.add(new SourceException(e.range,"invalid token "+e.args[0]));
					break;
			}
		} else if (e.type instanceof Rule) {
			switch ((Rule)e.type) {
				//blocks
				case CODE:
					ensureScope(a,e,scope,Scope.CODE);
					a.addAll(validate((ArrayList<Element>) e.args[0],Scope.CODE));
					break;
				case INDEX:
					ensureScope(a,e,scope,Scope.RVALUE);
					a.addAll(validate((ArrayList<Element>) e.args[0],Scope.RVALUE));
					break;
				case PAREN:
					ensureScope(a,e,scope,Scope.CODE,Scope.RVALUE,Scope.LVALUE);
					a.addAll(validate((ArrayList<Element>) e.args[0],Scope.RVALUE));
					break;
					
				//operator level
				case TRUE:
				case FALSE:
					ensureScope(a,e,scope,Scope.RVALUE);
					break;
				case NOT:
				case BIT_NOT:
				case NEG:
					ensureScope(a,e,scope,Scope.RVALUE);
					a.addAll(validateElement((Element) e.args[0],Scope.RVALUE));
					break;
				case POW:
				case MUL:
				case DIV:
				case MOD:
				case ADD:
				case SUB:
				case BIT_AND:
				case BIT_OR:
				case EQ:
				case NEQ:
				case LT:
				case GT:
				case LTE:
				case GTE:
				case AND:
				case TO:
				case OR:
				case CONCAT:
					ensureScope(a,e,scope,Scope.RVALUE);
					a.addAll(validateElement((Element) e.args[0],Scope.RVALUE));
					a.addAll(validateElement((Element) e.args[1],Scope.RVALUE));
					break;
					
				//struct level
					
				//code level
				case LOCAL_ASN:
					a.addAll(validate((ArrayList<Element>) e.args[1],Scope.RVALUE));
				case LOCAL:
					ensureScope(a,e,scope,Scope.CODE);
					a.addAll(validate((ArrayList<Element>) e.args[0],Scope.LVALUE));
					break;
				case ADD_ASN:
				case SUB_ASN:
				case MUL_ASN:
				case DIV_ASN:
					ensureScope(a,e,scope,Scope.CODE);
					a.addAll(validateElement((Element) e.args[0],Scope.LVALUE));
					a.addAll(validateElement((Element) e.args[1],Scope.RVALUE));
					break;
				case ASSIGN:
					ensureScope(a,e,scope,Scope.CODE);
					a.addAll(validate((ArrayList<Element>) e.args[0],Scope.LVALUE));
					a.addAll(validate((ArrayList<Element>) e.args[1],Scope.RVALUE));
					break;
				case IFBLOCK:
					ensureScope(a,e,scope,Scope.CODE);
					Element e3 = (Element) e.args[0];
					a.addAll(validateElement((Element) e3.args[0],Scope.RVALUE));
					a.addAll(validate((ArrayList<Element>) e3.args[2],Scope.CODE));
					e3 = (Element) e.args[2];
					if (e3!=null) {
						a.addAll(validate((ArrayList<Element>) e3.args[2],Scope.CODE));
					}
					for (Element e4 : (ArrayList<Element>) e.args[1]) {
						a.addAll(validateElement((Element) e4.args[0],Scope.RVALUE));
						a.addAll(validate((ArrayList<Element>) e4.args[2], Scope.CODE));
					}
					break;
				case WHILE:
				case REPEAT:
					ensureScope(a,e,scope,Scope.CODE);
					a.addAll(validateElement((Element) e.args[0],Scope.RVALUE));
					a.addAll(validate((ArrayList<Element>) e.args[2],Scope.CODE));
					break;
				case FOR:
					ensureScope(a,e,scope,Scope.CODE);
					a.addAll(validate((ArrayList<Element>) e.args[0],Scope.LVALUE));
					a.addAll(validate((ArrayList<Element>) e.args[1],Scope.RVALUE));
					a.addAll(validate((ArrayList<Element>) e.args[2],Scope.CODE));
					break;
				case RETURN:
					a.addAll(validateElement((Element) e.args[0],Scope.RVALUE));
				case RETURN_NULL:
				case BREAK:
					ensureScope(a,e,scope,Scope.CODE);
					break;
					
				//global level
				case STRUCT_EXT:
					a.addAll(validateElement((Element) e.args[1],Scope.LVALUE));
				case STRUCT:
					ensureScope(a,e,scope,Scope.GLOBAL);
					a.addAll(validateElement((Element) e.args[0],Scope.LVALUE));
					a.addAll(validate((ArrayList<Element>) e.args[2],Scope.STRUCT));
					break;
				case ENUM:
					ensureScope(a,e,scope,Scope.GLOBAL);
					a.addAll(validate((ArrayList<Element>) e.args[1],Scope.LVALUE));
					break;
				case PACKAGE:
				case IMPORT:
					ensureScope(a,e,scope,Scope.GLOBAL);
					a.addAll(validateElement((Element) e.args[0],Scope.LVALUE));
					break;
				
				//non-code level
				case FUNC:
				case ITERATOR:
					if (e.args[3]!=null) {
						a.addAll(validate((ArrayList<Element>) e.args[3],Scope.LVALUE));
					}
					ensureScope(a,e,scope,Scope.GLOBAL,Scope.STRUCT);
					a.addAll(validate((ArrayList<Element>) e.args[2],Scope.CODE));
					break;
				case FIELD_ASN:
					a.addAll(validate((ArrayList<Element>) e.args[1],Scope.RVALUE));
				case FIELD:
					ensureScope(a,e,scope,Scope.GLOBAL,Scope.STRUCT);
					a.addAll(validate((ArrayList<Element>) e.args[0],Scope.LVALUE));
					break;
					
				//always OK
				case GLOBAL_DIR:
					break;
					
				//always not OK
				case IF:
				case ELSE:
				case ELSEIF:
				case EXTEND:
					a.add(genScopeError(e,scope));
					break;
				
				//other cases

				case REF_CALL:
				case FCALL:
					if (e.args[2]!=null) {
						a.addAll(validate((ArrayList<Element>) e.args[2],Scope.LVALUE));
					}
					ensureScope(a,e,scope,Scope.RVALUE,Scope.CODE);
					break;
				case ICALL:
					ensureScope(a,e,scope,Scope.RVALUE,Scope.LVALUE);
					break;
				case CHAIN:
					ensureScope(a,e,scope,Scope.CODE,Scope.RVALUE,Scope.LVALUE);
					for (Element e2 : (ArrayList<Element>)e.args[0]) {
						a.addAll(validateElement(e2,Scope.RVALUE));
					}
					break;
				case TUPLE:
					ensureScope(a,e,scope,Scope.RVALUE,Scope.LVALUE);
					for (Element e2 : (ArrayList<Element>)e.args[0]) {
						a.addAll(validateElement(e2,scope));
					}
					break;
				default:
					a.add(genScopeError(e,scope));
			}
		}
		return a;
	}
	
	public static void ensureScope(ArrayList<SourceException> a, Element e, Scope scope, Range range, Scope... valid) {
		boolean matched = false;
		for (Scope canBe : valid) {
			if (canBe==scope) {
				matched = true;
			}
		}
		if (!matched) {
			a.add(genScopeError(e,scope,range));
		}
	}
	
	public static void ensureScope(ArrayList<SourceException> a, Element e, Scope scope, Scope... valid) {
		ensureScope(a,e,scope,e.range,valid);
	}
	
	public static SourceException genScopeError(Element e, Scope scope) {
		return genScopeError(e,scope,e.range);
	}
	
	public static SourceException genScopeError(Element e, Scope scope, Range range) {
		return new SourceException(range,"Element of kind "+e.type+" is not allowed in scope "+scope);
	}
}
