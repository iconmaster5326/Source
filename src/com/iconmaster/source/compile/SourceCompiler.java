package com.iconmaster.source.compile;

import com.iconmaster.source.compile.CompileLookup.LookupFunction;
import com.iconmaster.source.compile.CompileUtils.CodeTransformer;
import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceDataTypeException;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.exception.SourceSafeModeException;
import com.iconmaster.source.exception.SourceSyntaxException;
import com.iconmaster.source.exception.SourceUndefinedFunctionException;
import com.iconmaster.source.exception.SourceUndefinedVariableException;
import com.iconmaster.source.prototype.CustomType;
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
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author iconmaster
 */
public class SourceCompiler {
	public static ArrayList<SourceException> compile(SourcePackage pkg) {
		CompileData cd = new CompileData(pkg);
		
		//compile custom types
		for (CustomType ct : cd.pkg.getCustomTypes()) {
			if (ct.rawType!=null) {
				DataType dt = compileDataType(cd, ct.rawType);
				ct.parent = dt.type;
			}
		}
		
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
		checkPrivacy(cd);
		
		return cd.errs;
	}
	
	public static Expression compileFunction(CompileData cd, Function fn) {
		cd.dirs = fn.getDirectives();
		for (Field v : fn.getArguments()) {
			cd.frame.putVariable(v.getName());
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
		cd.frame.putVariable(field.getName());
		if (field.rawData()!=null) {
			Expression expr = compileExpr(cd, field.getName(), field.rawData());
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
		String asnType = null;
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
						if (Directives.has(e, "const")) {
							ArrayList<Element> les = (ArrayList<Element>) e.args[0];
							
							for (Element e2 : les) {
								String expr2 = resolveLValueRaw(cd, e2);
								cd.frame.putConst(expr2);
							}
						}
						for (Element e2 : (ArrayList<Element>) e.args[0]) {
							String expr2 = resolveLValueRaw(cd, e2);
							cd.frame.putDefined(expr2);
							if (Directives.has(e2, "const")) {
								cd.frame.putConst(expr2);
							}
							if (e2.dataType!=null) {
								cd.frame.setVarType(expr2, compileDataType(cd, e2.dataType));
							} else {
								if (Directives.has(cd.dirs, "safe")) {
									cd.errs.add(new SourceSafeModeException(e.range,"Variable "+expr2+" was not given a type (@safe mode is on)", expr2));
								}
							}
							if (!Directives.has(e, "inline")) {
								code.add(new Operation(OpType.DEF, cd.frame.getVarType(expr2), e.range, expr2));
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
									DataType rtype = lexprs.get(asni).type;
									Expression expr2 = resolveLValue(cd, code, e2, rtype);
									
									//check data types
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
						asnType = "_add";
					case SUB_ASN:
						if (e.type==Rule.SUB_ASN) {
							asnType = "_sub";
						}
					case MUL_ASN:
						if (e.type==Rule.MUL_ASN) {
							asnType = "_mul";
						}
					case DIV_ASN:
						if (e.type==Rule.DIV_ASN) {
							asnType = "_div";
						}
						Expression rexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[1]);
						Expression lexpr1 = resolveLValue(cd, code, (Element) e.args[0], rexpr.type);
						Expression lexpr2 = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
						
						ArrayList<DataType> a = new ArrayList<>();
						a.add(lexpr2.type);
						a.add(rexpr.type);
						
						retType = DataType.commonType(lexpr2.type, rexpr.type);
						
						Function fn;
						TypeDef td = lexpr2.type.type;
						do {
							fn = cd.pkg.getFunction(td.name+"."+asnType, new FunctionCall(asnType, a, retType, e.directives));
							td = td.parent;
						} while (fn == null && td != null);
									
						code.addAll(rexpr);
						code.addAll(lexpr2);
						
						if (fn==null) {
							cd.errs.add(new SourceDataTypeException(e.range,"Cannot perform operation "+asnType+" on types "+lexpr2.type+" and "+rexpr.type));
						} else {
							code.add(new Operation(OpType.CALL, lexpr1.retVar, fn.getFullName(), lexpr2.retVar, rexpr.retVar));
						}
						
						code.addAll(lexpr1);
						break;
					case FOR:
						ArrayList<String> forVars = new ArrayList<>();
						es = (ArrayList<Element>) e.args[0];
						for (Element e2 : es) {
							String var = resolveLValueRaw(cd, e2);
							forVars.add(var);
							cd.frame.putVariable(var);
							if (e2.dataType!=null) {
								cd.frame.setVarType(var, compileDataType(cd, e2.dataType));
							} else {
								cd.frame.setVarType(var, new DataType(true));
							}
						}
						Expression iterExpr = CompileLookup.iteratorLookup(cd, null, (Element) e.args[1]);
						code.add(new Operation(OpType.DO, e.range));
						code.addAll(iterExpr);
						code.add(new Operation(OpType.FOR, (TypeDef) null, e.range, forVars.toArray(new String[0])));
						code.add(new Operation(OpType.BEGIN, e.range));
						code.addAll(compileCode(cd, (ArrayList<Element>) e.args[2]));
						code.add(new Operation(OpType.END, e.range));
						code.add(new Operation(OpType.ENDB, e.range));
						
						break;
					case BREAK:
						code.add(new Operation(OpType.BRK, e.range));
						break;
					case CONTINUE:
						code.add(new Operation(OpType.CONT, e.range));
						break;
					default:
						code.addAll(compileExpr(cd, cd.frame.newVarName(), e));
				}
			}
		}
		//change types of known lvars to correct parent types
		for (Operation op : code) {
			if ((op.op.hasLVar() && op.op!=OpType.MOVL && op.op!=OpType.MOVA) || op.op==OpType.DEF) {
				DataType type = cd.frame.getVarType(op.args[0]);
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
					expr.add(new Operation(OpType.MOVN, expr.type, e.range, retVar, (String)e.args[0]));
					break;
				case STRING:
					expr.add(new Operation(OpType.MOVS,TypeDef.STRING, e.range, retVar, (String)e.args[0]));
					expr.type = new DataType(TypeDef.STRING,true);
					break;
				case CHAR:
					expr.type = new DataType(TypeDef.CHAR,true);
					expr.add(new Operation(OpType.MOVN, expr.type.type, e.range, retVar, String.valueOf((int) ((String)e.args[0]).charAt(0))));
					break;
				case WORD:
					return CompileLookup.rvalLookup(cd, retVar, e);
			}
		} else {
			String callName = StringUtils.mathElementToString(e);
			if (callName!=null) {
				Expression lexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
				Expression rexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[1]);
				
				expr.addAll(lexpr);
				expr.addAll(rexpr);
				
				ArrayList<DataType> a = new ArrayList<>();
				a.add(lexpr.type);
				a.add(rexpr.type);
				
				DataType retType = DataType.commonType(lexpr.type, rexpr.type);
				
				Function fn;
				TypeDef td = lexpr.type.type;
				do {
					fn = cd.pkg.getFunction(td.name+"."+callName, new FunctionCall(callName, a, retType, e.directives));
					td = td.parent;
				} while (fn == null && td != null);
				
				if (fn==null) {
					cd.errs.add(new SourceDataTypeException(e.range,"Cannot perform operation "+e.type+" on types "+lexpr.type+" and "+rexpr.type));
				} else {
					expr.add(new Operation(OpType.CALL, retVar, fn.getFullName(), lexpr.retVar, rexpr.retVar));
					expr.type = fn.getReturnType();
				}
			} else {
				ArrayList<Element> es;
				switch ((Rule)e.type) {
					case FCALL:
					case ICALL:
					case CHAIN:
					case ICALL_REF:
						return CompileLookup.rvalLookup(cd, retVar, e.range, e);
					case NEW:
						String instName = (String) e.args[0];
						es = (ArrayList<Element>) e.args[1];
						if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
							es = (ArrayList<Element>) es.get(0).args[0];
						}
						TypeDef instType = cd.pkg.getType(instName);
						expr.add(new Operation(OpType.NEW, instType, e.range, retVar, instType.name));
						expr.type = new DataType(instType);
						
						ArrayList<DataType> args = new ArrayList<>();
						args.add(new DataType(instType));
						ArrayList<String> names = new ArrayList<>();
						for (Element e2 : es) {
							Expression aexpr = compileExpr(cd, cd.frame.newVarName(), e2);
							expr.addAll(aexpr);
							names.add(aexpr.retVar);
							args.add(aexpr.type);
						}
						
						FunctionCall fcall = new FunctionCall(instType.name+"._new", args, new DataType(true), e.directives);
						Function fn = cd.pkg.getFunction(fcall.name, fcall);
						
						if (fn==null) {
							ArrayList<Function> fns = cd.pkg.getFunctions(fcall.name);
							if (!names.isEmpty() || !fns.isEmpty()) {
								cd.errs.add(new SourceUndefinedFunctionException(e.range, "Could not find constructor for type "+instType, fcall.name));
							}
						} else {
							names.add(0,retVar);
							names.add(0,fn.getFullName());
							names.add(0,cd.frame.newVarName());
							expr.add(new Operation(OpType.CALL, instType, e.range, names.toArray(new String[0])));
						}
						break;
					case DYN_INDEX:
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
						
						OpType ot;
						if (e.type==Rule.DYN_INDEX) {
							ot = OpType.MOVL;
							expr.type = new DataType(TypeDef.LIST,true);
						} else {
							ot = OpType.MOVA;
							expr.type = new DataType(TypeDef.ARRAY,true);
						}
						expr.type.params = new DataType[] {common};
						
						names.add(0,retVar);
						expr.add(new Operation(ot, common, e.range, names.toArray(new String[0])));
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
						callName = "_not";
						Expression lexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);

						expr.addAll(lexpr);

						ArrayList<DataType> a = new ArrayList<>();
						a.add(lexpr.type);

						DataType retType = lexpr.type;

						fn = null;
						TypeDef td = lexpr.type.type;
						do {
							fn = cd.pkg.getFunction(td.name+"."+callName, new FunctionCall(callName, a, retType, e.directives));
							td = td.parent;
						} while (fn == null && td != null);

						if (fn==null) {
							cd.errs.add(new SourceDataTypeException(e.range,"Cannot perform operation "+e.type+" on types "+lexpr.type));
						} else {
							expr.add(new Operation(OpType.CALL, retVar, fn.getFullName(), lexpr.retVar));
							expr.type = fn.getReturnType();
						}
						break;
					case NEG:
						callName = "_neg";
						lexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);

