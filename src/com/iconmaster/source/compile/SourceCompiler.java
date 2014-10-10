package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.Variable;
import com.iconmaster.source.tokenize.CompoundTokenRule;
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
				case ASSIGN:
					//String var = resolveLValue(pkg, code, (Element) e.args[0]);
					String var = (String) ((ArrayList<Element>)e.args[0]).get(0).args[0];
					//Expression right = compileExpression(pkg, var, (Element) e.args[1]);
					Expression right = compileExpression(pkg, var, (Element) ((ArrayList<Element>)e.args[1]).get(0));
					code.addAll(right);
					//unnessesary MOV (?)
					//code.add(new Operation(OpType.MOV,var,right.retVar));
					break;
			}
		}
		return code;
	}
	
	public static Expression compileExpression(SourcePackage pkg, String retVar, Element e) {
		Expression expr = new Expression();
		if (retVar == null) {
			retVar = pkg.nameProvider.getNewName();
		}
		expr.retVar = retVar;
		
		if (e.type instanceof TokenRule) {
			switch ((TokenRule)e.type) {
				case NUMBER:
					expr.add(new Operation(OpType.MOVK,retVar,(String)e.args[0]));
					break;
			}
		} else if (e.type instanceof CompoundTokenRule) {
			switch ((CompoundTokenRule)e.type) {
				
			}
		} else if (e.type instanceof Rule) {
			switch ((Rule)e.type) {
				case ADD:
					String lvar = pkg.nameProvider.getNewName();
					Expression left = compileExpression(pkg, lvar, (Element) e.args[0]);
					String rvar = pkg.nameProvider.getNewName();
					Expression right = compileExpression(pkg, rvar, (Element) e.args[1]);
					expr.addAll(left);
					expr.addAll(right);
					expr.add(new Operation(OpType.ADD,retVar,lvar,rvar));
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
		} else if (e.type instanceof CompoundTokenRule) {
			switch ((CompoundTokenRule)e.type) {
				
			}
		} else if (e.type instanceof Rule) {
			switch ((Rule)e.type) {
				
			}
		}
		return null;
	}
}
