package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.link.platform.hppl.HPPLCustomFunctions.CustomFunction;
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
	
	public static void addSto(AssemblyData ad, InlineOp op, StringBuilder sb) {
		switch (op.status) {
			case KEEP:
				sb.append(HPPLCharacters.STO);
				sb.append(ad.getVarMap(op.op.args[0]));
				break;
			case INLINE:
			case KEEP_NO_LVAL:
				break;
		}
	}
	
	public static String getString(AssemblyData ad, InlinedExpression expr) {
		StringBuilder sb = new StringBuilder();
		for (InlineOp op : expr) {
			sb = new StringBuilder();
			
			if (op.spec!=null) {
				switch (op.spec) {
					case CALL_IFN:
						break;
				}
			} else {
				switch (op.op.op) {
					case MOVN:
						ad.addLVar(ad, op);
						sb.append(ad.getInline(op.op.args[1]));
						addSto(ad, op, sb);
						break;
					case CALL:
						ad.addLVar(ad, op);
						CustomFunction cf = ad.getFuncAssembler(op.op.args[1]);
						if (cf!=null) {
							cf.assemble(ad, op, sb);
						} else {
							sb.append(ad.getFuncMap(op.op.args[1]));
							if (op.op.args.length>2) {
								sb.append("(");
								for (int i=2;i<op.op.args.length;i++) {
									sb.append(ad.getInline(op.op.args[i]));
									sb.append(",");
								}
								sb.deleteCharAt(sb.length()-1);
								sb.append(")");
							}
						}
						addSto(ad, op, sb);
						break;
				}
			}
			
			switch (op.status) {
				case INLINE:
					ad.addInline(op.op.args[0], sb.toString());
					break;
				case KEEP:
				case KEEP_NO_LVAL:
			}
		}
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
