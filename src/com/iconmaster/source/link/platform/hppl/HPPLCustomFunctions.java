package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.link.platform.hppl.InlinedExpression.InlineOp;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;

/**
 *
 * @author iconmaster
 */
public class HPPLCustomFunctions {
	public static interface CustomFunction {
		public Object assemble(AssemblyData ad, InlineOp op, StringBuilder sb);
	}
	
	public static interface CustomIterator {
		public Object assemble(AssemblyData ad, InlineOp iterOp, InlineOp forOp, InlinedExpression block, StringBuilder sb);
	}
	
	public static void loadCore(SourcePackage pkg) {
		pkg.getFunction("core.print").data.put("compName", "PRINT");
		pkg.getFunction("core.ifte").data.put("compName", "IFTE");
		
		for (TypeDef type : LibraryCore.MATH_TYPES) {
			pkg.getFunction("core."+type+"._add").data.put("onAssemble", (CustomFunction) (ad,op,sb) -> {
				sb.append("(");
				sb.append(ad.getInline(op.op.args[2]));
				sb.append("+");
				sb.append(ad.getInline(op.op.args[3]));
				sb.append(")");
				return null;
			});
		}
		
		for (TypeDef type : LibraryCore.MATH_TYPES) {
			pkg.getFunction("core."+type+"._neg").data.put("onAssemble", (CustomFunction) (ad,op,sb) -> {
				sb.append("(");
				sb.append(HPPLCharacters.NEG);
				sb.append("(");
				sb.append(ad.getInline(op.op.args[2]));
				sb.append("))");
				return null;
			});
		}
		
		for (Function fn : pkg.getFunctions("core.range")) {
			if (fn.getArguments().size()==2) {
				fn.data.put("onAssemble", (CustomFunction) (ad,op,sb) -> {
					String localName = HPPLNaming.getNewName();
					sb.append("MAKELIST(");
					sb.append(localName);
					sb.append(",");
					sb.append(localName);
					sb.append(",");
					sb.append(ad.getInline(op.op.args[2]));
					sb.append(",");
					sb.append(ad.getInline(op.op.args[3]));
					sb.append(")");
					return null;
				});
			} else {
				fn.data.put("onAssemble", (CustomFunction) (ad,op,sb) -> {
					String localName = HPPLNaming.getNewName();
					sb.append("MAKELIST(");
					sb.append(localName);
					sb.append(",");
					sb.append(localName);
					sb.append(",");
					sb.append(ad.getInline(op.op.args[2]));
					sb.append(",");
					sb.append(ad.getInline(op.op.args[3]));
					sb.append(",");
					sb.append(ad.getInline(op.op.args[4]));
					sb.append(")");
					return null;
				});
			}
		}
		
		for (Function iter : pkg.getIterators("core.range")) {
			if (iter.getArguments().size()==2) {
				iter.data.put("onAssemble", (CustomIterator) (ad,op1,op2,block,sb) -> {
					ad.vars.add(new HPPLVariable(op2.op.args[0], HPPLAssembler.getRenamed(ad, op2.op.args[0])));

					sb.append("FOR ");
					sb.append(ad.getVarMap(op2.op.args[0]));
					sb.append(" FROM ");
					sb.append(ad.getInline(op1.op.args[1]));
					sb.append(" TO ");
					sb.append(ad.getInline(op1.op.args[2]));
					sb.append(" DO\n");
					sb.append(HPPLAssembler.getString(ad, block));
					sb.append("END");
					return null;
				});
			} else {
				iter.data.put("onAssemble", (CustomIterator) (ad,op1,op2,block,sb) -> {
					ad.vars.add(new HPPLVariable(op2.op.args[0], HPPLAssembler.getRenamed(ad, op2.op.args[0])));

					sb.append("FOR ");
					sb.append(ad.getVarMap(op2.op.args[0]));
					sb.append(" FROM ");
					sb.append(ad.getInline(op1.op.args[1]));
					sb.append(" TO ");
					sb.append(ad.getInline(op1.op.args[2]));
					sb.append(" STEP ");
					sb.append(ad.getInline(op1.op.args[3]));
					sb.append(" DO\n");
					sb.append(HPPLAssembler.getString(ad, block));
					sb.append("END");
					return null;
				});
			}
		}
	}
}
