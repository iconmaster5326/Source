package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.compile.ScopeFrame.Variable;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;
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
			frame.putVariable(v.getName(), false/*!Directives.has(fn, "export")*/);
			if (v.getRawType()!=null) {
				v.setType(compileDataType(pkg, frame, v.getRawType(), errs));
				frame.setVarType(v.getName(), v.getType());
			}
		}
		if (fn.getReturn()!=null) {
			fn.setReturnType(compileDataType(pkg, frame, fn.getReturn(), errs));
		}
		Expression code = compileCode(pkg, frame, fn.rawData(), errs);
		fn.setCompiled(code);
		return code;
	}
	
	public static Expression compileField(SourcePackage pkg, ScopeFrame frame, Field field, ArrayList<SourceException> errs) {
		Variable v = frame.putVariable(field.getName(), false);
		if (field.getRawType()!=null) {
			field.setType(compileDataType(pkg, frame, field.getRawType(), errs));
		}
		if (field.rawData()!=null) {
			Expression expr = compileExpr(pkg, frame, v.name, field.rawData(), errs);
			field.setCompiled(expr);
			return expr;
		}
		return null;
	}
	
	public static Expression compileCode(SourcePackage pkg, ScopeFrame frame, ArrayList<Element> es, ArrayList<SourceException> errs) {
		Expression code = new Expression();
		OpType asnType = null;
		for (Element e : es) {
			switch ((Rule)e.type) {
				case LOCAL:
					for (Element e2 : (ArrayList<Element>) e.args[0]) {
						String expr2 = resolveLValueRaw(pkg, frame, e2);
						frame.putDefined(expr2);
						if (Directives.has(e, "inline")) {
							frame.putInline(expr2);
						}
						if (e2.dataType!=null) {
							frame.setVarType(expr2, compileDataType(pkg, frame, e2.dataType, errs));
						}
					}
					break;
				case LOCAL_ASN:
					if (Directives.has(e, "inline")) {
						int asni = 0;
						ArrayList<Element> les = (ArrayList<Element>) e.args[0];
						ArrayList<Element> res = (ArrayList<Element>) e.args[1];
						for (Element e2 : les) {
							String expr2 = resolveLValueRaw(pkg, frame, e2);
							frame.putInline(expr2);
							if (asni<res.size()) {
								frame.putInline(expr2, res.get(asni));
							}
							asni++;
						}
						break;
					}
					for (Element e2 : (ArrayList<Element>) e.args[0]) {
						String expr2 = resolveLValueRaw(pkg, frame, e2);
						frame.putDefined(expr2);
						if (e2.dataType!=null) {
							frame.setVarType(expr2, compileDataType(pkg, frame, e2.dataType, errs));
						}
					}
				case ASSIGN:
					ArrayList<String> names = new ArrayList<>();
					ArrayList<Expression> lexprs = new ArrayList<>();
					ArrayList<Expression> rexprs = new ArrayList<>();
					ArrayList<Operation> movs = new ArrayList<>();
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
							String name2 = resolveLValueRaw(pkg, frame, e2);
							if (name2!=null && frame.isInlined(name2)) {
								frame.putInline(name2, res.get(asni));
							} else {
								Expression expr2 = resolveLValue(pkg, frame, code, e2, errs);
								movs.add(new Operation(OpType.MOV, e2.range, expr2.retVar, names.get(asni)));
								code.addAll(lexprs.get(asni));
								rexprs.add(expr2);
								
								//check data types
								DataType rtype = lexprs.get(asni).type;
								DataType ltype = expr2.type;
								if (ltype==null) {
									ltype = new DataType(true);
								}
								if (rtype==null) {
									rtype = new DataType(true);
								}
								TypeDef highest = ltype.type.getHighestType(rtype.type, ltype.weak);
								if (highest==null) {
									errs.add(new SourceException(e.range,"Cannot assign a value of type "+rtype+" to variable "+expr2.retVar+" of type "+ltype));
								} else {
									frame.setVarType(expr2.retVar, new DataType(highest,ltype.weak));
									frame.setVarType(lexprs.get(asni).retVar, new DataType(highest,ltype.weak));
								}
							}
						}
						asni++;
					}
					for (Operation mov : movs) {
						code.add(mov);
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
				case ADD_ASN:
					asnType = OpType.ADD;
				case SUB_ASN:
					if (e.type==Rule.SUB_ASN) {
						asnType = OpType.SUB;
					}
				case MUL_ASN:
					if (e.type==Rule.MUL_ASN) {
						asnType = OpType.MUL;
					}
				case DIV_ASN:
					if (e.type==Rule.DIV_ASN) {
						asnType = OpType.DIV;
					}
					Expression lexpr1 = resolveLValue(pkg, frame, code, (Element) e.args[0], errs);
					Expression lexpr2 = compileExpr(pkg, frame, frame.newVarName(), (Element) e.args[0], errs);
					Expression rexpr = compileExpr(pkg, frame, frame.newVarName(), (Element) e.args[1], errs);
					code.addAll(rexpr);
					code.addAll(lexpr2);
					code.add(new Operation(asnType, e.range, lexpr1.retVar, lexpr2.retVar, rexpr.retVar));
					code.addAll(lexpr1);
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
					expr.type = new DataType(TypeDef.REAL,true);
					break;
				case STRING:
					expr.add(new Operation(OpType.MOVS, e.range, retVar, (String)e.args[0]));
					expr.type = new DataType(TypeDef.STRING,false);
					break;
				case WORD:
					if (pkg.getField((String)e.args[0])!=null) {
						expr.add(new Operation(OpType.MOV, e.range, retVar, (String)e.args[0]));
					} else if (frame.isInlined((String)e.args[0])) {
						Element e2 = frame.getInline((String)e.args[0]);
						if (e2==null) {
							errs.add(new SourceException(e.range,"Constant "+e.args[0]+" not initialized"));
						} else {
							Expression expr2 = compileExpr(pkg, frame, retVar, e2, errs);
							expr.addAll(expr2);
							expr.type = expr2.type;
						}
					} else if (!frame.isDefined((String)e.args[0])) {
						errs.add(new SourceException(e.range, "Undefined variable"));
					} else if (frame.getVariable((String)e.args[0])==null) {
						errs.add(new SourceException(e.range, "Uninitialized variable"));
					} else {
						expr.add(new Operation(OpType.MOV, e.range, retVar, frame.getVariableName((String)e.args[0])));
						expr.type = frame.getVarType(frame.getVariableName((String)e.args[0]));
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
				//check data types	
				DataType rtype = lexpr.type;
				DataType ltype = rexpr.type;
				if (ltype==null) {
					ltype = new DataType(true);
				}
				if (rtype==null) {
					rtype = new DataType(true);
				}
				TypeDef highest = ltype.type.getHighestType(rtype.type, ltype.weak && rtype.weak);
				if (highest==null) {
					if (e.type!=Rule.CONCAT) {
						errs.add(new SourceException(e.range,"Types "+ltype+" and "+rtype+" are not equatable"));
					} else {
						expr.type = new DataType(TypeDef.UNKNOWN, ltype.weak && rtype.weak);
					}
				} else {
					expr.type = new DataType(highest, ltype.weak && rtype.weak);
				}
			} else {
				ArrayList<Element> es;
				switch ((Rule)e.type) {
					case FCALL:
						Function fn = pkg.getFunction((String) e.args[0]);
						boolean method = false;
						if (fn==null) {
							fn = getRealFunction(pkg, frame, (String) e.args[0], errs);
							method = true;
							if (fn==null) {
								errs.add(new SourceException(e.range, "Undefined function "+e.args[0]));
								break;
							}
						}
						es = (ArrayList<Element>) e.args[1];
						if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
							es = (ArrayList<Element>) es.get(0).args[0];
						} else if (es.size()>1) {
							errs.add(new SourceException(e.range, "Illegal function call format"));
						}
						if ((es.size()+(method?1:0))!=fn.getArguments().size()) {
							errs.add(new SourceException(e.range, "function "+fn.getName()+" requires "+fn.getArguments().size()+" arguments; got "+(es.size()+(method?1:0))));
						}
						int i=(method?1:0);
						for (Element e2 : es) {
							Expression expr2 = compileExpr(pkg, frame, "", e2, errs);
							DataType ltype = fn.getArguments().get(i).getType();
							DataType rtype = expr2.type;
							if (ltype==null) {
								ltype = new DataType(true);
							}
							if (rtype==null) {
								rtype = new DataType(true);
							}
							TypeDef highest = ltype.type.getHighestType(rtype.type, ltype.weak);
							if (highest==null) {
								errs.add(new SourceException(e2.range,"Argument "+fn.getArguments().get(i).getName()+" of function "+fn.getName()+" is of type "+ltype+"; got an argument of type "+rtype));
							}
							i++;
						}
						ArrayList<Expression> args = new ArrayList<>();
						if (Directives.has(fn, "inline")) {
							if (method) {
								Expression mexpr = new Expression();
								String s = (String) e.args[0];
								s = s.substring(0,s.indexOf(".")-1);
								DataType type = frame.getVarType(s);
								mexpr.type = type;
								mexpr.retVar = fn.getArguments().get(0).getName();
								mexpr.add(new Operation(OpType.MOV, e.range, mexpr.retVar, s));
								args.add(mexpr);
							}
							int j=method?1:0;
							for (Element e2 : es) {
								String name = fn.getArguments().get(j).getName();
								Expression expr2 = compileExpr(pkg, frame, name, e2, errs);
								args.add(expr2);
								j++;
							}
							for (Expression expr2 : args) {
								expr.addAll(expr2);
							}
							ArrayList<Operation> fncode = compileFunction(pkg,  new ScopeFrame(pkg,frame), fn, errs);
							for (Operation op: fncode) {
								if (op.op==OpType.RET) {
									if (op.args.length==0) {
										op.op = OpType.NOP;
									} else {
										op.op = OpType.MOV;
										op.args = new String[] {retVar, op.args[0]};
									}
								}
							}
							expr.add(new Operation(OpType.BEGIN, e.range));
							expr.addAll(fncode);
							expr.add(new Operation(OpType.END, e.range));
						} else {
							ArrayList<String> names = new ArrayList<>();
							if (method) {
								Expression mexpr = new Expression();
								String s = (String) e.args[0];
								s = s.substring(0,s.indexOf("."));
								DataType type = frame.getVarType(s);
								mexpr.type = type;
								mexpr.retVar = frame.newVarName();
								mexpr.add(new Operation(OpType.MOV, e.range, mexpr.retVar, s));
								names.add(mexpr.retVar);
								args.add(mexpr);
							}
							for (Element e2 : es) {
								String name = frame.newVarName();
								Expression expr2 = compileExpr(pkg, frame, name, e2, errs);
								args.add(expr2);
								names.add(name);
							}
							for (Expression expr2 : args) {
								expr.addAll(expr2);
							}
							names.add(0, (String) getFullyQualifiedName(fn, frame));
							names.add(0,retVar);
							expr.add(new Operation(OpType.CALL, e.range, names.toArray(new String[0])));
						}
						expr.type = fn.getReturnType()==null?expr.type:fn.getReturnType();
						break;
					case ICALL:
						ArrayList<String> names = new ArrayList<>();
						es = (ArrayList<Element>) e.args[1];
						if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
							es = (ArrayList<Element>) es.get(0).args[0];
						} else if (es.size()!=1) {
							errs.add(new SourceException(e.range, "Illegal index format"));
						}
						for (Element e2 : es) {
							String name = frame.newVarName();
							Expression expr2 = compileExpr(pkg, frame, name, e2, errs);
							expr.addAll(expr2);
							names.add(name);
						}
						if (!frame.isDefined((String)e.args[0])) {
							errs.add(new SourceException(e.range, "Undefined variable "+e.args[0]));
						} else if (frame.getVariable((String)e.args[0])==null) {
							errs.add(new SourceException(e.range, "Uninitialized variable "+e.args[0]));
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
						expr.type = new DataType(TypeDef.LIST,false);
						break;
					case PAREN:
						es = (ArrayList<Element>) e.args[0];
						if (es.size()!=1) {
							errs.add(new SourceException(e.range, "Illegal parenthesis format"));
						}
						for (Element e2 : es) {
							expr.addAll(compileExpr(pkg, frame, retVar, e2, errs));
						}
						break;
					case TRUE:
						expr.add(new Operation(OpType.MOVN, e.range, retVar, "1"));
						expr.type = new DataType(TypeDef.REAL,true);
						break;
					case FALSE:
						expr.add(new Operation(OpType.MOVN, e.range, retVar, "0"));
						expr.type = new DataType(TypeDef.REAL,true);
						break;
					case TO:
						String name = frame.newVarName();
						Expression lexpr = compileExpr(pkg, frame, name, (Element) e.args[0], errs);
						expr.addAll(lexpr);
						DataType rtype = compileDataType(pkg, frame, (Element) e.args[1], errs);
						String fnName = rtype.type.name+"._cast";
						if (pkg.getFunction(fnName)==null) {
							errs.add(new SourceException(e.range, "No conversion function from type "+lexpr.type+" to type "+rtype+" exists"));
						} else {
							expr.add(new Operation(OpType.CALL, e.range, retVar, fnName, name));
							expr.type = rtype;
						}
						break;
				}
			}
		}
		if (e.dataType!=null) {
			expr.type = compileDataType(pkg, frame, e.dataType, errs);
		}
		return expr;
	}
	
	public static DataType compileDataType(SourcePackage pkg, ScopeFrame frame, Element e, ArrayList<SourceException> errs) {
		DataType dt = new DataType();
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					TypeDef def = pkg.getType((String) e.args[0]);
					if (def==null) {
						errs.add(new SourceException(e.range, "unknown data type "+e.args[0]));
					}
					dt.type = def;
					break;
				default:
					errs.add(new SourceException(e.range, "Illegal data type format"));
			}
		} else {
			switch ((Rule)e.type) {
				default:
					errs.add(new SourceException(e.range, "Illegal data type format"));
			}
		}
		return dt;
	}
	
	public static Expression resolveLValue(SourcePackage pkg, ScopeFrame frame, Expression code, Element e, ArrayList<SourceException> errs) {
		Expression expr = new Expression();
		String name;
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					name = (String) e.args[0];
					if (pkg.getField(name)!=null) {
						
					} else if (!frame.isDefined(name)) {
						errs.add(new SourceException(e.range,"Variable "+name+" not declared local"));
					} else if (frame.getVariable(name)==null) {
						name = frame.putVariable(name, false).name;
					} else {
						name = frame.getVariableName(name);
					}
					expr.retVar = name;
					expr.type = frame.getVarType(name);
					break;
				default:
					errs.add(new SourceException(e.range,"Illegal L-value"));
			}
		} else {
			switch ((Rule)e.type) {
				case ICALL:
					ArrayList<String> names = new ArrayList<>();
					ArrayList<Element> es = (ArrayList<Element>) e.args[1];
					if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
						es = (ArrayList<Element>) es.get(0).args[0];
					} else if (es.size()!=1) {
						errs.add(new SourceException(e.range, "Illegal index format"));
					}
					for (Element e2 : es) {
						String name2 = frame.newVarName();
						Expression expr2 = compileExpr(pkg, frame, name2, e2, errs);
						code.addAll(expr2);
						names.add(name2);
					}
					expr.retVar = frame.newVarName();
					names.add(0,expr.retVar);
					if (!frame.isDefined((String)e.args[0])) {
						errs.add(new SourceException(e.range, "Undefined variable "+e.args[0]));
					} else if (frame.getVariable((String)e.args[0])==null) {
						errs.add(new SourceException(e.range, "Uninitialized variable "+e.args[0]));
					} else {
						names.add(0,frame.getVariableName((String)e.args[0]));
					}
					expr.add(new Operation(OpType.MOVI, e.range, names.toArray(new String[0])));
					break;
				default:
					errs.add(new SourceException(e.range,"Illegal L-value"));
			}
		}
		return expr;
	}
	
	public static String resolveLValueRaw(SourcePackage pkg, ScopeFrame frame, Element e) {
		String name = null;
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					name = (String) e.args[0];
					break;
			}
		} else {
			switch ((Rule)e.type) {
			}
		}
		return name;
	}
	
	public static String getFullyQualifiedName(Function fn, ScopeFrame frame) {
		return fn.pkgName+"."+fn.getName();
	}
	
	public static Function getRealFunction(SourcePackage pkg, ScopeFrame frame, String name, ArrayList<SourceException> errs) {
		if (name.contains(".")) {
			String varName = name.substring(0,name.indexOf("."));
			String funcName = name.substring(name.indexOf(".")+1);
			DataType type = frame.getVarType(varName);
			if (type==null) {
				type = new DataType();
			}
			if (pkg.getFunction(type.type.name+"."+funcName)!=null) {
				return pkg.getFunction(type.type.name+"."+funcName);
			}
		}
		return null;
	}
}
