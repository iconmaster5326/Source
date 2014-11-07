package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.compile.ScopeFrame.Variable;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceDataTypeException;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.exception.SourceSafeModeException;
import com.iconmaster.source.exception.SourceSyntaxException;
import com.iconmaster.source.exception.SourceUndefinedFunctionException;
import com.iconmaster.source.exception.SourceUndefinedVariableException;
import com.iconmaster.source.exception.SourceUninitializedVariableException;
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
		CompileData cd = new CompileData(pkg);
		for (Field field : cd.pkg.getFields()) {
			if (!field.isCompiled() && !field.isLibrary()) {
				cd.frame = new ScopeFrame(cd.pkg);
				compileField(cd, field);
			}
		}
		for (Function fn : cd.pkg.getFunctions()) {
			if (!fn.isCompiled() && !fn.isLibrary()) {
				cd.frame = new ScopeFrame(cd.pkg);
				compileFunction(cd, fn);
			}
		}
		return cd.errs;
	}
	
	public static Expression compileFunction(CompileData cd, Function fn) {
		cd.dirs = fn.getDirectives();
		for (Field v : fn.getArguments()) {
			cd.frame.putVariable(v.getName(), false/*!Directives.has(fn, "export")*/);
			if (v.getRawType()!=null) {
				v.setType(compileDataType(cd, v.getRawType()));
				cd.frame.setVarType(v.getName(), v.getType());
			}
		}
		if (fn.getReturn()!=null) {
			fn.setReturnType(compileDataType(cd, fn.getReturn()));
		}
		Expression code = compileCode(cd, fn.rawData());
		fn.setCompiled(code);
		return code;
	}
	
	public static Expression compileField(CompileData cd, Field field) {
		cd.dirs = field.getDirectives();
		Variable v = cd.frame.putVariable(field.getName(), false);
		if (field.getRawType()!=null) {
			field.setType(compileDataType(cd, field.getRawType()));
		}
		if (field.rawData()!=null) {
			Expression expr = compileExpr(cd, v.name, field.rawData());
			field.setCompiled(expr);
			return expr;
		}
		return null;
	}
	
	public static Expression compileCode(CompileData cd, ArrayList<Element> es) {
		Expression code = new Expression();
		OpType asnType = null;
		for (Element e : es) {
			if (e.type==TokenRule.STRING) {
				//native code
				ArrayList<String> a = Directives.getValues(e.directives, "lang");
				if (a.isEmpty()) {
					cd.errs.add(new SourceException(e.range,"String has @lang, but no language is specified"));
				} else if (a.size()>1) {
					cd.errs.add(new SourceException(e.range,"More than 1 @lang string specified"));
				} else {
					String lang = a.get(0);
					code.add(new Operation(OpType.NATIVE, e.range, lang, (String) e.args[0]));
				}
			} else {
				switch ((Rule)e.type) {
					case LOCAL:
					case LOCAL_ASN:
						if (Directives.has(e, "inline")) {
							int asni = 0;
							ArrayList<Element> les = (ArrayList<Element>) e.args[0];
							ArrayList<Element> res = (ArrayList<Element>) e.args[1];
							if (res==null) {
								res = new ArrayList<>();
							}
							for (Element e2 : les) {
								String expr2 = resolveLValueRaw(cd, e2);
								cd.frame.putInline(expr2);
								if (asni<res.size()) {
									cd.frame.putInline(expr2, res.get(asni));
								}
								asni++;
							}
							break;
						}
						for (Element e2 : (ArrayList<Element>) e.args[0]) {
							String expr2 = resolveLValueRaw(cd, e2);
							cd.frame.putDefined(expr2);
							if (e2.dataType!=null) {
								cd.frame.setVarType(expr2, compileDataType(cd, e2.dataType));
							} else {
								if (Directives.has(cd.dirs, "safe")) {
									cd.errs.add(new SourceSafeModeException(e.range,"Variable "+expr2+" was not given a type (@safe mode is on)", expr2));
								}
							}
						}
					case ASSIGN:
						ArrayList<String> names = new ArrayList<>();
						ArrayList<Expression> lexprs = new ArrayList<>();
						ArrayList<Expression> rexprs = new ArrayList<>();
						ArrayList<Operation> movs = new ArrayList<>();
						ArrayList<Element> les = (ArrayList<Element>) e.args[0];
						ArrayList<Element> res = (ArrayList<Element>) e.args[1];
						if (res==null) {
							res = new ArrayList<>();
						}
						for (Element e2 : res) {
							String name2 = cd.frame.newVarName();
							Expression expr2 = compileExpr(cd, name2, e2);
							names.add(name2);
							lexprs.add(expr2);
						}
						int asni = 0;
						for (Element e2 : les) {
							if (asni<names.size()) {
								String name2 = resolveLValueRaw(cd, e2);
								if (name2!=null && cd.frame.isInlined(name2)) {
									cd.frame.putInline(name2, res.get(asni));
								} else {
									Expression expr2 = resolveLValue(cd, code, e2);
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
										cd.errs.add(new SourceDataTypeException(e.range,"Cannot assign a value of type "+rtype+" to variable "+expr2.retVar+" of type "+ltype));
									} else {
										cd.frame.setVarType(expr2.retVar, new DataType(highest,ltype.weak));
										cd.frame.setVarType(lexprs.get(asni).retVar, new DataType(highest,ltype.weak));
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
					case IFBLOCK:
						Element ifBlock = (Element) e.args[0];
						Expression cond = compileExpr(cd, cd.frame.newVarName(), (Element) ifBlock.args[0]);
						code.add(new Operation(OpType.DO, ifBlock.range));
						code.addAll(cond);
						code.add(new Operation(OpType.IF, ifBlock.range, cond.retVar));
						code.add(new Operation(OpType.BEGIN, ifBlock.range));
						cd.frame = new ScopeFrame(cd);
						code.addAll(compileCode(cd, (ArrayList<Element>) ifBlock.args[2]));
						code.add(new Operation(OpType.END, ifBlock.range));
						int ends = 1;
						for (Element elif : (ArrayList<Element>) e.args[1]) {
							cond = compileExpr(cd, cd.frame.newVarName(), (Element) elif.args[0]);
							code.add(new Operation(OpType.ELSE, elif.range));
							code.add(new Operation(OpType.DO, elif.range));
							code.addAll(cond);
							code.add(new Operation(OpType.IF, elif.range, cond.retVar));
							code.add(new Operation(OpType.BEGIN, elif.range));
							cd.frame = new ScopeFrame(cd);
							code.addAll(compileCode(cd, (ArrayList<Element>) elif.args[2]));
							code.add(new Operation(OpType.END, elif.range));
							ends++;
						}
						if (e.args[2]!=null) {
							Element elseBlock = (Element) e.args[2];
							code.add(new Operation(OpType.ELSE, elseBlock.range));
							code.add(new Operation(OpType.BEGIN, elseBlock.range));
							cd.frame = new ScopeFrame(cd);
							code.addAll(compileCode(cd, (ArrayList<Element>) elseBlock.args[2]));
							code.add(new Operation(OpType.END, elseBlock.range));
						}
						for (int i=0;i<ends;i++) {
							code.add(new Operation(OpType.ENDB, e.range));
						}
						break;
					case WHILE:
						cond = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
						code.add(new Operation(OpType.DO, e.range));
						code.addAll(cond);
						code.add(new Operation(OpType.WHILE, e.range, cond.retVar));
						code.add(new Operation(OpType.BEGIN, e.range));
						cd.frame = new ScopeFrame(cd);
						code.addAll(compileCode(cd, (ArrayList<Element>) e.args[2]));
						code.add(new Operation(OpType.END, e.range));
						code.add(new Operation(OpType.ENDB, e.range));
						break;
					case REPEAT:
						cond = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
						code.add(new Operation(OpType.DO, e.range));
						code.addAll(cond);
						code.add(new Operation(OpType.REP, e.range, cond.retVar));
						code.add(new Operation(OpType.BEGIN, e.range));
						cd.frame = new ScopeFrame(cd);
						code.addAll(compileCode(cd, (ArrayList<Element>) e.args[2]));
						code.add(new Operation(OpType.END, e.range));
						code.add(new Operation(OpType.ENDB, e.range));
						break;
					case RETURN_NULL:
						code.add(new Operation(OpType.RET, e.range));
						break;
					case RETURN:
						cond = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
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
						Expression lexpr1 = resolveLValue(cd, code, (Element) e.args[0]);
						Expression lexpr2 = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
						Expression rexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[1]);
						code.addAll(rexpr);
						code.addAll(lexpr2);
						code.add(new Operation(asnType, e.range, lexpr1.retVar, lexpr2.retVar, rexpr.retVar));
						code.addAll(lexpr1);
						break;
					default:
						code.addAll(compileExpr(cd, cd.frame.newVarName(), e));
				}
			}
		}
		code.add(0, new Operation(OpType.TYPE, null, cd.frame.getTypeStrings()));
		code.add(0, new Operation(OpType.DEF, null, cd.frame.getAllVars()));
		return code;
	}
	
	public static Expression compileExpr(CompileData cd, String retVar, Element e) {
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
					if (cd.pkg.getField((String)e.args[0])!=null) {
						expr.add(new Operation(OpType.MOV, e.range, retVar, (String)e.args[0]));
					} else if (cd.frame.isInlined((String)e.args[0])) {
						Element e2 = cd.frame.getInline((String)e.args[0]);
						if (e2==null) {
							cd.errs.add(new SourceUninitializedVariableException(e.range,"Constant "+e.args[0]+" not initialized", (String) e.args[0]));
						} else {
							Expression expr2 = compileExpr(cd, retVar, e2);
							expr.addAll(expr2);
							expr.type = expr2.type;
						}
					} else if (!cd.frame.isDefined((String)e.args[0])) {
						cd.errs.add(new SourceUndefinedVariableException(e.range, "Undefined variable "+e.args[0], (String) e.args[0]));
					} else if (cd.frame.getVariable((String)e.args[0])==null) {
						cd.errs.add(new SourceUninitializedVariableException(e.range, "Uninitialized variable "+e.args[0], (String) e.args[0]));
					} else {
						expr.add(new Operation(OpType.MOV, e.range, retVar, cd.frame.getVariableName((String)e.args[0])));
						expr.type = cd.frame.getVarType(cd.frame.getVariableName((String)e.args[0]));
					}
					break;
			}
		} else {
			if (OpType.MathToOpType((Rule) e.type)!=null && OpType.MathToOpType((Rule) e.type).isMathOp()) {
				Expression lexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
				Expression rexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[1]);
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
						cd.errs.add(new SourceDataTypeException(e.range,"Types "+ltype+" and "+rtype+" are not equatable"));
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
						Function fn = cd.pkg.getFunction((String) e.args[0]);
						boolean method = false;
						if (fn==null) {
							fn = getRealFunction(cd, (String) e.args[0]);
							method = true;
							if (fn==null) {
								cd.errs.add(new SourceUndefinedFunctionException(e.range, "Undefined function "+e.args[0], (String) e.args[0]));
								break;
							}
						}
						es = (ArrayList<Element>) e.args[1];
						if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
							es = (ArrayList<Element>) es.get(0).args[0];
						} else if (es.size()>1) {
							cd.errs.add(new SourceSyntaxException(e.range, "Illegal function call format"));
						}
						if ((es.size()+(method?1:0))!=fn.getArguments().size()) {
							cd.errs.add(new SourceUndefinedFunctionException(e.range, "function "+fn.getName()+" requires "+fn.getArguments().size()+" arguments; got "+(es.size()+(method?1:0)), (String) e.args[0]));
						}
						int i=(method?1:0);
						for (Element e2 : es) {
							Expression expr2 = compileExpr(cd, "", e2);
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
								cd.errs.add(new SourceDataTypeException(e2.range,"Argument "+fn.getArguments().get(i).getName()+" of function "+fn.getName()+" is of type "+ltype+"; got an argument of type "+rtype));
							}
							i++;
						}
						ArrayList<Expression> args = new ArrayList<>();
						if (Directives.has(fn, "inline")) {
							if (method) {
								Expression mexpr = new Expression();
								String s = (String) e.args[0];
								s = s.substring(0,s.indexOf(".")-1);
								DataType type = cd.frame.getVarType(s);
								mexpr.type = type;
								mexpr.retVar = fn.getArguments().get(0).getName();
								mexpr.add(new Operation(OpType.MOV, e.range, mexpr.retVar, s));
								args.add(mexpr);
							}
							int j=method?1:0;
							for (Element e2 : es) {
								String name = fn.getArguments().get(j).getName();
								Expression expr2 = compileExpr(cd, name, e2);
								args.add(expr2);
								j++;
							}
							for (Expression expr2 : args) {
								expr.addAll(expr2);
							}
							cd.frame = new ScopeFrame(cd);
							ArrayList<Operation> fncode = compileFunction(cd, fn);
							ArrayList<Operation> newcode = new ArrayList<>();
							String label = cd.pkg.nameProvider.getTempName();
							boolean lUsed = false;
							for (Operation op: fncode) {
								if (op.op==OpType.RET) {
									if (op.args.length!=0) {
										newcode.add(new Operation(OpType.MOV, op.range, retVar, op.args[0]));
									}
									newcode.add(new Operation(OpType.GOTO, op.range, label));
									lUsed = true;
								} else {
									newcode.add(op);
								}
							}
							expr.add(new Operation(OpType.BEGIN, e.range));
							expr.addAll(newcode);
							if (lUsed) {
								expr.add(new Operation(OpType.LABEL, e.range, label));
							}
							expr.add(new Operation(OpType.END, e.range));
						} else {
							ArrayList<String> names = new ArrayList<>();
							if (method) {
								Expression mexpr = new Expression();
								String s = (String) e.args[0];
								s = s.substring(0,s.indexOf("."));
								DataType type = cd.frame.getVarType(s);
								mexpr.type = type;
								mexpr.retVar = cd.frame.newVarName();
								mexpr.add(new Operation(OpType.MOV, e.range, mexpr.retVar, s));
								names.add(mexpr.retVar);
								args.add(mexpr);
							}
							for (Element e2 : es) {
								String name = cd.frame.newVarName();
								Expression expr2 = compileExpr(cd, name, e2);
								args.add(expr2);
								names.add(name);
							}
							for (Expression expr2 : args) {
								expr.addAll(expr2);
							}
							names.add(0, (String) getFullyQualifiedName(cd, fn));
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
							cd.errs.add(new SourceSyntaxException(e.range, "Illegal index format"));
						}
						for (Element e2 : es) {
							String name = cd.frame.newVarName();
							Expression expr2 = compileExpr(cd, name, e2);
							expr.addAll(expr2);
							names.add(name);
						}
						if (!cd.frame.isDefined((String)e.args[0])) {
							cd.errs.add(new SourceUndefinedVariableException(e.range, "Undefined variable "+e.args[0], (String) e.args[0]));
						} else if (cd.frame.getVariable((String)e.args[0])==null) {
							cd.errs.add(new SourceUninitializedVariableException(e.range, "Uninitialized variable "+e.args[0], (String) e.args[0]));
						} else {
							names.add(0,cd.frame.getVariableName((String)e.args[0]));
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
							cd.errs.add(new SourceSyntaxException(e.range, "Illegal list format"));
						}
						for (Element e2 : es) {
							String name = cd.frame.newVarName();
							expr.addAll(compileExpr(cd, name, e2));
							names.add(name);
						}
						names.add(0,retVar);
						expr.add(new Operation(OpType.MOVL, e.range, names.toArray(new String[0])));
						expr.type = new DataType(TypeDef.LIST,false);
						break;
					case PAREN:
						es = (ArrayList<Element>) e.args[0];
						if (es.size()!=1) {
							cd.errs.add(new SourceSyntaxException(e.range, "Illegal parenthesis format"));
						}
						for (Element e2 : es) {
							expr.addAll(compileExpr(cd, retVar, e2));
						}
						break;
					case TRUE:
						expr.add(new Operation(OpType.TRUE, e.range, retVar));
						expr.type = new DataType(TypeDef.REAL,true);
						break;
					case FALSE:
						expr.add(new Operation(OpType.FALSE, e.range, retVar));
						expr.type = new DataType(TypeDef.REAL,true);
						break;
					case TO:
						String name = cd.frame.newVarName();
						Expression lexpr = compileExpr(cd, name, (Element) e.args[0]);
						expr.addAll(lexpr);
						DataType rtype = compileDataType(cd, (Element) e.args[1]);
						String fnName = rtype.type.name+"._cast";
						if (cd.pkg.getFunction(fnName)==null) {
							cd.errs.add(new SourceUndefinedFunctionException(e.range, "No conversion function from type "+lexpr.type+" to type "+rtype+" exists", rtype.type.name+"._cast"));
						} else {
							expr.add(new Operation(OpType.CALL, e.range, retVar, fnName, name));
							expr.type = rtype;
						}
						break;
				}
			}
		}
		if (e.dataType!=null) {
			expr.type = compileDataType(cd, e.dataType);
		}
		return expr;
	}
	
	public static DataType compileDataType(CompileData cd, Element e) {
		DataType dt = new DataType();
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					TypeDef def = cd.pkg.getType((String) e.args[0]);
					if (def==null) {
						cd.errs.add(new SourceDataTypeException(e.range, "unknown data type "+e.args[0]));
					}
					dt.type = def;
					break;
				default:
					cd.errs.add(new SourceSyntaxException(e.range, "Illegal data type format"));
			}
		} else {
			switch ((Rule)e.type) {
				default:
					cd.errs.add(new SourceSyntaxException(e.range, "Illegal data type format"));
			}
		}
		return dt;
	}
	
	public static Expression resolveLValue(CompileData cd, Expression code, Element e) {
		Expression expr = new Expression();
		String name;
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					name = (String) e.args[0];
					if (cd.pkg.getField(name)!=null) {
						
					} else if (!cd.frame.isDefined(name)) {
						cd.errs.add(new SourceUndefinedVariableException(e.range,"Variable "+name+" not declared local", (String) e.args[0]));
					} else if (cd.frame.getVariable(name)==null) {
						name = cd.frame.putVariable(name, false).name;
					} else {
						name = cd.frame.getVariableName(name);
					}
					expr.retVar = name;
					expr.type = cd.frame.getVarType(name);
					break;
				default:
					cd.errs.add(new SourceSyntaxException(e.range,"Illegal L-value"));
			}
		} else {
			switch ((Rule)e.type) {
				case ICALL:
					ArrayList<String> names = new ArrayList<>();
					ArrayList<Element> es = (ArrayList<Element>) e.args[1];
					if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
						es = (ArrayList<Element>) es.get(0).args[0];
					} else if (es.size()!=1) {
						cd.errs.add(new SourceSyntaxException(e.range, "Illegal index format"));
					}
					for (Element e2 : es) {
						String name2 = cd.frame.newVarName();
						Expression expr2 = compileExpr(cd, name2, e2);
						code.addAll(expr2);
						names.add(name2);
					}
					expr.retVar = cd.frame.newVarName();
					names.add(0,expr.retVar);
					if (!cd.frame.isDefined((String)e.args[0])) {
						cd.errs.add(new SourceUndefinedVariableException(e.range, "Undefined variable "+e.args[0], (String) e.args[0]));
					} else if (cd.frame.getVariable((String)e.args[0])==null) {
						cd.errs.add(new SourceUninitializedVariableException(e.range, "Uninitialized variable "+e.args[0], (String) e.args[0]));
					} else {
						names.add(0,cd.frame.getVariableName((String)e.args[0]));
					}
					expr.add(new Operation(OpType.MOVI, e.range, names.toArray(new String[0])));
					break;
				default:
					cd.errs.add(new SourceSyntaxException(e.range,"Illegal L-value"));
			}
		}
		return expr;
	}
	
	public static String resolveLValueRaw(CompileData cd, Element e) {
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
	
	public static String getFullyQualifiedName(CompileData cd, Function fn) {
		return fn.pkgName+"."+fn.getName();
	}
	
	public static Function getRealFunction(CompileData cd, String name) {
		if (name.contains(".")) {
			String varName = name.substring(0,name.indexOf("."));
			String funcName = name.substring(name.indexOf(".")+1);
			DataType type = cd.frame.getVarType(varName);
			if (type==null) {
				type = new DataType();
			}
			if (cd.pkg.getFunction(type.type.name+"."+funcName)!=null) {
				return cd.pkg.getFunction(type.type.name+"."+funcName);
			}
		}
		return null;
	}
}
