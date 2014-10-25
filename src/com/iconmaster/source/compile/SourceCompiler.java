package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.compile.ScopeFrame.Variable;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
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
		ScopeFrame sf = new ScopeFrame(pkg);
		for (Field field : pkg.getFields()) {
			if (!field.isCompiled() && !field.isLibrary()) {
				compileField(pkg, new ScopeFrame(pkg, sf), field, errs);
			}
		}
		for (Function fn : pkg.getFunctions()) {
			if (!fn.isCompiled() && !fn.isLibrary()) {
				compileFunction(pkg, new ScopeFrame(pkg, sf), fn, errs);
			}
		}
		return errs;
	}
	
	public static Expression compileFunction(SourcePackage pkg, ScopeFrame frame, Function fn, ArrayList<SourceException> errs) {
		for (Field v : fn.getArguments()) {
			frame.putVariable(v.getName(), !Directives.has(fn, "export"));
		}
		Expression code = compileCode(pkg, frame, fn.rawData(), errs);
		fn.setCompiled(code);
		return code;
	}
	
	public static Expression compileField(SourcePackage pkg, ScopeFrame frame, Field field, ArrayList<SourceException> errs) {
		Variable v = frame.putVariable(field.getName(), false);
		Expression expr = compileExpr(pkg, frame, v.name, field.rawData(), errs);
		field.setCompiled(expr);
		return expr;
	}
	
	public static Expression compileCode(SourcePackage pkg, ScopeFrame frame, ArrayList<Element> es, ArrayList<SourceException> errs) {
		Expression code = new Expression();
		for (Element e : es) {
			switch ((Rule)e.type) {
				case LOCAL:
					for (Element e2 : (ArrayList<Element>) e.args[0]) {
						String expr2 = resolveLValueRaw(pkg, frame, e2, errs);
						frame.putDefined(expr2);
						if (Directives.has(e, "inline")) {
							frame.putInline(expr2);
						}
					}
					break;
				case LOCAL_ASN:
					if (Directives.has(e, "inline")) {
						int asni = 0;
						ArrayList<Element> les = (ArrayList<Element>) e.args[0];
						ArrayList<Element> res = (ArrayList<Element>) e.args[1];
						for (Element e2 : les) {
							String expr2 = resolveLValueRaw(pkg, frame, e2, errs);
							frame.putInline(expr2);
							if (asni<res.size()) {
								frame.putInline(expr2, res.get(asni));
							}
							asni++;
						}
						break;
					}
					for (Element e2 : (ArrayList<Element>) e.args[0]) {
						String expr2 = resolveLValueRaw(pkg, frame, e2, errs);
						frame.putDefined(expr2);
					}
				case ASSIGN:
					ArrayList<String> names = new ArrayList<>();
					ArrayList<Expression> lexprs = new ArrayList<>();
					ArrayList<Expression> rexprs = new ArrayList<>();
					ArrayList<Element> les = (ArrayList<Element>) e.args[0];
					ArrayList<Element> res = (ArrayList<Element>) e.args[1];
					for (Element e2 : res) {
						String name2 = frame.newVarName();
						Expression expr2 = compileExpr(pkg, frame, name2, e2, errs);
						names.add(name2);
						lexprs.add(expr2);
					}
					int asni = 0;
					for (Element e2 : les) {
						if (asni<names.size()) {
							if (frame.isInlined(names.get(asni))) {
								frame.putInline(names.get(asni), e2);
							} else {
								Expression expr2 = resolveLValue(pkg, frame, code, e2, errs);
								code.add(new Operation(OpType.MOV, e2.range, expr2.retVar, names.get(asni)));
								code.addAll(lexprs.get(asni));
								rexprs.add(expr2);
							}
						}
						asni++;
					}
					for (Expression expr2 : rexprs) {
						code.addAll(expr2);
					}
					break;
				case IF:
					Expression cond = compileExpr(pkg, frame, frame.newVarName(), (Element) e.args[0], errs);
					code.addAll(cond);
					code.add(new Operation(OpType.IF, e.range, cond.retVar));
					code.add(new Operation(OpType.BEGIN, e.range));
					code.addAll(compileCode(pkg, new ScopeFrame(pkg,frame), (ArrayList<Element>) e.args[2], errs));
					code.add(new Operation(OpType.END, e.range));
					code.add(new Operation(OpType.ENDB, e.range));
					break;
				case ELSE:
					code.add(new Operation(OpType.ELSE, e.range));
					code.add(new Operation(OpType.BEGIN, e.range));
					code.addAll(compileCode(pkg, new ScopeFrame(pkg,frame), (ArrayList<Element>) e.args[2], errs));
					code.add(new Operation(OpType.END, e.range));
					code.add(new Operation(OpType.ENDB, e.range));
					break;
				case WHILE:
					cond = compileExpr(pkg, frame, frame.newVarName(), (Element) e.args[0], errs);
					code.addAll(cond);
					code.add(new Operation(OpType.WHILE, e.range, cond.retVar));
					code.add(new Operation(OpType.BEGIN, e.range));
					code.addAll(compileCode(pkg, new ScopeFrame(pkg,frame), (ArrayList<Element>) e.args[2], errs));
					code.add(new Operation(OpType.END, e.range));
					code.add(new Operation(OpType.ENDB, e.range));
					break;
				case REPEAT:
					cond = compileExpr(pkg, frame, frame.newVarName(), (Element) e.args[0], errs);
					code.addAll(cond);
					code.add(new Operation(OpType.REP, e.range, cond.retVar));
					code.add(new Operation(OpType.BEGIN, e.range));
					code.addAll(compileCode(pkg, new ScopeFrame(pkg,frame), (ArrayList<Element>) e.args[2], errs));
					code.add(new Operation(OpType.END, e.range));
					code.add(new Operation(OpType.ENDB, e.range));
					break;
				case RETURN_NULL:
					code.add(new Operation(OpType.RET, e.range));
					break;
				case RETURN:
					cond = compileExpr(pkg, frame, frame.newVarName(), (Element) e.args[0], errs);
					code.addAll(cond);
					code.add(new Operation(OpType.RET, e.range, cond.retVar));
					break;
				default:
					code.addAll(compileExpr(pkg, frame, frame.newVarName(), e, errs));
			}
		}
		code.add(0, new Operation(OpType.DEF, null, frame.getAllVars()));
		return code;
	}
	
	public static Expression compileExpr(SourcePackage pkg, ScopeFrame frame, String retVar, Element e, ArrayList<SourceException> errs) {
		Expression expr = new Expression();
		expr.retVar = retVar;
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case NUMBER:
					expr.add(new Operation(OpType.MOVN, e.range, retVar, (String)e.args[0]));
					break;
				case STRING:
					expr.add(new Operation(OpType.MOVS, e.range, retVar, (String)e.args[0]));
					break;
				case WORD:
					if (pkg.getField((String)e.args[0])!=null) {
						expr.add(new Operation(OpType.MOV, e.range, retVar, (String)e.args[0]));
					} else if (frame.isInlined((String)e.args[0])) {
						Element e2 = frame.getInline((String)e.args[0]);
						if (e2==null) {
							errs.add(new SourceException(e.range,"Constant "+e.args[0]+" not initialized"));
						} else {
							expr.addAll(compileExpr(pkg, frame, retVar, e2, errs));
						}
					} else if (!frame.isDefined((String)e.args[0])) {
						errs.add(new SourceException(e.range, "Undefined variable"));
					} else if (frame.getVariable((String)e.args[0])==null) {
						errs.add(new SourceException(e.range, "Uninitialized variable"));
					} else {
						expr.add(new Operation(OpType.MOV, e.range, retVar, frame.getVariableName((String)e.args[0])));
					}
					break;
			}
		} else {
			if (OpType.MathToOpType((Rule) e.type)!=null && OpType.MathToOpType((Rule) e.type).isMathOp()) {
				Expression lexpr = compileExpr(pkg, frame, frame.newVarName(), (Element) e.args[0], errs);
				Expression rexpr = compileExpr(pkg, frame, frame.newVarName(), (Element) e.args[1], errs);
				expr.addAll(lexpr);
				expr.addAll(rexpr);
				expr.add(new Operation(OpType.MathToOpType((Rule) e.type), e.range, retVar, lexpr.retVar, rexpr.retVar));
			} else {
				ArrayList<Element> es;
				switch ((Rule)e.type) {
					case FCALL:
						ArrayList<String> names = new ArrayList<>();
						for (Element e2 : (ArrayList<Element>) e.args[1]) {
							String name = frame.newVarName();
							Expression expr2 = compileExpr(pkg, frame, name, e2, errs);
							expr.addAll(expr2);
							names.add(name);
						}
						names.add(0, (String) e.args[0]);
						names.add(0,retVar);
						expr.add(new Operation(OpType.CALL, e.range, names.toArray(new String[0])));
						break;
					case ICALL:
						names = new ArrayList<>();
						es = (ArrayList<Element>) e.args[1];
						if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
							es = (ArrayList<Element>) es.get(0).args[0];
						} else if (es.size()>1) {
							errs.add(new SourceException(e.range, "Illegal index format"));
						}
						for (Element e2 : es) {
							String name = frame.newVarName();
							Expression expr2 = compileExpr(pkg, frame, name, e2, errs);
							expr.addAll(expr2);
							names.add(name);
						}
						if (!frame.isDefined((String)e.args[0])) {
							errs.add(new SourceException(e.range, "Undefined variable"));
						} else if (frame.getVariable((String)e.args[0])==null) {
							errs.add(new SourceException(e.range, "Uninitialized variable"));
						} else {
							names.add(0,frame.getVariableName((String)e.args[0]));
						}
						names.add(0,retVar);
						expr.add(new Operation(OpType.INDEX, e.range, names.toArray(new String[0])));
						break;
					case INDEX:
						names = new ArrayList<>();
						es = (ArrayList<Element>) e.args[0];
						if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
							es = (ArrayList<Element>) es.get(0).args[0];
						} else if (es.size()>1) {
							errs.add(new SourceException(e.range, "Illegal list format"));
						}
						for (Element e2 : es) {
							String name = frame.newVarName();
							expr.addAll(compileExpr(pkg, frame, name, e2, errs));
							names.add(name);
						}
						names.add(0,retVar);
						expr.add(new Operation(OpType.MOVL, e.range, names.toArray(new String[0])));
						break;
				}
			}
		}
		return expr;
	}
	
	public static DataType compileDataType(SourcePackage pkg, ScopeFrame frame, Element expr, ArrayList<SourceException> errs) {
		return null;
	}
	
	public static Expression resolveLValue(SourcePackage pkg, ScopeFrame frame, Expression code, Element e, ArrayList<SourceException> errs) {
		Expression expr = new Expression();
		String name;
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					name = (String) e.args[0];
					if (pkg.getField(name)!=null) {
						
//					} else if (frame.isInlined(name)) {
//						Expression inline = frame.getInline(name);
//						if (inline==null) {
//							errs.add(new SourceException(e.range,"Constant "+name+" not initialized"));
//						} else {
//							code.addAll(inline);
//							expr.retVar = inline.retVar;
//							return expr;
//						}
					} else if (!frame.isDefined(name)) {
						errs.add(new SourceException(e.range,"Variable "+name+" not declared local"));
					} else if (frame.getVariable(name)==null) {
						name = frame.putVariable(name, false).name;
					} else {
						name = frame.getVariableName(name);
					}
					expr.retVar = name;
					break;
				default:
					errs.add(new SourceException(e.range,"Illegal L-value"));
			}
		} else {
			switch ((Rule)e.type) {
				default:
					errs.add(new SourceException(e.range,"Illegal L-value"));
			}
		}
		return expr;
	}
	
	public static String resolveLValueRaw(SourcePackage pkg, ScopeFrame frame, Element e, ArrayList<SourceException> errs) {
		String name = null;
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					name = (String) e.args[0];
					break;
				default:
					errs.add(new SourceException(e.range,"Illegal L-value"));
			}
		} else {
			switch ((Rule)e.type) {
				default:
					errs.add(new SourceException(e.range,"Illegal L-value"));
			}
		}
		return name;
	}
}
