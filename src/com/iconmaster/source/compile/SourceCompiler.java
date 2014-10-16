package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.Variable;
import com.iconmaster.source.tokenize.TokenRule;
import com.iconmaster.source.util.Directives;
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
				case RETURN:
					String name = pkg.nameProvider.getTempName();
					Expression expr = compileExpression(pkg,name, (Element) e.args[0]);
					code.addAll(expr);
					code.add(new Operation(OpType.RET,e.range,name));
					break;
				case RETURN_NULL:
					code.add(new Operation(OpType.RET,e.range));
					break;
				default:
					code.addAll(compileExpression(pkg,null,e));
			}
		}
		return optimize(pkg, code);
		//return code;
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
			if (OpType.MathToOpType((Rule) e.type)!=null && OpType.MathToOpType((Rule) e.type).isMathOp()) {
				String lvar = pkg.nameProvider.getTempName();
				Expression left = compileExpression(pkg, lvar, (Element) e.args[0]);
				String rvar = pkg.nameProvider.getTempName();
				Expression right = compileExpression(pkg, rvar, (Element) e.args[1]);
				expr.addAll(left);
				expr.addAll(right);
				expr.add(new Operation(OpType.MathToOpType((Rule) e.type),e.range,retVar,lvar,rvar));
			} else {
				switch ((Rule)e.type) {
					case FCALL:
						ArrayList<Element> args = ((ArrayList<Element>) e.args[1]);
						ArrayList<String> opArgs = new ArrayList<>();
						opArgs.add(retVar);
						opArgs.add((String) e.args[0]);
						for (Element arg : args) {
							if (arg.type==Rule.TUPLE) {
								for (Element e3 : (ArrayList<Element>)arg.args[0]) {
									String argName = pkg.nameProvider.getTempName();
									Expression expr3 = compileExpression(pkg, argName, e3);
									expr.addAll(expr3);
									opArgs.add(argName);
								}
							} else {
								String argName = pkg.nameProvider.getTempName();
								Expression expr2 = compileExpression(pkg, argName, arg);
								expr.addAll(expr2);
								opArgs.add(argName);
							}
						}
						if (pkg.getFunction((String) e.args[0])!=null) {
							Function fn = pkg.getFunction((String) e.args[0]);
							if (Directives.has(fn, "inline") && !fn.isLibrary() && opArgs.size()-2==fn.getArguments().size()) {
								for (int i=2;i<opArgs.size();i++) {
									expr.add(new Operation(OpType.MOV,e.range,fn.getArguments().get(i-2).getName(),opArgs.get(i)));
								}
								if (!fn.isCompiled()) {
									compileFunction(pkg,fn);
								}
								expr.addAll(inlineFunc(fn.getCode(),retVar));
								break;
							}
						}
						expr.add(new Operation(OpType.CALL,e.range,opArgs.toArray(new String[] {})));
						break;
					case INDEX:
						ArrayList<String> names = new ArrayList<>();
						if (((ArrayList<Element>) e.args[0]).get(0).type==Rule.TUPLE) {
							for (Element e2 : ((ArrayList<Element>)((Element)((ArrayList<Element>) e.args[0]).get(0)).args[0])) {
								String lvar = pkg.nameProvider.getTempName();
								Expression expr2 = compileExpression(pkg, lvar, e2);
								expr.addAll(expr2);
								names.add(lvar);
							}
						} else {
							String lvar = pkg.nameProvider.getTempName();
							Expression expr2 = compileExpression(pkg, lvar, ((ArrayList<Element>) e.args[0]).get(0));
							expr.addAll(expr2);
							names.add(lvar);
						}

						String[] opArgs2 = new String[names.size()+1];
						opArgs2[0] = retVar;
						int i = 1;
						for (String name : names) {
							opArgs2[i] = name;
							i++;
						}
						expr.add(new Operation(OpType.MOVL,e.range,opArgs2));
						break;
				}
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
	
	public static ArrayList<Operation> inlineFunc(ArrayList<Operation> code, String retVar) {
		ArrayList<Operation> code2 = new ArrayList<>();
		for (Operation op : code) {
			if (op.op==OpType.RET) {
				if (op.args.length>0) {
					code2.add(new Operation(OpType.MOV,op.range,retVar,op.args[0]));
				}
			} else {
				code2.add(op);
			}
		}
		return code2;
	}
	
	public static ArrayList<Operation> optimize(SourcePackage pkg, ArrayList<Operation> code) {
//		int i = 0;
//		ArrayList<Operation> a = (ArrayList<Operation>) code.clone();
//		HashMap<String,String> reps = new HashMap<>();
//		for (Operation op : code) {
//			boolean addIt = true;
//			if (op.op==OpType.MOV) {
//				boolean valid = true;
//				int lval = -1;
//				int rval = -1;
//				for (int j = i;j<code.size();j++) {
//					Operation op2 = code.get(j);
//					if (op2.op.hasLVar() && op.args[0].equals(op2.args[0])) {
//						if (lval!=-1 || op2.op!=OpType.MOV) {
//							valid = false;
//							break;
//						}
//						lval = j;
//					}
//					if (op2.op!=OpType.MOV) {
//						for (String arg : op2.getVarNames()) {
//							if (arg.equals(op.args[0])) {
//								valid = false;
//								break;
//							}
//						}
//					} else {
//						if (op2.args[1].equals(op.args[0])) {
//							if (rval!=-1) {
//								valid = false;
//								break;
//							}
//							rval = j;
//						}
//					}
//				}
//				if (valid && lval!=-1 && rval!=-1) {
//					boolean canInline = true;
//					for (int j = lval+1;j<rval;j++) {
//						Operation op2 = code.get(j);
//						if (op2.op.hasLVar() && op.args[0].equals(op2.args[1])) {
//							canInline = false;
//						}
//					}
//					if (canInline) {
//						addIt = false;
//						reps.put(op.args[0], op.args[1]);
//					}
//				}
//			}
//			if (addIt) {
//				a.add(op);
//			}
//			i++;
//		}
//		System.out.println(reps);
//		for (Operation op : a) {
//			if (op.op==OpType.MOV) {
//				if (reps.containsKey(op.args[0])) {
//					op.args[0] = reps.get(op.args[0]);
//				}
//				if (reps.containsKey(op.args[1])) {
//					op.args[1] = reps.get(op.args[1]);
//				}
//			}
//		}
//		return a;
		return code;
	}
}
