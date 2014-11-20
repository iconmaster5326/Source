package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.tokenize.TokenRule;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author iconmaster
 */
public class CompileLookup {
	public static enum LookupType {
		RAWSTR,RAWCALL,RAWINDEX,
		ROOT,VAR,PKG,TYPE,FUNC,METHOD,GLOBAL,FIELD,INDEX;
		
		public boolean isRaw() {
			switch(this) {
				case RAWSTR:
				case RAWCALL:
				case RAWINDEX:
					return true;
				default:
					return false;
			}
		}
	}
	
	public static class LookupNode<T> {
		public LookupNode p;
		public ArrayList<LookupNode> c = new ArrayList<>();
		public T data;
		public LookupType type;
		public String match;

		public LookupNode(LookupType type, LookupNode p, T data, String match, LookupNode... c) {
			this.p = p;
			this.data = data;
			this.type = type;
			this.match = match;
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
			LookupNode node = new LookupNode(raw?LookupType.RAWSTR:LookupType.PKG, this, null, name);
			c.add(node);
			return node;
		}
		
		public static LookupNode root() {
			return new LookupNode(LookupType.ROOT, null, null, null);
		}
		
		public static LookupNode addFromFullName(CompileData cd, LookupType type, LookupNode p, Object data, String match, LookupNode... c) {
			String[] subs = match.split("\\.");
			LookupNode tree = p;
			LookupNode orig = new LookupNode(type, p, data, subs[subs.length-1], c);
			for (String sub : Arrays.copyOf(subs, subs.length-1)) {
				tree = tree.getPkg(sub, type.isRaw());
			}
			tree.c.add(orig);
			return orig;
		}
		
		public LookupNode<T> cloneNode() {
			return new LookupNode<>(type, null, data, match);
		}
	}
	
	public static LookupNode getLookupTree(CompileData cd) {
		LookupNode tree = getLookupTree(cd, LookupNode.root());
		for (String v : cd.frame.getAllVars()) {
			LookupNode tree2 = LookupNode.addFromFullName(cd,LookupType.VAR, tree, v, v);
			getLookupTree(cd, tree2);
		}
		return tree;
	}
	
	public static LookupNode getLookupTree(CompileData cd, LookupNode node) {
		switch (node.type) {
			case ROOT:
				for (Function fn : cd.pkg.getFunctions()) {
					LookupNode tree = LookupNode.addFromFullName(cd,LookupType.FUNC, node, fn, fn.getName());
					getLookupTree(cd, tree);
					tree = LookupNode.addFromFullName(cd,LookupType.FUNC, node, fn, fn.pkgName+"."+fn.getName());
					getLookupTree(cd, tree);
				}
				for (Field fn : cd.pkg.getFields()) {
					LookupNode tree = LookupNode.addFromFullName(cd,LookupType.FUNC, node, fn, fn.getName());
					getLookupTree(cd, tree);
					tree = LookupNode.addFromFullName(cd,LookupType.FUNC, node, fn, fn.pkgName+"."+fn.getName());
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
							LookupNode tree = LookupNode.addFromFullName(cd,LookupType.METHOD, node, fn, methodName);
							getLookupTree(cd, tree);
						}
					}
				}
				break;
		}
		return node;
	}
	
	public static LookupNode parseArgs(CompileData cd, Object... args) {
		LookupNode tree = LookupNode.root();
		LookupNode node = tree;
		for (Object arg : args) {
			if (arg instanceof LookupNode) {
				node.c.add((LookupNode)arg);
				((LookupNode)arg).p = node;
				node = (LookupNode) arg;
			} else if (arg instanceof String) {
				node = LookupNode.addFromFullName(cd, LookupType.RAWSTR, node, arg, (String) arg);
			} else if (arg instanceof Element) {
				Element e = (Element) arg;
				if (e.type instanceof Rule) {
					switch ((Rule)e.type) {
						case FCALL:
							break;
						case ICALL:
							break;
						case ICALL_REF:
							break;
						case CHAIN:
							ArrayList<Element> es = (ArrayList<Element>) e.args[0];
							LookupNode tree2 = parseArgs(cd, es.toArray());
							node.c.add(tree2);
							tree2.p = node;
							node = tree2;
							break;
						case PAREN:
							es = (ArrayList<Element>) e.args[0];
							tree2 = parseArgs(cd, es.get(0));
							node.c.add(tree2);
							tree2.p = node;
							node = tree2;
							break;
					}
				} else {
					switch ((TokenRule)e.type) {
						case WORD:
							node = LookupNode.addFromFullName(cd, LookupType.RAWSTR, node, e.args[0], (String) e.args[0]);
							break;
					}
				}
			}
		}
		return tree;
	}
	
	public static Expression rvalLookup(CompileData cd, String retVar, Range rn, Object... args) {
		LookupNode rawtree = parseArgs(cd, args);
		LookupNode rawnode = rawtree;
		
		LookupNode lookupTree = getLookupTree(cd);
		ArrayList<LookupNode> lookupNodes = new ArrayList<>();
		lookupNodes.add(lookupTree);
		
		ArrayList<LookupNode> nodes = new ArrayList<>();
		nodes.add(LookupNode.root());
		
		Expression expr = new Expression();
		expr.retVar = retVar;
		
		while (true) {
			if (rawnode.c.isEmpty()) {
				break;
			}
			rawnode = (LookupNode) rawnode.c.get(0);
			
			ArrayList<LookupNode> newLookupNodes = new ArrayList<>();
			ArrayList<LookupNode> newNodes = new ArrayList<>();
			
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
					}
				}
				i++;
			}
			
			lookupNodes = newLookupNodes;
			nodes = newNodes;
			
			if (nodes.isEmpty()) {
				//error
				cd.errs.add(new SourceException(null, "Lookup failed"));
				return expr;
			}
		}
		
		LookupNode node = nodes.get(0);
		
		while (node.p!=null) {
			node = node.p;
		}
		
		while (true) {
			if (node.c.isEmpty()) {
				break;
			}
			node = (LookupNode) node.c.get(0);
			
			switch (node.type) {
				case VAR:
					expr.add(new Operation(OpType.MOV, retVar, (String) node.data));
					expr.type = cd.frame.getVarType((String) node.data);
					break;
			}
		}
		
		return expr;
	}
}
