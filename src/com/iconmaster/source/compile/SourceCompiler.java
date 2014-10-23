package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
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
	public static ArrayList<SourceException> compile(SourcePackage pkg) {
		ArrayList<SourceException> errs = new ArrayList<>();
		for (Variable var : pkg.getVariables()) {
			errs.addAll(compileVariable(pkg, var));
		}
		for (Function fn : pkg.getFunctions()) {
			errs.addAll(compileFunction(pkg,fn));
		}
		return errs;
	}
	
	public static ArrayList<SourceException> compileFunction(SourcePackage pkg, Function fn) {
		ArrayList<SourceException> errs = new ArrayList<>();
		if (fn.rawData()!=null) {
			ArrayList<Operation> code = null;
			try {
				code = compileCode(pkg,fn.rawData());
			} catch (SourceException ex) {
				errs.add(ex);
			}
			fn.setCompiled(code);
		} else {
			fn.setCompiled(null);
		}
		return errs;
	}
	
	public static ArrayList<SourceException> compileVariable(SourcePackage pkg, Variable var) {
		ArrayList<SourceException> errs = new ArrayList<>();
		if (var.rawData()!=null) {
			Expression expr = null;
			try {
				expr = compileExpression(pkg, var.getName(), var.rawData());
			} catch (SourceException ex) {
				errs.add(ex);
			}
			var.setCompiled(expr);
		} else {
			var.setCompiled(null);
		}
		return errs;
	}
	
	public static ArrayList<Operation> compileCode(SourcePackage pkg, ArrayList<Element> a) throws SourceException {
		ArrayList<Operation> code = new ArrayList<>();
		OpType asnType = null; //a temp vairable for +=, etc.
		for (Element e : a) {
			switch ((Rule)e.type) {
				case LOCAL:
					ArrayList<String> lnames = new ArrayList<>();
					for (Element e2 : (ArrayList<Element>)e.args[0]) {
						lnames.add(resolveLValue(pkg,code,e2));
					}
					code.add(new Operation(OpType.DEF, e.range, lnames.toArray(new String[0])));
					if (!Directives.getAll(e).isEmpty()) {
						for (String name : lnames) {
							ArrayList<String> pa = (ArrayList<String>) Directives.getAll(e).clone();
							pa.add(0, name);
							code.add(new Operation(OpType.PROP, e.range, pa.toArray(new String[0])));
						}
					}
					break;
				case LOCAL_ASN:
					lnames = new ArrayList<>();
					for (Element e2 : (ArrayList<Element>)e.args[0]) {
						lnames.add(resolveLValue(pkg,code,e2));
					}
					code.add(new Operation(OpType.DEF, e.range, lnames.toArray(new String[0])));
					if (!Directives.getAll(e).isEmpty()) {
						for (String name : lnames) {
							ArrayList<String> pa = (ArrayList<String>) Directives.getAll(e).clone();
							pa.add(0, name);
							code.add(new Operation(OpType.PROP, e.range, pa.toArray(new String[0])));
						}
					}
				case ASSIGN:
					int i = 0;
					ArrayList<Element> vals = (ArrayList<Element>)e.args[1];
					ArrayList<String> names = new ArrayList<>();
					for (Element e2 : (ArrayList<Element>)e.args[0]) {
						if (i<vals.size()) {
							String name = pkg.nameProvider.getTempName();
							names.add(name);
							String var = resolveLValue(pkg,code,e2);
							if (!Directives.getAll(e).isEmpty()) {
								ArrayList<String> pa = (ArrayList<String>) Directives.getAll(e).clone();
								pa.add(0, name);
								code.add(new Operation(OpType.PROP, e.range, pa.toArray(new String[0])));
							}
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
				case ADD_ASN:
					if (e.type == Rule.ADD_ASN) {
						asnType = OpType.ADD;
					}
				case SUB_ASN:
					if (e.type == Rule.SUB_ASN) {
						asnType = OpType.SUB;
					}
				case MUL_ASN:
					if (e.type == Rule.MUL_ASN) {
						asnType = OpType.MUL;
					}
				case DIV_ASN:
					if (e.type == Rule.DIV_ASN) {
						asnType = OpType.DIV;
					}
					String var = resolveLValue(pkg, code, (Element) e.args[0]);
					String name = pkg.nameProvider.getTempName();
					Expression rexpr = compileExpression(pkg, name, (Element) e.args[1]);
					code.addAll(rexpr);
					code.add(new Operation(asnType,e.range,var,var,name));
					break;
				case RETURN:
					name = pkg.nameProvider.getTempName();
					Expression expr = compileExpression(pkg,name, (Element) e.args[0]);
					code.addAll(expr);
					code.add(new Operation(OpType.RET,e.range,name));
					break;
				case RETURN_NULL:
					code.add(new Operation(OpType.RET,e.range));
					break;
				case IF:
					name = pkg.nameProvider.getTempName();
					expr = compileExpression(pkg, name, (Element) e.args[0]);
					code.addAll(expr);
					code.add(new Operation(OpType.IF,e.range,name));
					code.add(new Operation(OpType.BEGIN,e.range));
					code.addAll(compileCode(pkg, (ArrayList<Element>) e.args[2]));
					code.add(new Operation(OpType.END,e.range));
					code.add(new Operation(OpType.ENDB,e.range));
					break;
				case ELSE:
					code.add(new Operation(OpType.ELSE,e.range));
					code.add(new Operation(OpType.BEGIN,e.range));
					code.addAll(compileCode(pkg, (ArrayList<Element>) e.args[2]));
					code.add(new Operation(OpType.END,e.range));
					code.add(new Operation(OpType.ENDB,e.range));
					break;
				case ELSEIF:
					code.add(new Operation(OpType.ELSE,e.range));
					name = pkg.nameProvider.getTempName();
					expr = compileExpression(pkg, name, (Element) e.args[0]);
					code.addAll(expr);
					code.add(new Operation(OpType.IF,e.range,name));
					code.add(new Operation(OpType.BEGIN,e.range));
					code.addAll(compileCode(pkg, (ArrayList<Element>) e.args[2]));
					code.add(new Operation(OpType.ENDB,e.range));
					code.add(new Operation(OpType.END,e.range));
					code.add(new Operation(OpType.ENDB,e.range));
					break;
				case WHILE:
					name = pkg.nameProvider.getTempName();
					expr = compileExpression(pkg, name, (Element) e.args[0]);
					code.addAll(expr);
					code.add(new Operation(OpType.WHILE,e.range,name));
					code.add(new Operation(OpType.BEGIN,e.range));
					code.addAll(compileCode(pkg, (ArrayList<Element>) e.args[2]));
					code.add(new Operation(OpType.END,e.range));
					code.add(new Operation(OpType.ENDB,e.range));
					break;
				case REPEAT:
					name = pkg.nameProvider.getTempName();
					code.add(new Operation(OpType.REP,e.range,name));
					code.add(new Operation(OpType.BEGIN,e.range));
					code.addAll(compileCode(pkg, (ArrayList<Element>) e.args[2]));
					expr = compileExpression(pkg, name, (Element) e.args[0]);
					code.addAll(expr);
					code.add(new Operation(OpType.END,e.range));
					code.add(new Operation(OpType.ENDB,e.range));
					break;
				case CODE:
					code.add(new Operation(OpType.BEGIN,e.range));
					code.addAll(compileCode(pkg, (ArrayList<Element>) e.args[0]));
					code.add(new Operation(OpType.END,e.range));
					break;
				default:
					code.addAll(compileExpression(pkg,null,e));
			}
		}
		return optimize(pkg, code);
		//return code;
	}
	
	public static Expression compileExpression(SourcePackage pkg, String retVar, Element e) throws SourceException {
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
					case ICALL:
						String iname = pkg.nameProvider.getTempName();
						expr.addAll(compileExpression(pkg, iname, ((ArrayList<Element>) e.args[1]).get(0)));
						expr.add(new Operation(OpType.INDEX, e.range, retVar, (String) e.args[0], iname));
						break;
					case INDEX:
						ArrayList<String> names = new ArrayList<>();
						if (!((ArrayList<Element>) e.args[0]).isEmpty()) {
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
					case PAREN:
						ArrayList<Element> pargs = (ArrayList<Element>) e.args[0];
						if (pargs.size()!=1) {
							throw new SourceException(e.range,"Invalid use of parenthesis");
						}
						expr.addAll(compileExpression(pkg, retVar, pargs.get(0)));
						break;
				}
			}
		}
		return expr;
	}
	
	public static String resolveLValue(SourcePackage pkg, ArrayList<Operation> ops, Element e) throws SourceException {
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					return (String) e.args[0];
			}
		} else if (e.type instanceof Rule) {
			switch ((Rule)e.type) {
				
			}
		}
		throw new SourceException(e.range,"Could not resolve L-value");
	}
	
	public static ArrayList<Operation> inlineFunc(ArrayList<Operation> code, String retVar) throws SourceException {
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
	
	public static ArrayList<Operation> optimize(SourcePackage pkg, ArrayList<Operation> code) throws SourceException {
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
