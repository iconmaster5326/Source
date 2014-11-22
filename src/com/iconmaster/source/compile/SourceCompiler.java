package com.iconmaster.source.compile;

import com.iconmaster.source.compile.CompileUtils.CodeTransformer;
import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.compile.ScopeFrame.Variable;
import static com.iconmaster.source.compile.SourceCompiler.compileExpr;
import static com.iconmaster.source.compile.SourceCompiler.compileFunction;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceDataTypeException;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.exception.SourceSafeModeException;
import com.iconmaster.source.exception.SourceSyntaxException;
import com.iconmaster.source.exception.SourceUndefinedFunctionException;
import com.iconmaster.source.exception.SourceUndefinedVariableException;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.FunctionCall;
import com.iconmaster.source.prototype.Iterator;
import com.iconmaster.source.prototype.ParamTypeDef;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;
import com.iconmaster.source.tokenize.TokenRule;
import com.iconmaster.source.util.Directives;
import com.iconmaster.source.util.IDirectable;
import com.iconmaster.source.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author iconmaster
 */
public class SourceCompiler {
	public static ArrayList<SourceException> compile(SourcePackage pkg) {
		CompileData cd = new CompileData(pkg);
		
		ArrayList<Function> fns = new ArrayList<>();
		fns.addAll(cd.pkg.getFunctions());
		fns.addAll(cd.pkg.getIterators());
		//compile data types first, it's important
		for (Field field : cd.pkg.getFields()) {
			if (!field.isCompiled() && !field.isLibrary()) {
				if (field.getRawType()!=null) {
					field.setType(compileDataType(cd, field.getRawType()));
				}
			}
		}
		for (Function fn : fns) {
			cd.frame = new ScopeFrame(cd.pkg);
			if (fn.rawParams!=null) {
				int i = 0;
				for (Field v : fn.rawParams) {
					if (v.getRawType()!=null) {
						v.setType(compileDataType(cd, v.getRawType()));
					} else if (v.getType()==null) {
						v.setType(new DataType(true));
					}
					if (!(v.getType().type instanceof ParamTypeDef)) {
						ParamTypeDef ptd = new ParamTypeDef(v.getName(), i, v.getType().type);
						v.setType(new DataType(ptd));
					}
					cd.frame.setParam(v.getName(), v.getType());
					i++;
				}
			}
			if (!fn.isCompiled() && !fn.isLibrary()) {
				for (Field v : fn.getArguments()) {
					if (v.getRawType()!=null) {
						v.setType(compileDataType(cd, v.getRawType()));
					}
				}
			}
			if (fn.getReturn()!=null) {
				if (fn instanceof Iterator) {
					((Iterator)fn).iterReturns = compileIteratorReturnType(cd, ((Iterator)fn).getReturn());
				} else {
					fn.setReturnType(compileDataType(cd, fn.getReturn()));
				}
			}
			cd.frame = cd.frame.parent;
		}
		//now comp functions and fields for real
		for (Field field : cd.pkg.getFields()) {
			if (!field.isCompiled() && !field.isLibrary()) {
				cd.frame = new ScopeFrame(cd.pkg);
				compileField(cd, field);
				cd.frame = cd.frame.parent;
			}
		}
		for (Function fn : fns) {
			if (!fn.isCompiled() && !fn.isLibrary()) {
				cd.frame = new ScopeFrame(cd.pkg);
				compileFunction(cd, fn);
				cd.frame = cd.frame.parent;
			}
		}
		
		//inline stuff
		CompileUtils.transform(cd.pkg, fnInliner);
		CompileUtils.transform(cd.pkg, paramEraser);
		CompileUtils.transform(cd.pkg, nameConflictResolver);
		CompileUtils.transform(cd.pkg, optimizer);
		Optimizer.countUsages(pkg);
		
		return cd.errs;
	}
	
	public static Expression compileFunction(CompileData cd, Function fn) {
		cd.dirs = fn.getDirectives();
		for (Field v : fn.getArguments()) {
			cd.frame.putVariable(v.getName(), false/*!Directives.has(fn, "export")*/);
			if (v.getRawType()!=null) {
				cd.frame.setVarType(v.getName(), v.getType());
			}
		}
		if (fn.rawParams!=null) {
			for (Field v : fn.rawParams) {
				cd.frame.setParam(v.getName(), v.getType());
			}
		}
		Expression code = compileCode(cd, fn.rawData(), fn.getReturnType());
		for (Field v : fn.getArguments()) {
			code.add(0,new Operation(OpType.DEF, v.getName()));
		}
		fn.setCompiled(code);
		return code;
	}
	
	public static Expression compileField(CompileData cd, Field field) {
		cd.dirs = field.getDirectives();
		Variable v = cd.frame.putVariable(field.getName(), false);
		if (field.rawData()!=null) {
			Expression expr = compileExpr(cd, v.name, field.rawData());
			field.setCompiled(expr);
			if (field.getType()==null) {
				field.setType(expr.type);
			}
			return expr;
		}
		return null;
	}
	
	public static Expression compileCode(CompileData cd, ArrayList<Element> es) {
		return compileCode(cd, es, new DataType(true));
	}
	
