package com.iconmaster.source.validate;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.tokenize.CompoundTokenRule;
import com.iconmaster.source.tokenize.TokenRule;
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
		ArrayList<SourceException> a = new ArrayList<>();
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case NUMBER:
					ensureScope(a,e,scope,Scope.RVALUE);
					break;
				case STRING:
					ensureScope(a,e,scope,Scope.RVALUE);
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
		} else if (e.type instanceof CompoundTokenRule) {
			switch ((CompoundTokenRule)e.type) {
				case BLOCK:
					ensureScope(a,e,scope,Scope.CODE);
					a.addAll(validate((ArrayList<Element>) e.args[0],Scope.CODE));
					break;
				case INDEX:
					return validate((ArrayList<Element>) e.args[0],Scope.RVALUE);
				case PAREN:
					return validate((ArrayList<Element>) e.args[0],Scope.RVALUE);
			}
		} else if (e.type instanceof Rule) {
			switch ((Rule)e.type) {
				//operator level
				case TRUE:
				case FALSE:
				case NOT:
				case BIT_NOT:
				case POW:
				case NEG:
				case MUL:
				case DIV:
				case MOD:
				case ADD:
				case SUB:
				case BIT_AND:
				case BIT_OR:
				case EQ:
				case LT:
				case GT:
				case LTE:
				case GTE:
				case AND:
				case OR:
					ensureScope(a,e,scope,Scope.RVALUE);
					break;
					
				//struct level
					
				//code level
				case LOCAL:
				case LOCAL_ASN:
				case ASSIGN:
				case IF:
				case ELSEIF:
				case ELSE:
				case FOR:
				case WHILE:
				case REPEAT:
				case RETURN:
				case RETURN_NULL:
				case BREAK:
					ensureScope(a,e,scope,Scope.CODE);
					break;
					
				//global level
				case STRUCT:
				case STRUCT_EXT:
				case ENUM:
				case PACKAGE:
				case IMPORT:
					ensureScope(a,e,scope,Scope.GLOBAL);
					break;
				
				//non-code level
				case FUNC:
				case ITERATOR:
				case FIELD:
				case FIELD_ASN:
					ensureScope(a,e,scope,Scope.GLOBAL,Scope.STRUCT);
					break;
					
				//always OK
				case GLOBAL_DIR:
					break;
					
				//always not OK
				case EXTEND:
					a.add(genScopeError(e,scope));
					break;
				
				//other cases

				case FCALL:
					break;
				case ICALL:
					break;
				case CHAIN:
					break;
				case TUPLE:
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
