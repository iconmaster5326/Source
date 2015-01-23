package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.exception.SourceUndefinedVariableException;
import com.iconmaster.source.exception.SourceUninitializedVariableException;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.FunctionCall;
import com.iconmaster.source.prototype.TypeDef;
import com.iconmaster.source.tokenize.TokenRule;
import com.iconmaster.source.util.Range;
import com.iconmaster.source.util.SourceDecompiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class CompileLookup {
	public static enum LookupType {
		RAWSTR,RAWCALL,RAWINDEX,
		ROOT,VAR,PKG,TYPE,FUNC,METHOD,FIELD,GLOBAL,INDEX,EXPR;
		
		public boolean isRaw() {
			switch(this) {
				case RAWSTR:
				case RAWCALL:
				case RAWINDEX:
				case EXPR:
					return true;
				default:
					return false;
			}
		}

		@Override
		public String toString() {
			switch (this) {
				case VAR:
					return "variable";
				case ROOT:
					return "package";
				case PKG:
					return "package";
				case TYPE:
					return "type";
				case FUNC:
					return "function";
				case METHOD:
					return "method";
				case FIELD:
					return "field";
				case GLOBAL:
					return "field";
				case INDEX:
					return "index";
				case RAWSTR:
					return "member";
				case RAWCALL:
					return "function";
				case RAWINDEX:
					return "index";
				case EXPR:
					return "expression";
			}
			return "member";
		}
	}
	
	public static class LookupNode<T> {
		public LookupNode p;
		public ArrayList<LookupNode> c = new ArrayList<>();
		public T data;
		public LookupType type;
		public String match;
		public Range range;
		public DataType dataType;

		public LookupNode(LookupType type, LookupNode p, T data, Range range, String match, LookupNode... c) {
			this.p = p;
			this.data = data;
			this.type = type;
			this.match = match;
			this.range = range;
			this.c.addAll(Arrays.asList(c));
		}
		
		public ArrayList<LookupNode> getNodes(String name) {
			ArrayList<LookupNode> a = new ArrayList<>();
			for (LookupNode node : c) {
				if (name.equals(node.match)) {
					a.add(node);
				}
			}
			return a;
		}
		
		public LookupNode getPkg(String name, boolean raw) {
			for (LookupNode node : getNodes(name)) {
				if (raw) {
					if (node.type==LookupType.RAWSTR) {
						return node;
					}
				} else {
					if (node.type==LookupType.PKG) {
						return node;
					}
				}
			}
			LookupNode node = new LookupNode(raw?LookupType.RAWSTR:LookupType.PKG, this, range, null, name);
			c.add(node);
			return node;
		}
		
		public static LookupNode root(Range range) {
			return new LookupNode(LookupType.ROOT, null, "<default>", range, "<default>");
		}
		
		public static LookupNode addFromFullName(CompileData cd, LookupType type, LookupNode p, Object data, String match, Range range, boolean first, LookupNode... c) {
			String[] subs = match.split("\\.");
			LookupNode tree = p;
			LookupNode orig = new LookupNode(type, p, data, range, subs[subs.length-1], c);
			for (String sub : Arrays.copyOf(subs, subs.length-1)) {
				tree = tree.getPkg(sub, type.isRaw());
			}
			if (first) {
				tree.c.add(0,orig);
			} else {
				tree.c.add(orig);
			}
			return orig;
		}
		
		public LookupNode<T> cloneNode() {
			return new LookupNode<>(type, null, data, range, match);
		}
	}
	
	public static class LookupFunction {
		public String name;
		public ArrayList<Expression> args;
		public ArrayList<String> dirs;
		public DataType retType;
		
		public boolean index = false;
		public Function fn;

		public LookupFunction(String name, ArrayList<Expression> args, DataType retType, ArrayList<String> dirs) {
			this.name = name;
			this.args = args;
			this.dirs = dirs;
			this.retType = retType;
		}
		
		public LookupFunction(CompileData cd, Range rn, String name, DataType retType, ArrayList<String> dirs, String... args) {
			this.name = name;
			this.dirs = dirs;
			this.retType = retType;
			
			this.args = new ArrayList<>();
			for (String s : args) {
				Expression expr = rvalLookup(cd, cd.frame.newVarName(), rn, s);
				this.args.add(expr);
			}
		}
		
		public FunctionCall toFuncCall() {
			ArrayList<DataType> a = new ArrayList<>();
			for (Expression expr : args) {
				a.add(expr.type);
			}
			return new FunctionCall(name, a, retType, dirs);
		}
		
		public LookupFunction cloneFunc() {
			LookupFunction fcall = new LookupFunction(name, args, retType, dirs);
			fcall.index = index;
			fcall.fn = fn;
			return fcall;
		}
	}
	
	public static Expression mathOp(CompileData cd, String name, String retVar, Object ret, String[] sargs, Object[] args) {
		ArrayList<Expression> a = new ArrayList<>();
		int sarg = 0;
		for (Object arg : args) {
			Expression e = new Expression();
			e.retVar = sargs[sarg];
			if (arg instanceof DataType) {
				e.type = (DataType) arg;
			} else if (arg instanceof TypeDef) {
				e.type = new DataType((TypeDef) arg);
			}
			a.add(e);
			sarg++;
		}
		
		DataType rt = null;
		if (ret instanceof DataType) {
			rt = (DataType) ret;
		} else if (ret instanceof TypeDef) {
			rt = new DataType((TypeDef) ret);
		}
		
		return rvalLookup(cd, retVar, new LookupFunction(name, a, rt, new ArrayList<>()));
	}
	
	public static LookupNode getLookupTree(CompileData cd) {
		LookupNode tree = getLookupTree(cd, LookupNode.root(null));
		for (String v : cd.frame.getAllVars()) {
			LookupNode tree2 = LookupNode.addFromFullName(cd,LookupType.VAR, tree, v, v, null, true);
			getLookupTree(cd, tree2);
		} 
		return tree;
	}
	
	public static LookupNode getLookupTree(CompileData cd, LookupNode node) {
		switch (node.type) {
			case ROOT:
				for (Function fn : cd.pkg.getFunctions()) {
					LookupNode tree = LookupNode.addFromFullName(cd,LookupType.FUNC, node, fn, fn.getName(), null, false);
					getLookupTree(cd, tree);
					tree = LookupNode.addFromFullName(cd,LookupType.FUNC, node, fn, fn.pkgName+"."+fn.getName(), null, false);
					getLookupTree(cd, tree);
				}
				for (Field fn : cd.pkg.getFields()) {
					LookupNode tree = LookupNode.addFromFullName(cd,LookupType.GLOBAL, node, fn, fn.getName(), null, false);
					getLookupTree(cd, tree);
					tree = LookupNode.addFromFullName(cd,LookupType.GLOBAL, node, fn, fn.pkgName+"."+fn.getName(), null, false);
					getLookupTree(cd, tree);
				}
				for (TypeDef td : cd.pkg.getTypes()) {
					LookupNode tree = LookupNode.addFromFullName(cd,LookupType.TYPE, node, td, td.name, null, false);
					getLookupTree(cd, tree);
				}
				break;
			case VAR:
				String varName = (String) node.data;
				DataType type = cd.frame.getVarType(varName);
				if (type!=null) {
					for (Function fn : cd.pkg.getFunctions()) {
						if (fn.getName().startsWith(type.type.name+".")) {
							String methodName = fn.getName();
							methodName = methodName.substring(methodName.indexOf(".")+1);
							LookupNode tree = LookupNode.addFromFullName(cd,LookupType.METHOD, node, fn, methodName, null, false);
							getLookupTree(cd, tree);
						}
					}
				}
				break;
			case TYPE:
				for (Function fn : cd.pkg.getFunctions()) {
					TypeDef td = (TypeDef) node.data;
					while (td!=null) {
						if (fn.getName().startsWith(td.name+".")) {
							String methodName = fn.getName();
							methodName = methodName.substring(td.name.length()+1);
							LookupNode tree = LookupNode.addFromFullName(cd,LookupType.METHOD, node, fn, methodName, null, false);
							getLookupTree(cd, tree);
						}
						td = td.parent;
					}
				}
				break;
		}
		return node;
	}
	
	public static LookupNode parseArgs(CompileData cd, Object... args) {
		LookupNode tree = LookupNode.root(null);
		LookupNode node = tree;
		for (Object arg : args) {
			if (arg instanceof LookupNode) {
				node.c.add((LookupNode)arg);
				((LookupNode)arg).p = node;
				node = (LookupNode) arg;
			} else if (arg instanceof String) {
				node = LookupNode.addFromFullName(cd, LookupType.RAWSTR, node, arg, (String) arg, null, false);
			} else if (arg instanceof LookupFunction) {
				node = LookupNode.addFromFullName(cd, LookupType.RAWCALL, node, arg, ((LookupFunction) arg).name, null, false);
			} else if (arg instanceof Element) {
				Element e = (Element) arg;
				if (e.type instanceof Rule) {
					switch ((Rule)e.type) {
						case FCALL:
							LookupFunction fcall = new LookupFunction(null, new ArrayList<>(), (DataType) null, new ArrayList<>());
							fcall.name = (String) e.args[0];
							fcall.name = fcall.name.substring(fcall.name.lastIndexOf(".")+1);
							ArrayList<Element> es = (ArrayList<Element>) e.args[1];
							if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
								es = (ArrayList<Element>) es.get(0).args[0];
							}
							for (Element e2 : es) {
								Expression expr2 = SourceCompiler.compileExpr(cd, cd.frame.newVarName(), e2);
								fcall.args.add(expr2);
							}
							if (e.dataType!=null) {
								DataType dt = SourceCompiler.compileDataType(cd, e.dataType);
								fcall.retType = dt;
							}
							fcall.dirs.addAll(e.directives);
							node = LookupNode.addFromFullName(cd, LookupType.RAWCALL, node, fcall, (String) e.args[0],e.range, false);
							break;
						case CHAIN:
							es = (ArrayList<Element>) e.args[0];
							LookupNode tree2 = parseArgs(cd, es.toArray());
							tree2 = (LookupNode) tree2.c.get(0);
							node.c.add(tree2);
							tree2.p = node;
							node = tree2;
							break;
						case PAREN:
							es = (ArrayList<Element>) e.args[0];
							tree2 = parseArgs(cd, es.get(0));
							tree2 = (LookupNode) tree2.c.get(0);
							node.c.add(tree2);
							tree2.p = node;
							node = tree2;
							break;
						case ICALL:
							fcall = new LookupFunction(null, new ArrayList<>(), (DataType) null, new ArrayList<>());
							node = LookupNode.addFromFullName(cd, LookupType.RAWSTR, node, e.args[0], (String) e.args[0],e.range, false);
							
							fcall.name = "_getindex";
							es = (ArrayList<Element>) e.args[1];
							if (es.size()==1 && es.get(0).type==Rule.TUPLE) {
								es = (ArrayList<Element>) es.get(0).args[0];
							}
							for (Element e2 : es) {
								Expression expr2 = SourceCompiler.compileExpr(cd, cd.frame.newVarName(), e2);
								fcall.args.add(expr2);
							}
							if (e.dataType!=null) {
								DataType dt = SourceCompiler.compileDataType(cd, e.dataType);
								fcall.retType = dt;
							}
							fcall.dirs.addAll(e.directives);
							node = LookupNode.addFromFullName(cd, LookupType.RAWCALL, node, fcall, fcall.name,e.range, false);
							break;
						default:
							Expression expr2 = SourceCompiler.compileExpr(cd, cd.frame.newVarName(), e);
							node = new LookupNode(LookupType.EXPR, node, expr2, e.range, SourceDecompiler.elementToString(e));
							node.p.c.add(node);
							break;
					}
				} else {
					switch ((TokenRule)e.type) {
						case WORD:
							node = LookupNode.addFromFullName(cd, LookupType.RAWSTR, node, e.args[0], (String) e.args[0], e.range, false);
							break;
						default:
							Expression expr2 = SourceCompiler.compileExpr(cd, cd.frame.newVarName(), e);
							node = new LookupNode(LookupType.EXPR, node, expr2, e.range, SourceDecompiler.elementToString(e));
							node.p.c.add(node);
							break;
					}
				}
			}
		}
		return tree;
	}
	
	public static Expression toExpr(CompileData cd, String retVar, LookupNode node) {
		Expression expr = new Expression();
		expr.retVar = retVar;
		
		if (node==null) {
			return expr;
		}
		
		while (node.p!=null) {
			node = node.p;
		}
		
		String prev;
		String var = null;
		while (true) {
			if (node.c.isEmpty()) {
				break;
			}
			node = (LookupNode) node.c.get(0);
			
			if (node.type!=LookupType.PKG) {
				prev = var;
				if (node.c.isEmpty()) {
					var = retVar;
				} else {
					var = cd.frame.newVarName();
				}

				switch (node.type) {
					case VAR:
						if (cd.frame.isInlined((String)node.data)) {
							Element e2 = cd.frame.getInline((String)node.data);
							if (e2==null) {
								cd.errs.add(new SourceUninitializedVariableException(node.range,"Constant "+node.data+" not initialized", (String) node.data));
							} else {
								Expression expr2 = SourceCompiler.compileExpr(cd, retVar, e2);
								expr.addAll(expr2);
								expr.type = expr2.type;
							}
						} else if (!cd.frame.isDefined((String)node.data)) {
							cd.errs.add(new SourceUndefinedVariableException(node.range, "Undefined variable "+node.data, (String) node.data));
						} else if (cd.frame.getVariable((String)node.data)==null) {
							cd.errs.add(new SourceUninitializedVariableException(node.range, "Uninitialized variable "+node.data, (String) node.data));
						} else {
							expr.type = cd.frame.getVarType((String)node.data);
							expr.add(new Operation(OpType.MOV, expr.type, node.range, var, (String)node.data));
						}
						break;
					case GLOBAL:
						expr.type = ((Field)node.data).getType();
						expr.add(new Operation(OpType.MOV, expr.type, node.range, var, ((Field)node.data).getName()));
						break;
					case METHOD:
						Expression expr2 = ((LookupFunction)node.data).args.get(0);
						expr2.retVar = prev;
					case FUNC:
						LookupFunction fcall = (LookupFunction) node.data;
						ArrayList<String> names = new ArrayList<>();
						for (Expression ex : fcall.args) {
							expr.addAll(ex);
							names.add(ex.retVar);
						}
						names.add(0,fcall.fn.getFullName());
						names.add(0,var);
						expr.type = ((LookupFunction)node.data).retType;
						expr.add(new Operation(OpType.CALL, ((LookupFunction)node.data).retType, node.range, names.toArray(new String[0])));
						break;
					case EXPR:
						expr.addAll(((Expression)node.data));
						expr.add(new Operation(OpType.MOV, ((Expression)node.data).type, node.range, var, ((Expression)node.data).retVar));
						expr.type = ((Expression)node.data).type;
						break;
				}
			}
		}
		
		return expr;
	}
	
	public static Expression rvalLookup(CompileData cd, String retVar, Object... args) {
		return toExpr(cd, retVar, lookup(cd, args));
	}
	
	public static LookupNode getType(LookupNode tree, String name) {
		for (LookupNode node : (ArrayList<LookupNode>) tree.c) {
			if (node.type==LookupType.TYPE && name.equals(node.match)) {
				return node;
			} else {
				LookupNode res = getType(node,name);
				if (res!=null) {
					return res;
				}
			}
		}
		return null;
	}
	
	public static LookupNode lookup(CompileData cd, Object... args) {
		LookupNode rawtree = parseArgs(cd, args);
		LookupNode rawnode = rawtree;
		
		LookupNode lookupTree = getLookupTree(cd);
		ArrayList<LookupNode> lookupNodes = new ArrayList<>();
		lookupNodes.add(lookupTree);
		
		ArrayList<LookupNode> nodes = new ArrayList<>();
		nodes.add(LookupNode.root(null));
		
		while (true) {
			if (rawnode.c.isEmpty()) {
				break;
			}
			rawnode = (LookupNode) rawnode.c.get(0);
			
			ArrayList<LookupNode> newLookupNodes = new ArrayList<>();
			ArrayList<LookupNode> newNodes = new ArrayList<>();
			
			if (rawnode.type==LookupType.EXPR) {
				int i = 0;
				for (LookupNode node : lookupNodes) {
					if (node.type==LookupType.ROOT) {
						LookupNode child = nodes.get(i);
						node = getType(lookupTree, ((Expression)rawnode.data).type.type.name);
						newLookupNodes.add(node);

						LookupNode newNode = rawnode.cloneNode();
						child.c.add(newNode);
						newNode.p = child;
						newNodes.add(newNode);
					}
					i++;
				}
			} else {
				int i = 0;
				for (LookupNode lookupNode : lookupNodes) {
					LookupNode node = nodes.get(i);
					for (LookupNode child : (ArrayList<LookupNode>) lookupNode.c) {
						switch (rawnode.type) {
							case RAWSTR:
								if (rawnode.match == null ? false : rawnode.match.equals(child.match)) {
									LookupNode newNode = child.cloneNode();
									node.c.add(newNode);
									newNode.p = node;
									newNodes.add(newNode);
									newLookupNodes.add(child);
								}
								break;
							case RAWCALL:
								if (rawnode.match.equals(child.match) && (child.type==LookupType.FUNC || child.type==LookupType.METHOD)) {
									LookupFunction fcall = (LookupFunction) rawnode.data;
									if (lookupNode.type==LookupType.TYPE) {
										fcall.args.add(0,new Expression());
										fcall.args.get(0).type = node.dataType;
									}
									FunctionCall fcall2 = fcall.toFuncCall();

									if (cd.pkg.isFunctionCallCompatible((Function) child.data, fcall2)) {
										LookupNode newNode = child.cloneNode();
										node.c.add(newNode);
										newNode.p = node;
										fcall.fn = (Function) child.data;
										newNode.data = fcall;
										newNodes.add(newNode);
										newLookupNodes.add(child);
									}
								}
								break;
						}
					}
					i++;
				}
			}
			
			ArrayList<LookupNode> oldLookupNodes = lookupNodes;
			ArrayList<LookupNode> oldNodes = nodes;
			
			lookupNodes = newLookupNodes;
			nodes = newNodes;
			
			if (nodes.isEmpty()) {
				int j = 0;
				//cd.errs.add(new SourceException(rawnode.range, "Lookup failed for "+rawnode.match+":"));
				for (LookupNode child : oldLookupNodes) {
					LookupNode node = oldNodes.get(j);
					cd.errs.add(new SourceException(rawnode.range, "Could not find "+rawnode.type+" "+rawnode.match+" of "+child.type+" "+child.match));
					j++;
				}
				return null;
			}
			
			newLookupNodes = new ArrayList<>();
			int j = 0;
			for (LookupNode child : lookupNodes) {
				LookupNode node = nodes.get(j);
				DataType varType = null;
				
				switch (child.type) {
					case VAR:
						varType = cd.frame.getVarType((String) child.data);
						break;
					case GLOBAL:
						varType = ((Field)child.data).getType();
						break;
					case FUNC:
					case METHOD:
						Function fcall = (Function) child.data;
						varType = fcall.getReturnType();
						
						LookupFunction fcall2 = (LookupFunction) node.data;
						
						ArrayList<DataType> ct = new ArrayList<>();
						ArrayList<DataType> gt = new ArrayList<>();
						for (Field f : fcall.getArguments()) {
							ct.add(f.getType()==null?new DataType():f.getType());
						}
						for (Expression expr : fcall2.args) {
							gt.add(expr.type);
						}
						HashMap<String,DataType> map = Parameterizer.parameterize(cd, rawnode.range, ct, gt, new HashMap<>());
						int k = 0;
						for (Expression expr : fcall2.args) {
							expr.type = Parameterizer.replaceWithParams(expr.type, map);
							k++;
						}

						varType = Parameterizer.replaceWithParams(varType, map);
						
						fcall2.retType = varType;
						break;
				}
				
				if (varType != null) {
					child = getType(lookupTree, varType.type.name);
				}
				node.dataType = varType;
				newLookupNodes.add(child);
				j++;
			}
			lookupNodes = newLookupNodes;
		}
		
		return nodes.get(0);
	}
}
