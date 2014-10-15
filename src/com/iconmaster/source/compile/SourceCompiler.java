package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.Variable;
import com.iconmaster.source.tokenize.TokenRule;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class SourceCompiler {
	public static void compile(SourcePackage pkg) {
		for (Variable var : pkg.getVariables()) {
			compileVariable(pkg, var);
		}
		for (Function fn : pkg.getFunctions()) {
			compileFunction(pkg,fn);
		}
	}
	
	public static void compileFunction(SourcePackage pkg, Function fn) {
		if (fn.rawData()!=null) {
			ArrayList<Operation> code = compileCode(pkg,fn.rawData());
			fn.setCompiled(code);
		} else {
			fn.setCompiled(null);
		}
	}
	
	public static void compileVariable(SourcePackage pkg, Variable var) {
		if (var.rawData()!=null) {
			Expression expr = compileExpression(pkg, var.getName(), var.rawData());
			var.setCompiled(expr);
		} else {
			var.setCompiled(null);
		}
	}
	
	public static ArrayList<Operation> compileCode(SourcePackage pkg, ArrayList<Element> a) {
		ArrayList<Operation> code = new ArrayList<>();
		for (Element e : a) {
			switch ((Rule)e.type) {
				case LOCAL:
					ArrayList<String> lnames = new ArrayList<>();
					for (Element e2 : (ArrayList<Element>)e.args[0]) {
						lnames.add(resolveLValue(pkg,code,e2));
					}
					code.add(new Operation(OpType.DEF, e.range, lnames.toArray(new String[0])));
					break;
				case LOCAL_ASN:
					lnames = new ArrayList<>();
					for (Element e2 : (ArrayList<Element>)e.args[0]) {
						lnames.add(resolveLValue(pkg,code,e2));
					}
					code.add(new Operation(OpType.DEF, e.range, lnames.toArray(new String[0])));
				case ASSIGN:
					int i = 0;
					ArrayList<Element> vals = (ArrayList<Element>)e.args[1];
					ArrayList<String> names = new ArrayList<>();
					for (Element e2 : (ArrayList<Element>)e.args[0]) {
						if (i<vals.size()) {
							String name = pkg.nameProvider.getTempName();
							names.add(name);
							String var = resolveLValue(pkg,code,e2);
							Expression right = compileExpression(pkg, name, vals.get(i));
							code.addAll(right);
						}
						i++;
					}
					i = 0;
					for (Element e2 : (ArrayList<Element>)e.args[0]) {
						if (i<vals.size()) {
							String name = names.get(i);
							String var = resolveLValue(pkg,code,e2);
							code.add(new Operation(OpType.MOV, e2.range, var,name));
						}
						i++;
					}
					break;
				default:
					code.addAll(compileExpression(pkg,null,e));
			}
		}
		return code;
	}
	
	public static Expression compileExpression(SourcePackage pkg, String retVar, Element e) {
		Expression expr = new Expression();
		if (retVar == null) {
			retVar = pkg.nameProvider.getTempName();
		}
		expr.retVar = retVar;
		
		if (e==null) {
			return expr;
		}
		
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					expr.add(new Operation(OpType.MOV,e.range,retVar,(String)e.args[0]));
					break;
				case NUMBER:
					expr.add(new Operation(OpType.MOVN,e.range,retVar,(String)e.args[0]));
					break;
				case STRING:
					expr.add(new Operation(OpType.MOVS,e.range,retVar,(String)e.args[0]));
					break;
			}
		} else if (e.type instanceof Rule) {
			switch ((Rule)e.type) {
				case ADD:
					String lvar = pkg.nameProvider.getTempName();
					Expression left = compileExpression(pkg, lvar, (Element) e.args[0]);
					String rvar = pkg.nameProvider.getTempName();
					Expression right = compileExpression(pkg, rvar, (Element) e.args[1]);
					expr.addAll(left);
					expr.addAll(right);
					expr.add(new Operation(OpType.ADD,e.range,retVar,lvar,rvar));
					break;
				case FCALL:
					ArrayList<Element> args = ((ArrayList<Element>) e.args[1]);
					String[] opArgs = new String[args.size()+2];
					opArgs[0] = retVar;
					opArgs[1] = (String) e.args[0];
					int i = 2;
					for (Element arg : args) {
						String argName = pkg.nameProvider.getTempName();
						Expression expr2 = compileExpression(pkg, argName, arg);
						expr.addAll(expr2);
						opArgs[i] = argName;
						i++;
					}
					expr.add(new Operation(OpType.CALL,e.range,opArgs));
					break;
				case INDEX:
					ArrayList<String> names = new ArrayList<>();
					if (((ArrayList<Element>) e.args[0]).get(0).type==Rule.TUPLE) {
						for (Element e2 : ((ArrayList<Element>)((Element)((ArrayList<Element>) e.args[0]).get(0)).args[0])) {
							lvar =  pkg.nameProvider.getTempName();
							Expression expr2 = compileExpression(pkg, lvar, e2);
							expr.addAll(expr2);
							names.add(lvar);
						}
					} else {
						lvar =  pkg.nameProvider.getTempName();
						Expression expr2 = compileExpression(pkg, lvar, ((ArrayList<Element>) e.args[0]).get(0));
						expr.addAll(expr2);
						names.add(lvar);
					}

					opArgs = new String[names.size()+1];
					opArgs[0] = retVar;
					i = 1;
					for (String name : names) {
						opArgs[i] = name;
						i++;
					}
					expr.add(new Operation(OpType.MOVL,e.range,opArgs));
					break;
			}
		}
		return expr;
	}
	
	public static String resolveLValue(SourcePackage pkg, ArrayList<Operation> ops, Element e) {
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					return (String) e.args[0];
			}
		} else if (e.type instanceof Rule) {
			switch ((Rule)e.type) {
				
			}
		}
		return null;
	}
}