						expr.addAll(lexpr);

						a = new ArrayList<>();
						a.add(lexpr.type);

						retType = lexpr.type;

						fn = null;
						td = lexpr.type.type;
						do {
							fn = cd.pkg.getFunction(td.name+"."+callName, new FunctionCall(callName, a, retType, e.directives));
							td = td.parent;
						} while (fn == null && td != null);

						if (fn==null) {
							cd.errs.add(new SourceDataTypeException(e.range,"Cannot perform operation "+e.type+" on types "+lexpr.type));
						} else {
							expr.add(new Operation(OpType.CALL, retVar, fn.getFullName(), lexpr.retVar));
							expr.type = fn.getReturnType();
						}
						break;
					case BIT_NOT:
						callName = "_bnot";
						lexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);

						expr.addAll(lexpr);

						a = new ArrayList<>();
						a.add(lexpr.type);

						retType = lexpr.type;

						fn = null;
						td = lexpr.type.type;
						do {
							fn = cd.pkg.getFunction(td.name+"."+callName, new FunctionCall(callName, a, retType, e.directives));
							td = td.parent;
						} while (fn == null && td != null);

						if (fn==null) {
							cd.errs.add(new SourceDataTypeException(e.range,"Cannot perform operation "+e.type+" on types "+lexpr.type));
						} else {
							expr.add(new Operation(OpType.CALL, retVar, fn.getFullName(), lexpr.retVar));
							expr.type = fn.getReturnType();
						}
						break;
					case TO:
						String name = cd.frame.newVarName();
						lexpr = compileExpr(cd, name, (Element) e.args[0]);
						expr.addAll(lexpr);
						DataType rtype = compileDataType(cd, (Element) e.args[1]);
						String fnName = rtype.type.name+"._cast";
						ArrayList<Expression> argl = new ArrayList<>();
						argl.add(lexpr);
						Expression rfn = CompileLookup.rvalLookup(cd, retVar, new LookupFunction(fnName, argl, rtype, e.directives));
						expr.addAll(rfn);
						expr.type = rtype;
						break;
					case RAW_EQ:
						lexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
						Expression rexpr = compileExpr(cd, cd.frame.newVarName(), (Element) e.args[1]);
						
						expr.addAll(lexpr);
						expr.addAll(rexpr);
						
						expr.add(new Operation(OpType.RAWEQ, TypeDef.BOOLEAN, e.range, retVar, lexpr.retVar, rexpr.retVar));
						expr.type = new DataType(TypeDef.BOOLEAN);
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
				DataType type = cd.frame.getVarType(op.args[0]);
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
					ArrayList<DataType> pList = new ArrayList<>();
					ArrayList<Element> es = (ArrayList<Element>) e.args[1];
					if (es.size()>0 && es.get(0).type==Rule.TUPLE) {
						es = (ArrayList<Element>) es.get(0).args[0];
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
	
	public static Expression resolveLValue(CompileData cd, Expression code, Element e, DataType setType) {
		Expression expr = new Expression();
		String name;
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case WORD:
					name = (String) e.args[0];
					if (cd.pkg.getField(name)!=null) {
						
					} else if (!cd.frame.isDefined(name)) {
						cd.errs.add(new SourceUndefinedVariableException(e.range,"Variable "+name+" not declared local", name));
					} else if (cd.frame.isConst(name) && cd.frame.getVariable(name)) {
						cd.errs.add(new SourceSafeModeException(e.range,"Cannot assign a new value to constant "+name, name));
					} else if (!cd.frame.getVariable(name)) {
						cd.frame.putVariable(name);
					}
					expr.retVar = name;
					expr.type = cd.frame.getVarType(name);
					break;
				default:
					cd.errs.add(new SourceSyntaxException(e.range,"Illegal L-value"));
			}
		} else {
			switch ((Rule)e.type) {
				case ICALL_REF:
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
					
					Expression listExpr;
					if (e.type==Rule.ICALL) {
						Element listE = new Element(e.range, TokenRule.WORD);
						listE.args[0] = e.args[0];
						listExpr = resolveLValue(cd, expr, listE, new DataType(TypeDef.UNKNOWN));
					} else {
						Expression expr2 = SourceCompiler.compileExpr(cd, cd.frame.newVarName(), (Element) e.args[0]);
						code.addAll(expr2);
						listExpr = new Expression();
						listExpr.retVar = expr2.retVar;
						listExpr.type = expr2.type;
					}

					if (listExpr.type==null) {
						listExpr.type = new DataType(true);
					}
					
					expr.retVar = cd.pkg.nameProvider.getTempName();
					
					ArrayList<Expression> arga = new ArrayList<>();
					arga.add(listExpr);
					Expression ex = new Expression();
					ex.type = setType;
					ex.retVar = expr.retVar;
					arga.add(ex);
					for (Expression expr3 : exprs) {
						arga.add(expr3);
					}
					Expression rfn = CompileLookup.rvalLookup(cd, cd.frame.newVarName(), new LookupFunction(listExpr.type.type.name+"._setindex", arga, null, e.directives));
					
					expr.addAll(rfn);
					expr.addAll(listExpr);
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
	
	public static void checkPrivacy(CompileData cd) {
		CompileUtils.transform(cd.pkg, (pkg, work, code) -> {
			String pkgOf;
			if (work instanceof Function) {
				pkgOf = ((Function)work).pkgName;
			} else if (work instanceof Field) {
				pkgOf = ((Field)work).pkgName;
			} else {
				pkgOf = cd.pkg.getName();
			}
			
			for (Operation op : code) {
				if (op.op == OpType.CALL) {
					String fnName = op.args[1];
					Function fn = pkg.getFunction(fnName);
					
					if (!fn.pkgName.equals(pkgOf) && Directives.has(fn, "private")) {
						cd.errs.add(new SourceSafeModeException(op.range, "Function "+fnName+" is @private to package "+fn.pkgName+", call was from package "+pkgOf, fnName));
					}
				}
				
				int i = 0;
				for (boolean b : op.getVarSlots()) {
					if (b) {
						String var = op.args[i];
						Field f = pkg.getField(var);
						if (f!=null && !f.pkgName.equals(pkgOf) && Directives.has(f, "private")) {
							cd.errs.add(new SourceSafeModeException(op.range, "Field "+var+" is @private to package "+f.pkgName+", use was from package "+pkgOf, var));
						}
					}
					i++;
				}
			}
			
			return code;
		});
	}
}