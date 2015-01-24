package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.link.platform.hppl.InlinedExpression.InlineOp;
import com.iconmaster.source.link.platform.hppl.InlinedExpression.SpecialOp;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class HPPLAssembler {
	public static String assemble(SourcePackage pkg) {
		AssemblyData ad = new AssemblyData(pkg);
		StringBuilder sb = new StringBuilder("#pragma mode( separator(.,;) integer(h32) )\n//This program compiled with Source: www.github.com/iconmaster5326/Source\n\n");
		
		//get assembled data of everything
		for (Function fn : pkg.getFunctions()) {
			if (shouldAssemble(ad, fn)) {
				HPPLFunction hfn = assembleFunction(ad, fn);
				ad.funcs.add(hfn);
			}
		}
		
		//convert assembled data into a string
		for (HPPLFunction fn : ad.funcs) {
			fn.toString(ad);
		}
		for (HPPLField f : ad.fields) {
			f.toString(ad);
		}
		
		//add prototypes
		for (HPPLFunction fn : ad.funcs) {
			sb.append(fn.compileName);
			sb.append("(");
			if (!fn.args.isEmpty()) {
				for (HPPLVariable var : fn.args) {
					sb.append(var.compileName);
					sb.append(",");
				}
				sb.deleteCharAt(sb.length()-1);
			}
			sb.append(")");
			sb.append(";");
		}
		if (!ad.minify) {
			sb.append("\n");
		}
		
		for (HPPLField f : ad.fields) {
			sb.append(f.compileName);
			sb.append(";");
		}
		if (!ad.minify) {
			sb.append("\n");
		}
		
		for (HPPLVariable v : ad.vars) {
			sb.append(v.compileName);
			sb.append(";");
		}
		if (!ad.minify) {
			sb.append("\n");
		}
		
		//add the content
		for (HPPLFunction fn : ad.funcs) {
			if (fn.output!=null) {
				sb.append(fn.output);
			}
			if (!ad.minify) {
				sb.append("\n");
			}
		}	
		
		return sb.toString();
	}
	
	public static HPPLFunction assembleFunction(AssemblyData ad, Function fn) {
		ArrayList<HPPLVariable> args = new ArrayList<>();
		for (Field arg : fn.getArguments()) {
			HPPLVariable var = new HPPLVariable(arg.getName(), HPPLNaming.getNewName());
			//ad.vars.add(var);
			args.add(var);
		}
		return new HPPLFunction(HPPLNaming.getNewName(), args, assembleCode(ad, fn.getCode()), fn);
	}
	
	public static InlinedExpression encapsulate(AssemblyData ad, ArrayList<InlinedExpression> expr) {
		if (expr.size()==1) {
			return expr.get(0);
		}
		
		HPPLFunction fn = new HPPLFunction(HPPLNaming.getNewName(), new ArrayList<>(), expr, null);
		InlinedExpression expr2 = new InlinedExpression();
		expr2.add(new InlineOp(new Operation(Operation.OpType.CALL, fn.compileName), SpecialOp.CALL_IFN));
		ad.funcs.add(fn);
		return expr2;
	}
	
	public static ArrayList<InlinedExpression> assembleCode(AssemblyData ad, ArrayList<Operation> code) {
		InlinedExpression expr = HPPLInliner.inlineCode(ad, code);
		return HPPLInliner.getStatements(expr);
	}
	
	public static String getString(AssemblyData ad, InlinedExpression expr) {
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}
	
	public static String getString(AssemblyData ad, ArrayList<InlinedExpression> exprs) {
		StringBuilder sb = new StringBuilder();
		for (InlinedExpression expr : exprs) {
			sb.append(getString(ad, expr));
			sb.append(";");
			if (!ad.minify) {
				sb.append("\n");
			}
		}
		if (!exprs.isEmpty()) {
			if (ad.minify) {
				sb.deleteCharAt(sb.length()-1);
			}
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	
	public static boolean shouldAssemble(AssemblyData ad, Function fn) {
		return !fn.isLibrary() && fn.isCompiled();
	}
}
