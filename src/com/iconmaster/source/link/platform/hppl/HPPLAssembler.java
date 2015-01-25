package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.assemble.AssembledOutput;
import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.link.platform.hppl.HPPLCustomFunctions.CustomFunction;
import com.iconmaster.source.link.platform.hppl.HPPLCustomFunctions.CustomIterator;
import com.iconmaster.source.link.platform.hppl.InlinedExpression.InlineOp;
import com.iconmaster.source.link.platform.hppl.InlinedExpression.SpecialOp;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.Iterator;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.IDirectable;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class HPPLAssembler {
	public static AssembledOutput assemble(SourcePackage pkg) {
		AssemblyData ad = new AssemblyData(pkg);
		StringBuilder sb = new StringBuilder("#pragma mode( separator(.,;) integer(h32) )\n//This program compiled with Source version @VERSION@, found at www.github.com/iconmaster5326/Source.\n\n");
		
		//get assembled data of everything
		for (Function fn : pkg.getFunctions()) {
			if (shouldAssemble(ad, fn)) {
				HPPLFunction hfn = assembleFunction(ad, fn);
				ad.funcs.add(hfn);
			}
		}
		for (Field f : pkg.getFields()) {
			if (shouldAssemble(ad, f)) {
				HPPLField hf = assembleField(ad, f);
				ad.fields.add(hf);
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
			sb.append(f.output);
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
		
		return new HPPLOutput(sb.toString());
	}
	
	public static HPPLFunction assembleFunction(AssemblyData ad, Function fn) {
		ArrayList<HPPLVariable> args = new ArrayList<>();
		for (Field arg : fn.getArguments()) {
			HPPLVariable var = new HPPLVariable(arg.getName(), getRenamed(arg,arg.getName()));
			args.add(var);
		}
		return new HPPLFunction(PlatformHPPL.shouldKeepName(fn) ? HPPLNaming.formatFuncName(fn) : HPPLNaming.getNewName(), args, assembleCode(ad, fn.getCode()), fn);
	}
	
	public static HPPLField assembleField(AssemblyData ad, Field f) {
		return new HPPLField(getRenamed(f,f.getName()), assembleCode(ad, f.getValue()), f);
	}
	
	public static String getRenamed(IDirectable f,String name) {
		return PlatformHPPL.shouldKeepName(f) ? HPPLNaming.formatVarName(name) : HPPLNaming.getNewName();
	}
	
	public static InlinedExpression encapsulate(AssemblyData ad, InlinedExpression code) {
		ArrayList<InlinedExpression> expr = HPPLInliner.getStatements(code);
		
		if (expr.size()==1) {
			return code;
		}
		
		HPPLFunction fn = new HPPLFunction(HPPLNaming.getNewName(), new ArrayList<>(), code, null);
		InlinedExpression expr2 = new InlinedExpression();
		expr2.add(new InlineOp(new Operation(Operation.OpType.CALL, fn.compileName), SpecialOp.CALL_IFN));
		ad.funcs.add(fn);
		return expr2;
	}
	
	public static InlinedExpression assembleCode(AssemblyData ad, ArrayList<Operation> code) {
		if (code==null) {
			return null;
		}
		InlinedExpression expr = HPPLInliner.inlineCode(ad, code);
		return expr;
	}
	
	public static String getString(AssemblyData ad, InlineOp op) {
		StringBuilder sb = new StringBuilder();
		switch (op.op.op) {
			case MOVN:
				sb.append(op.op.args[1]);
				break;
			case MOVS:
				sb.append("\"");
				sb.append(op.op.args[1]);
				sb.append("\"");
				break;
			case MOV:
				sb.append(ad.getInline(op.op.args[1]));
				break;
			case MOVA:
			case MOVL:
				sb.append("{");
				if (op.op.args.length!=1) {
					for (int i=1;i<op.op.args.length;i++) {
						sb.append(ad.getInline(op.op.args[i]));
						sb.append(",");
					}
					sb.deleteCharAt(sb.length()-1);
				}
				sb.append("}");
				break;
			case TRUE:
				sb.append("1");
				break;
			case FALSE:
				sb.append("0");
				break;
			case CALL:
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
				break;
			}
		return sb.toString();
	}
	
	public static String getString(AssemblyData ad, InlinedExpression expr) {
		StringBuilder lines = new StringBuilder();
		
		StringBuilder sb;
		for (int opn=0;opn<expr.size();opn++) {
			InlineOp op = expr.get(opn);
			sb = new StringBuilder();
			boolean endLine = true;
			
			if (op.spec!=null) {
				switch (op.spec) {
					case CALL_IFN:
						break;
				}
			} else {
				if (op.op.op.hasLVar()) {
					if (op.status == InlinedExpression.Status.KEEP && !ad.exists(op.op.args[0])) {
						ad.vars.add(new HPPLVariable(op.op.args[0], getRenamed(ad,op.op.args[0])));
					}
					sb.append(getString(ad, op));
					switch (op.status) {
						case KEEP:
							sb.append(HPPLCharacters.STO);
							sb.append(ad.getVarMap(op.op.args[0]));
							break;
						case INLINE:
						case KEEP_NO_LVAL:
							break;
					}
				} else {
					switch (op.op.op) {
						case RET:
							sb.append("RETURN ");
							sb.append(ad.getInline(op.op.args[0]));
							break;
						case IF:
							sb.append("IF ");
							sb.append(ad.getInline(op.op.args[0]));
							sb.append(" THEN\n");
							endLine = false;
							ad.pushFrame();
							break;
						case ELSE:
							ad.popFrame();
							ad.pushFrame();
							sb.append("ELSE\n");
							endLine = false;
							break;
						case WHILE:
							sb.append("WHILE ");
							sb.append(ad.getInline(op.op.args[0]));
							sb.append(" DO\n");
							endLine = false;
							ad.pushFrame();
							break;
						case REP:
							sb.append("REPEAT\n");
							endLine = false;
							ad.pushFrame();
							ad.frame().blockEnd = "UNTIL "+ad.getInline(op.op.args[0]);
							break;
						case BRK:
							sb.append("BREAK");
							break;
						case CONT:
							sb.append("CONTINUE");
							break;
						case ENDB:
							if (ad.frame().blockEnd==null) {
								sb.append("END");
							} else {
								sb.append(ad.frame().blockEnd);
							}
							ad.popFrame();
							break;
						case ITER:
							InlineOp forOp = expr.get(opn+1);
							int endPos = forOp.matchingBlock;
							InlinedExpression expr2 = new InlinedExpression();
							expr2.addAll(expr.subList(opn+2, endPos));
							Iterator iter = ad.pkg.getIterator(op.op.args[0]);
							if (iter.data.containsKey("onAssemble")) {
								((CustomIterator)iter.data.get("onAssemble")).assemble(ad, op, forOp, expr2, sb);
							} else {
								
							}
							opn = endPos + 1;
							break;
						case NATIVE:
							if (op.op.args[0].equals("hppl")) {
								sb.append(op.op.args[1]);
								endLine = false;
							} else if (op.op.args[0].equals("comment")) {
								sb.append(" //");
								sb.append(op.op.args[1]);
								sb.append("\n");
								endLine = false;
							} else {
								sb.append(" //ERROR: statement in unknown language ");
								sb.append(op.op.args[0]);
								sb.append(" located here\n");
								endLine = false;
							}
							break;
						default:
							endLine = false;
							break;
					}
				}
			}
			
			switch (op.status) {
				case INLINE:
					ad.addInline(op.op.args[0], sb.toString());
					break;
				case KEEP:
				case KEEP_NO_LVAL:
				case NOT_APPLICABLE:
					lines.append(sb);
					if (endLine) {
						lines.append(";");
						if (!ad.minify) {
							lines.append("\n");
						}
					}
					break;
			}
		}
		
//		lines.deleteCharAt(lines.length()-1);
//		if (!ad.minify) {
//			lines.deleteCharAt(lines.length()-1);
//		}
		return lines.toString();
	}
	
	public static boolean shouldAssemble(AssemblyData ad, Function fn) {
		return !fn.isLibrary() && fn.isCompiled();
	}
	
	public static boolean shouldAssemble(AssemblyData ad, Field f) {
		return !f.isLibrary();
	}
}