	public static Expression compileCode(CompileData cd, ArrayList<Element> es, DataType retType) {
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
							if (!Directives.has(e, "inline")) {
								code.add(new Operation(OpType.DEF, cd.frame.getVarTypeNode(expr2), e.range, expr2));
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
									
									//check data types
									DataType rtype = lexprs.get(asni).type;
									DataType ltype = expr2.type;
									DataType newType = DataType.getNewType(ltype, rtype);
									if (!DataType.canCastTo(ltype,rtype)) {
										cd.errs.add(new SourceDataTypeException(e.range,"Cannot assign a value of type "+rtype+" to variable "+expr2.retVar+" of type "+ltype));
									} else {
										cd.frame.setVarType(expr2.retVar, newType);
										cd.frame.setVarType(lexprs.get(asni).retVar, newType);
									}
									
									movs.add(new Operation(OpType.MOV, newType, e2.range, expr2.retVar, names.get(asni)));
									code.addAll(lexprs.get(asni));
									rexprs.add(expr2);
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
						if (cond.type.type!=TypeDef.BOOLEAN) {
							cd.errs.add(new SourceDataTypeException(e.range,"Condition must be a boolean, got an item of type "+cond.type));
						}
						code.add(new Operation(OpType.DO, ifBlock.range));
						code.addAll(cond);
						code.add(new Operation(OpType.IF, ifBlock.range, cond.retVar));
						code.add(new Operation(OpType.BEGIN, ifBlock.range));
						cd.frame = new ScopeFrame(cd);
						code.addAll(compileCode(cd, (ArrayList<Element>) ifBlock.args[2]));
						cd.frame = cd.frame.parent;
						code.add(new Operation(OpType.END, ifBlock.range));
						int ends = 1;
						for (Element elif : (ArrayList<Element>) e.args[1]) {
							cond = compileExpr(cd, cd.frame.newVarName(), (Element) elif.args[0]);
							if (cond.type.type!=TypeDef.BOOLEAN) {
								cd.errs.add(new SourceDataTypeException(e.range,"Condition must be a boolean, got an item of type "+cond.type));
							}
							code.add(new Operation(OpType.ELSE, elif.range));
							code.add(new Operation(OpType.DO, elif.range));
							code.addAll(cond);
							code.add(new Operation(OpType.IF, elif.range, cond.retVar));
							code.add(new Operation(OpType.BEGIN, elif.range));
							cd.frame = new ScopeFrame(cd);
							code.addAll(compileCode(cd, (ArrayList<Element>) elif.args[2]));
							cd.frame = cd.frame.parent;
							code.add(new Operation(OpType.END, elif.range));
							ends++;
						}
						if (e.args[2]!=null) {
							Element elseBlock = (Element) e.args[2];
							code.add(new Operation(OpType.ELSE, elseBlock.range));
							code.add(new Operation(OpType.BEGIN, elseBlock.range));
							cd.frame = new ScopeFrame(cd);
							code.addAll(compileCode(cd, (ArrayList<Element>) elseBlock.args[2]));
							cd.frame = cd.frame.parent;
							code.add(new Operation(OpType.END, elseBlock.range));
						}
						for (int i=0;i<ends;i++) {
							code.add(new Operation(OpType.ENDB, e.range));
						}
						break;
					case WHILE:
						cond = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
						if (cond.type.type!=TypeDef.BOOLEAN) {
							cd.errs.add(new SourceDataTypeException(e.range,"Condition must be a boolean, got an item of type "+cond.type));
						}
						code.add(new Operation(OpType.DO, e.range));
						code.addAll(cond);
						code.add(new Operation(OpType.WHILE, e.range, cond.retVar));
						code.add(new Operation(OpType.BEGIN, e.range));
						cd.frame = new ScopeFrame(cd);
						code.addAll(compileCode(cd, (ArrayList<Element>) e.args[2]));
						cd.frame = cd.frame.parent;
						code.add(new Operation(OpType.END, e.range));
						code.add(new Operation(OpType.ENDB, e.range));
						break;
					case REPEAT:
						cond = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
						if (cond.type.type!=TypeDef.BOOLEAN) {
							cd.errs.add(new SourceDataTypeException(e.range,"Condition must be a boolean, got an item of type "+cond.type));
						}
						code.add(new Operation(OpType.DO, e.range));
						code.addAll(cond);
						code.add(new Operation(OpType.REP, e.range, cond.retVar));
						code.add(new Operation(OpType.BEGIN, e.range));
						cd.frame = new ScopeFrame(cd);
						code.addAll(compileCode(cd, (ArrayList<Element>) e.args[2]));
						cd.frame = cd.frame.parent;
						code.add(new Operation(OpType.END, e.range));
						code.add(new Operation(OpType.ENDB, e.range));
						break;
					case RETURN_NULL:
						code.add(new Operation(OpType.RET, e.range));
						break;
					case RETURN:
						ArrayList<String> rets = new ArrayList<>();
						cond = new Expression();
						if (((Element)e.args[0]).type==Rule.TUPLE || ((Element)e.args[0]).type==Rule.PAREN) {
							Element e2 = ((Element)e.args[0]);
							if (e2.type==Rule.PAREN && ((Element)e2.args[0]).type==Rule.TUPLE) {
								e2 = (Element) e2.args[0];
							}
							for (Element e3 : (ArrayList<Element>) e2.args[0]) {
								cond = compileExpr(cd, cd.frame.newVarName(), e3);
								code.addAll(cond);
								rets.add(cond.retVar);
							}
						} else {
							cond = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
							code.addAll(cond);
							rets.add(cond.retVar);
							
							if (!DataType.canCastTo(retType, cond.type)) {
								cd.errs.add(new SourceDataTypeException(e.range,"Return type is "+retType+", got type "+cond.type));
							}
						}
						
						code.add(new Operation(OpType.RET, cond.type,e.range, rets.toArray(new String[0])));
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
						
						if (!DataType.canCastTo(lexpr2.type,rexpr.type)) {
							cd.errs.add(new SourceDataTypeException(e.range,"Cannot assign a value of type "+rexpr.type+" to variable "+lexpr1.retVar+" of type "+lexpr1.type));
						}
									
						code.addAll(rexpr);
						code.addAll(lexpr2);
						code.add(new Operation(asnType, TypeDef.getCommonParent(lexpr2.type.type,rexpr.type.type), e.range, lexpr1.retVar, lexpr2.retVar, rexpr.retVar));
						code.addAll(lexpr1);
						break;
					case FOR:
						code.add(new Operation(OpType.DO, e.range));
						ArrayList<String> iterVars = new ArrayList<>();
						for (Element e2 : (ArrayList<Element>) e.args[0]) {
							String exprRaw = resolveLValueRaw(cd, e2);
							cd.frame.putDefined(exprRaw);
							cd.frame.setVarType(exprRaw, compileDataType(cd, e2.dataType));
							Expression expr2 = resolveLValue(cd, code, e2);
							iterVars.add(expr2.retVar);
							code.addAll(expr2);
						}
								
						if (e.args[1] instanceof Element && ((Element)e.args[1]).type==Rule.FCALL && "range".equals(((Element)e.args[1]).args[0])) {
							//for in range
							es = (ArrayList<Element>) ((Element)e.args[1]).args[1];
							if (es.size()>0 && es.get(0).type == Rule.TUPLE) {
								es = (ArrayList<Element>) es.get(0).args[0];
							}
							if (es.size()<2 || es.size()>3) {
								cd.errs.add(new SourceSyntaxException(e.range,"Function range must have 2 or 3 arguments"));
							} else {
								Expression begin = compileExpr(cd, cd.frame.newVarName(), es.get(0));
								Expression end = compileExpr(cd, cd.frame.newVarName(), es.get(1));
								Expression step = null;
								if (es.size()==3) {
									step = compileExpr(cd, cd.frame.newVarName(), es.get(2));
								}
								ArrayList<String> args = new ArrayList<>();
								if (iterVars.size()!=1) {
									cd.errs.add(new SourceSyntaxException(e.range,"Ranged for loop must only have 1 iterator variable"));
								}
								String iterVar = iterVars.get(0);
								cd.frame.putVariable(iterVar, false);
								args.add(iterVar);
								if (Directives.has(e, "downloop") && !Directives.has(e, "uploop")) {
									args.add("-1");
								} else if (!Directives.has(e, "downloop") && Directives.has(e, "uploop")) {
									args.add("1");
								} else {
									args.add("0");
								}
								args.add(begin.retVar);
								code.addAll(begin);
								args.add(end.retVar);
								code.addAll(end);
								if (step!=null) {
									args.add(step.retVar);
									code.addAll(step);
								}
								if (begin.type.type!=end.type.type) {
									cd.errs.add(new SourceDataTypeException(e.range,"The arguments of range must be of the same type"));
								}
								System.out.println(cd.frame.getVarTypeNode(iterVar));
								if (!DataType.canCastTo(cd.frame.getVarTypeNode(iterVar),begin.type)) {
									cd.errs.add(new SourceDataTypeException(e.range,"Cannot cast range iterator data type "+cd.frame.getVarTypeNode(iterVar)+" to "+begin.type));
								}
								cd.frame.setVarType(iterVar, begin.type);
								code.add(new Operation(OpType.FORR, begin.type, e.range, args.toArray(new String[0])));
								code.add(new Operation(OpType.BEGIN, e.range));
								cd.frame = new ScopeFrame(cd);
								code.addAll(compileCode(cd, (ArrayList<Element>) e.args[2]));
								cd.frame = cd.frame.parent;
								code.add(new Operation(OpType.END, e.range));
								code.add(new Operation(OpType.ENDB, e.range));
							}
						} else {
							boolean isIter = false;
							RealFunction riter = null;
							
							ArrayList<DataType> arga = new ArrayList<>();
							ArrayList<Expression> arge = new ArrayList<>();
							ArrayList<DataType> irets = new ArrayList<>();
							
							if (e.args[1] instanceof Element && ((Element)e.args[1]).type==Rule.FCALL) {
								String iterName = (String) ((Element)e.args[1]).args[0];
								
								es = (ArrayList<Element>) ((Element)e.args[1]).args[1];
								if (es.size()>0 && es.get(0).type == Rule.TUPLE) {
									es = (ArrayList<Element>) es.get(0).args[0];
								}
								for (Element e2 : es) {
									Expression expr2 = compileExpr(cd, cd.frame.newVarName(), e2);
									arga.add(expr2.type);
									arge.add(expr2);
								}
								
								riter = getRealFunction(cd, new FunctionCall(iterName, arga, irets, ((Element)e.args[1]).directives));
								
								if (riter.fn!=null) {
									isIter = true;
									
									if (riter.method) {
										Expression expr2 = new Expression();
										expr2.retVar = riter.methodOf;
										expr2.type = cd.frame.getVarTypeNode(riter.methodOf);
										arge.add(expr2);
									}
								}
							}
							
							if (isIter) {
								Iterator iter = (Iterator) riter.fn;
								int i = 0;
								ArrayList<String> iterVars2 = new ArrayList<>();
								for (Expression expr2 : arge) {
									code.addAll(expr2);
									iterVars2.add(expr2.retVar);
								}
								for (String var : iterVars) {
									cd.frame.setVarType(var, iter.iterReturns.get(i));
									iterVars2.add(var);
									i++;
								}
								iterVars2.add(0,iter.getFullName());
								code.add(new Operation(OpType.FORC, e.range, iterVars2.toArray(new String[0])));
								code.add(new Operation(OpType.BEGIN, e.range));
								cd.frame = new ScopeFrame(cd);
								code.addAll(compileCode(cd, (ArrayList<Element>) e.args[2]));
								cd.frame = cd.frame.parent;
								code.add(new Operation(OpType.END, e.range));
								code.add(new Operation(OpType.ENDB, e.range));
							} else {
								if (iterVars.size()!=1) {
									cd.errs.add(new SourceSyntaxException(e.range,"For-each loop must only have 1 iterator variable"));
								}
								Expression listExpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[1]);
								code.addAll(listExpr);
								String iterName = iterVars.get(0);
								if (listExpr.type==null) {
									listExpr.type = new DataType(true);
								}
								if (!listExpr.type.type.indexable) {
									cd.errs.add(new SourceDataTypeException(e.range,"Cannot iterate over data type "+listExpr.type));
								}
								DataType rtd = new DataType(listExpr.type.type.indexReturns);
								if (rtd.type instanceof ParamTypeDef && listExpr.type.params.length>((ParamTypeDef)rtd.type).paramNo) {
									rtd = listExpr.type.params[((ParamTypeDef)rtd.type).paramNo];
								}
								if (!DataType.canCastTo(cd.frame.getVarTypeNode(iterName), rtd)) {
									cd.errs.add(new SourceDataTypeException(e.range,"Cannot cast range iterator data type "+cd.frame.getVarTypeNode(iterName)+" to "+rtd));
								}
								cd.frame.setVarType(iterName, rtd);
								iterVars.add(0,listExpr.retVar);
								code.add(new Operation(OpType.FORE, rtd, e.range, iterVars.toArray(new String[0])));
								code.add(new Operation(OpType.BEGIN, e.range));
								cd.frame = new ScopeFrame(cd);
								code.addAll(compileCode(cd, (ArrayList<Element>) e.args[2]));
								cd.frame = cd.frame.parent;
								code.add(new Operation(OpType.END, e.range));
								code.add(new Operation(OpType.ENDB, e.range));
							}
						}
						break;
					default:
						code.addAll(compileExpr(cd, cd.frame.newVarName(), e));
				}
			}
		}
		//change types of known lvars to correct parent types
		for (Operation op : code) {
			if ((op.op.hasLVar() && op.op!=OpType.MOVL) || op.op==OpType.DEF) {
				DataType type = cd.frame.getVarTypeNode(op.args[0]);
				if (type!=null) {
					op.type = type.type;
				}
			}
		}
		return code;
	}
	
	public static Expression compileExpr(CompileData cd, String retVar, Element e) {
		Expression expr = new Expression();
		expr.retVar = retVar;
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case NUMBER:
					if (StringUtils.isReal((String) e.args[0])) {
						expr.type = new DataType(TypeDef.REAL,true);
					} else {
						expr.type = new DataType(TypeDef.INT,true);
					}
					expr.add(new Operation(OpType.MOVN, expr.type.type, e.range, retVar, (String)e.args[0]));
					break;
				case STRING:
					expr.add(new Operation(OpType.MOVS,TypeDef.STRING, e.range, retVar, (String)e.args[0]));
					expr.type = new DataType(TypeDef.STRING,true);
					break;
				case CHAR:
					expr.type = new DataType(TypeDef.INT,true);
					expr.add(new Operation(OpType.MOVN, expr.type.type, e.range, retVar, String.valueOf((int) ((String)e.args[0]).charAt(0))));
					break;
				case WORD:
					return CompileLookup.rvalLookup(cd, retVar, e);
			}
		} else {
			if (OpType.MathToOpType((Rule) e.type)!=null && OpType.MathToOpType((Rule) e.type).isMathOp()) {
				Expression lexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
				Expression rexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[1]);
				expr.addAll(lexpr);
				expr.addAll(rexpr);
				OpType opt = OpType.MathToOpType((Rule) e.type);
					
				DataType rtype = lexpr.type;
				DataType ltype = rexpr.type;
				if (rtype==null) {
					rtype = new DataType(true);
				}
				if (ltype==null) {
					ltype = new DataType(true);
				}
				if (TypeDef.getCommonParent(rtype.type, ltype.type)!=ltype.type && TypeDef.getCommonParent(ltype.type, rtype.type)!=rtype.type) {
					if (opt!=OpType.CONCAT) {
						cd.errs.add(new SourceDataTypeException(e.range,"Types "+ltype+" and "+rtype+" are not equatable"));
					}
				} else {
					if (opt.isBooleanMathOp()) {
						expr.type = new DataType(TypeDef.BOOLEAN);
					} else {
						expr.type = DataType.commonType(ltype, rtype);
					}
				}
				
				if (opt==OpType.CONCAT) {
					expr.type = new DataType(TypeDef.STRING);
				}
				
				expr.add(new Operation(opt, expr.type.type, e.range, retVar, lexpr.retVar, rexpr.retVar));
			} else {
				ArrayList<Element> es;
				switch ((Rule)e.type) {
					case FCALL:
					case CHAIN:
						return CompileLookup.rvalLookup(cd, retVar, e.range, e);
					case ICALL:
						ArrayList<String> names = new ArrayList<>();
						ArrayList<Expression> exprs = new ArrayList<>();
						es = (ArrayList<Element>) e.args[1];
						if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
							es = (ArrayList<Element>) es.get(0).args[0];
						}
						for (Element e2 : es) {
							String name = cd.frame.newVarName();
							Expression expr2 = compileExpr(cd, name, e2);
							exprs.add(expr2);
							names.add(name);
						}
						Element listE = new Element(e.range, TokenRule.WORD);
						listE.args[0] = e.args[0];
						Expression listExpr = resolveLValue(cd, expr, listE);
						ArrayList<DataType> arga = new ArrayList<>();
						arga.add(listExpr.type);
						for (Expression expr3 : exprs) {
							arga.add(expr3.type);
						}
						RealFunction rfn = getRealFunction(cd, new FunctionCall(listExpr.type.type.name+"._getindex", arga, new DataType(true), e.directives));
						if (rfn.fn!=null) {
							int i = 0;
							if (rfn.fn.getArguments().size()-1!=exprs.size()) {
								cd.errs.add(new SourceDataTypeException(e.range, "Data type "+listExpr.type+" expected "+(rfn.fn.getArguments().size()-1)+" indices, got "+exprs.size()));
							} else {
								for (Expression expr3 : exprs) {
									if (!DataType.canCastTo(expr3.type, rfn.fn.getArguments().get(i+1).getType())) {
										cd.errs.add(new SourceDataTypeException(e.range, "Cannot index data type "+listExpr.type+" with a value of data type "+expr3.type));
									}
									expr.addAll(expr3);
									i++;
								}
							}
							names.add(0,listExpr.retVar);
							names.add(0,rfn.fn.getFullName());
							names.add(0,retVar);
							expr.type = rfn.fn.getReturnType();
							expr.add(new Operation(OpType.CALL, expr.type.type, e.range, names.toArray(new String[0])));
							expr.addAll(listExpr);
						} else if (listExpr.type!=null && listExpr.type.type.indexable) {
							int i = 0;
							if (!listExpr.type.type.varargIndex && listExpr.type.type.indexableBy.length!=exprs.size()) {
								cd.errs.add(new SourceDataTypeException(e.range, "Data type "+listExpr.type+" expected "+listExpr.type.type.indexableBy.length+" indices, got "+exprs.size()));
							} else {
								for (Expression expr3 : exprs) {
									TypeDef argtd = listExpr.type.type.indexableBy[i];
									if (argtd instanceof ParamTypeDef) {
										argtd = listExpr.type.type.params[((ParamTypeDef)argtd).paramNo];
									}
									if (!DataType.canCastTo(expr3.type, new DataType(argtd))) {
										cd.errs.add(new SourceDataTypeException(e.range, "Cannot index data type "+listExpr.type+" with a value of data type "+expr3.type));
									}
									expr.addAll(expr3);
									i++;
								}
							}
							names.add(0,listExpr.retVar);
							names.add(0,retVar);
							DataType rtd = new DataType(listExpr.type.type.indexReturns);
							if (rtd.type instanceof ParamTypeDef) {
								rtd = ((ParamTypeDef)rtd.type).paramNo>=listExpr.type.params.length?null:listExpr.type.params[((ParamTypeDef)rtd.type).paramNo];
							}
							expr.type = rtd;
							expr.add(new Operation(OpType.INDEX, expr.type, e.range, names.toArray(new String[0])));
							expr.addAll(listExpr);
						} else {
							if (rfn.nameFound) {
								cd.errs.add(new SourceDataTypeException(e.range, "Cannot index data type "+listExpr.type+"; no overload _getindex found"));
							} else {
								cd.errs.add(new SourceDataTypeException(e.range, "Cannot index data type "+listExpr.type));
							}
						}
						break;
					case INDEX:
						names = new ArrayList<>();
						es = (ArrayList<Element>) e.args[0];
						if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
							es = (ArrayList<Element>) es.get(0).args[0];
						} else if (es.size()>1) {
							cd.errs.add(new SourceSyntaxException(e.range, "Illegal list format"));
						}
						DataType common = null;
						for (Element e2 : es) {
							String name = cd.frame.newVarName();
							Expression expr2 = compileExpr(cd, name, e2);
							expr.addAll(expr2);
							names.add(name);
							
							if (common==null) {
								common = expr2.type;
							} else {
								common = DataType.commonType(common, expr2.type);
							}
						}
						if (common==null) {
							common = new DataType();
						}
						
						names.add(0,retVar);
						expr.add(new Operation(OpType.MOVL, common, e.range, names.toArray(new String[0])));
						expr.type = new DataType(TypeDef.LIST,true);
						expr.type.params = new DataType[] {common};
						break;
					case PAREN:
						es = (ArrayList<Element>) e.args[0];
						if (es.size()!=1) {
							cd.errs.add(new SourceSyntaxException(e.range, "Illegal parenthesis format"));
						}
						for (Element e2 : es) {
							Expression pexpr = compileExpr(cd, retVar, e2);
							expr.addAll(pexpr);
							expr.type = pexpr.type;
						}
						break;
					case TRUE:
						expr.add(new Operation(OpType.TRUE, TypeDef.BOOLEAN, e.range, retVar));
						expr.type = new DataType(TypeDef.BOOLEAN,true);
						break;
					case FALSE:
						expr.add(new Operation(OpType.FALSE, TypeDef.BOOLEAN, e.range, retVar));
						expr.type = new DataType(TypeDef.BOOLEAN,true);
						break;
					case NOT:
						Expression nexpr = compileExpr(cd, cd.pkg.nameProvider.getTempName(), (Element) e.args[0]);
						expr.addAll(nexpr);
						expr.add(new Operation(OpType.NOT, TypeDef.BOOLEAN, e.range, retVar, nexpr.retVar));
						expr.type = new DataType(TypeDef.BOOLEAN);
						break;
					case NEG:
						nexpr = compileExpr(cd, cd.pkg.nameProvider.getTempName(), (Element) e.args[0]);
						expr.addAll(nexpr);
						expr.add(new Operation(OpType.NEG, nexpr.type.type, e.range, retVar, nexpr.retVar));
						expr.type = nexpr.type;
						break;
					case TO:
						String name = cd.frame.newVarName();
						Expression lexpr = compileExpr(cd, name, (Element) e.args[0]);
						expr.addAll(lexpr);
						DataType rtype = compileDataType(cd, (Element) e.args[1]);
						String fnName = rtype.type.name+"._cast";
						ArrayList<DataType> argl = new ArrayList<>();
						argl.add(lexpr.type);
						rfn = getRealFunction(cd, new FunctionCall(fnName, argl, rtype, e.directives));
						if (rfn.fn==null) {
							cd.errs.add(new SourceUndefinedFunctionException(e.range, "No conversion function from type "+lexpr.type+" to type "+rtype+" exists", rtype.type.name+"._cast"));
						} else {
							expr.add(new Operation(OpType.CALL, rtype.type, e.range, retVar, rfn.fn.getFullName(), name));
							expr.type = rtype;
						}
						break;
				}
			}
		}
		if (e.dataType!=null) {
			expr.type = compileDataType(cd, e.dataType);
		}
		//change types of known lvars to correct parent types
		for (Operation op : expr) {
			if ((op.op.hasLVar() && op.op!=OpType.MOVL) || op.op==OpType.DEF) {
				DataType type = cd.frame.getVarTypeNode(op.args[0]);
				if (type!=null) {
					op.type = type.type;
				}
			}
		}
		return expr;
	}
	
	public static DataType compileDataType(CompileData cd, Element e) {
		if (e==null) {
			return new DataType(true);
		}
		DataType dt = new DataType();
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					TypeDef def = cd.pkg.getType((String) e.args[0]);
					if (def==null) {
						DataType type = cd.frame.getParam((String) e.args[0]);
						if (type==null) {
							cd.errs.add(new SourceDataTypeException(e.range, "unknown data type "+e.args[0]));
						} else {
							def = type.type;
						}
					}
					dt.type = def;
					break;
				default:
					cd.errs.add(new SourceSyntaxException(e.range, "Illegal data type format"));
			}
		} else {
			switch ((Rule)e.type) {
				case ICALL:
					TypeDef def = cd.pkg.getType((String) e.args[0]);
					if (def==null) {
						DataType type = cd.frame.getParam((String) e.args[0]);
						if (type==null) {
							cd.errs.add(new SourceDataTypeException(e.range, "unknown data type "+e.args[0]));
							break;
						} else {
							def = type.type;
						}
					}
					dt.type = def;
					if (!def.hasParams) {
						cd.errs.add(new SourceSyntaxException(e.range, "Cannot parameterize type "+def));
						break;
					}
					ArrayList<DataType> pList = new ArrayList<>();
					ArrayList<Element> es = (ArrayList<Element>) e.args[1];
					if (es.size()>0 && es.get(0).type==Rule.TUPLE) {
						es = (ArrayList<Element>) es.get(0).args[0];
					}
					if (!def.varargParams && def.params.length!=es.size()) {
						cd.errs.add(new SourceSyntaxException(e.range, "Type "+def+" expects "+def.params.length+" parameters, got "+es.size()));
					}
					for (Element e2 : es) {
						DataType param = compileDataType(cd, e2);
						pList.add(param);
					}
					dt.params = pList.toArray(dt.params);
					break;
				default:
					cd.errs.add(new SourceSyntaxException(e.range, "Illegal data type format"));
			}
		}
		return dt;
	}
	
	public static ArrayList<DataType> compileIteratorReturnType(CompileData cd, Element e) {
		ArrayList<DataType> a = new ArrayList<>();
		if (e.type==Rule.TUPLE || e.type==Rule.PAREN) {
			for (Element e2 : (ArrayList<Element>) e.args[0]) {
				a.addAll(compileIteratorReturnType(cd, e2));
			}
		} else {
			a.add(compileDataType(cd, e));
		}
		return a;
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
					expr.type = cd.frame.getVarTypeNode(name);
					break;
				default:
					cd.errs.add(new SourceSyntaxException(e.range,"Illegal L-value"));
			}
		} else {
			switch ((Rule)e.type) {
				case ICALL:
					ArrayList<String> names = new ArrayList<>();
					ArrayList<Expression> exprs = new ArrayList<>();
					ArrayList<Element> es = (ArrayList<Element>) e.args[1];
					if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
						es = (ArrayList<Element>) es.get(0).args[0];
					}
					for (Element e2 : es) {
						String name2 = cd.frame.newVarName();
						Expression expr2 = compileExpr(cd, name2, e2);
						exprs.add(expr2);
						names.add(name2);
					}
					Element listE = new Element(e.range, TokenRule.WORD);
					listE.args[0] = e.args[0];
					Expression listExpr = resolveLValue(cd, expr, listE);
					ArrayList<DataType> arga = new ArrayList<>();
					arga.add(listExpr.type);
					arga.add(new DataType(true)); //TODO: make it so we KNOW this
					for (Expression expr3 : exprs) {
						arga.add(expr3.type);
					}
					RealFunction rfn = getRealFunction(cd, new FunctionCall(listExpr.type.type.name+"._setindex", arga, listExpr.type, e.directives));
					if (rfn.fn!=null) {
						int i = 0;
						if (rfn.fn.getArguments().size()-2!=exprs.size()) {
							cd.errs.add(new SourceDataTypeException(e.range, "Data type "+listExpr.type+" expected "+(rfn.fn.getArguments().size()-2)+" indices, got "+exprs.size()));
						} else {
							for (Expression expr3 : exprs) {
								if (!DataType.canCastTo(expr3.type, rfn.fn.getArguments().get(i+2).getType())) {
									cd.errs.add(new SourceDataTypeException(e.range, "Cannot index data type "+listExpr.type+" with a value of data type "+expr3.type));
								}
								code.addAll(expr3);
								i++;
							}
						}
						expr.retVar = cd.pkg.nameProvider.getTempName();
						names.add(0,expr.retVar);
						names.add(0,listExpr.retVar);
						names.add(0,rfn.fn.getFullName());
						names.add(0,listExpr.retVar);
						expr.add(new Operation(OpType.CALL, expr.type.type, e.range, names.toArray(new String[0])));
						expr.addAll(listExpr);
					} else if (listExpr.type!=null && listExpr.type.type.indexable) {
						if (!listExpr.type.type.varargIndex && listExpr.type.type.indexableBy.length!=exprs.size()) {
							cd.errs.add(new SourceDataTypeException(e.range, "Data type "+listExpr.type+" expected "+listExpr.type.type.indexableBy.length+" indices, got "+exprs.size()));
						} else {
							int i = 0;
							for (Expression expr3 : exprs) {
								DataType rtd = new DataType(listExpr.type.type.indexableBy[i]);
								if (rtd.type instanceof ParamTypeDef) {
									rtd = listExpr.type.params[((ParamTypeDef)rtd.type).paramNo];
								}
								expr.type = rtd;
								if (!DataType.canCastTo(expr3.type, rtd)) {
									cd.errs.add(new SourceDataTypeException(e.range, "Cannot index data type "+listExpr.type+" with a value of data type "+expr3.type));
								}
								code.addAll(expr3);
								i++;
							}
						}
						expr.retVar = cd.pkg.nameProvider.getTempName();
						DataType rtd = new DataType(listExpr.type.type.indexReturns);
						if (rtd.type instanceof ParamTypeDef) {
							rtd = listExpr.type.params[((ParamTypeDef)rtd.type).paramNo];
						}
						expr.type = rtd;
						names.add(0,expr.retVar);
						names.add(0,listExpr.retVar);
						expr.add(new Operation(OpType.MOVI, expr.type.type, e.range, names.toArray(new String[0])));
						expr.addAll(listExpr);
					} else {
						if (rfn.nameFound) {
							cd.errs.add(new SourceDataTypeException(e.range, "Cannot index data type "+listExpr.type+"; no overload _setindex found"));
						} else {
							cd.errs.add(new SourceDataTypeException(e.range, "Cannot index data type "+listExpr.type));
						}
					}
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
	
	public static RealFunction getRealFunction(CompileData cd, FunctionCall call) {
		String[] subs = call.name.split("\\.");
		String fnToCall = call.name;
		for (int i = subs.length-1;i>=0;i--) {
			String pkgName = "";
			for (String str : Arrays.copyOfRange(subs, 0, i)) {
				if (!pkgName.isEmpty()) {
					pkgName+=".";
				}
				pkgName+=str;
			}
			String fnName = "";
			for (String str : Arrays.copyOfRange(subs, i, subs.length)) {
				if (!fnName.isEmpty()) {
					fnName+=".";
				}
				fnName+=str;
			}
			
			if (cd.frame.isDefined(pkgName) || cd.frame.isInlined(pkgName) || cd.pkg.getField(pkgName)!=null) {
				DataType type;
				if (cd.frame.isDefined(pkgName)) {
					type = cd.frame.getVarTypeNode(pkgName);
					if (type==null) {
						type = new DataType(true);
					}
				} else if (cd.frame.isInlined(pkgName)) {
					type = cd.frame.getVarTypeNode(pkgName);
					if (type==null) {
						type = compileExpr(cd,"",cd.frame.getInline(pkgName)).type;
						if (type==null) {
							type = new DataType(true);
						}
					}
				} else {
					type = cd.pkg.getField(pkgName).getType();
					if (type==null) {
						type = new DataType(true);
					}
				}
				TypeDef otype = type.type;
				while (type.type!=null) {
					fnToCall = type.type.name+"."+fnName;
					call.args.add(0,type);
					if (cd.pkg.getFunction(fnToCall,call)!=null) {
						type.type = otype;
						return new RealFunction(cd.pkg.getFunction(fnToCall,call),pkgName,true);
					}
					call.args.remove(0);
					type.type = type.type.parent;
				}
				type.type = otype;
			} else {
				fnToCall = (!pkgName.isEmpty()?(pkgName+"."):"")+fnName;
				if (cd.pkg.getFunction(fnToCall,call)!=null) {
					return new RealFunction(cd.pkg.getFunction(fnToCall,call),null,true);
				}
			}
		}
		return new RealFunction(null,null,cd.pkg.getFunction(fnToCall)!=null);
	}
		
	public static CodeTransformer fnInliner = (pkg, work, code) -> {
		ArrayList<Operation> a = new ArrayList<>();
		for (int ii=0;ii<code.size();ii++) {
			Operation op = code.get(ii);
			
			if (op.op == OpType.CALL) {
				Function fn = pkg.getFunction(op.args[1]);
				if (fn!=null && Directives.has(fn,"inline")) {
					a.add(new Operation(OpType.BEGIN, op.range));
					CompileData cd = new CompileData(pkg);
					cd.dirs = fn.getDirectives();
					cd.frame = new ScopeFrame(pkg);
					ArrayList<Operation> code2 = compileFunction(cd, fn);
					
					for (int k=0;k<code2.size();k++) {
						if (code2.get(k).op!=OpType.DEF) {
							for (int i=2;i<op.args.length;i++) {
								code2.add(k,new Operation(OpType.MOV, fn.getArguments().get(i-2).getType(), op.range, fn.getArguments().get(i-2).getName(), op.args[i]));
							}
							break;
						}
					}
					
					String label = pkg.nameProvider.getTempName();
					boolean labelUsed = false;
					for (Operation op2 : code2) {
						if (op2.op==OpType.RET) {
							if (op2.args.length!=0) {
								a.add(new Operation(OpType.MOV, op2.type, op2.range, op.args[0], op2.args[0]));
							}
							a.add(new Operation(OpType.GOTO, op2.range, label));
							labelUsed = true;
						} else {
							a.add(op2);
						}
					}
					if (labelUsed) {
						a.add(new Operation(OpType.LABEL, op.range, label));
					}
					a.add(new Operation(OpType.END, op.range));
				} else {
					a.add(op);
				}
			} else {
				a.add(op);
			}
		}
		return a;
	};
	
	public static CodeTransformer paramEraser = (pkg, work, code) -> {
		ArrayList<Operation> a = new ArrayList<>();
		for (int ii=0;ii<code.size();ii++) {
			Operation op = code.get(ii);

			if (op.type instanceof ParamTypeDef) {
				op.type = op.type.parent;
			}
			a.add(op);
		}
		return a;
	};
	
	public static CodeTransformer optimizer = (pkg, work, code) -> {
		if (work instanceof IDirectable) {
			if (Directives.has((IDirectable) work, "!optimize")) {
				return code;
			}
		}
		return Optimizer.optimize(pkg, code);
	};
	
	public static CodeTransformer nameConflictResolver = (pkg, work, code) -> {
		class Frame {
			public Frame parent;
			public HashSet<String> defs = new HashSet<>();
			public HashMap<String,String> map = new HashMap<>();

			public Frame(Frame parent) {
				this.parent = parent;
			}
			
			public void set(String var) {
				defs.add(var);
			}
			
			public boolean get(String var) {
				if (defs.contains(var)) {
					return true;
				} else if (parent==null) {
					return false;
				} else {
					return parent.get(var);
				}
			}
			
			public void setMap(String var1, String var2) {
				map.put(var1, var2);
			}
			
			public String getMap(String var) {
				if (map.get(var)!=null) {
					return map.get(var);
				} else if (parent==null) {
					return null;
				} else {
					return parent.getMap(var);
				}
			}
			
			public boolean isMap(String var) {
				return getMap(var)!=null;
			}
			
			public boolean overriden(String var) {
				if (parent==null) {
					return false;
				}
				return parent.get(var) && get(var);
			}
		}
		
		Frame f = new Frame(null);
		ArrayList<Operation> a = new ArrayList<>();
		for (int ii=0;ii<code.size();ii++) {
			Operation op = code.get(ii).cloneOp();

			if (op.op == OpType.DEF) {
				f.set(op.args[0]);
				if (f.overriden(op.args[0])) {
					f.setMap(op.args[0],pkg.nameProvider.getTempName());
				}
			} else if (op.op == OpType.BEGIN) {
				f = new Frame(f);
				a.add(op);
			} else if (op.op == OpType.END) {
				f = f.parent;
				a.add(op);
			} else {
				int arg = 0;
				for (Boolean b : op.getVarSlots()) {
					if (b && f.isMap(op.args[arg])) {
						op.args[arg] = f.getMap(op.args[arg]);
					}
					arg++;
				}
				a.add(op);
			}
		}
		return a;
	};
}