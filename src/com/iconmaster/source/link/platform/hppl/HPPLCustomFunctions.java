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
	
	public static void mathFunc(SourcePackage pkg, String fnName, String infix) {
		pkg.getFunction(fnName).data.put("onAssemble", (CustomFunction) (ad,op,sb) -> {
			sb.append("(");
			sb.append(ad.getInline(op.op.args[2]));
			sb.append(infix);
			sb.append(ad.getInline(op.op.args[3]));
			sb.append(")");
			return null;
		});
	}
	
	public static void loadCore(SourcePackage pkg) {
		pkg.getFunction("core.print").data.put("compName", "PRINT");
		pkg.getFunction("core.ifte").data.put("compName", "IFTE");
		
		for (TypeDef type : LibraryCore.MATH_TYPES) {
			pkg.getFunction("core."+type+"._neg").data.put("onAssemble", (CustomFunction) (ad,op,sb) -> {
				sb.append("(");
				sb.append(HPPLCharacters.NEG);
				sb.append("(");
				sb.append(ad.getInline(op.op.args[2]));
				sb.append("))");
				return null;
			});
			
			mathFunc(pkg, "core."+type+"._add", "+");
			mathFunc(pkg, "core."+type+"._sub", "-");
			mathFunc(pkg, "core."+type+"._mul", "*");
			mathFunc(pkg, "core."+type+"._div", "/");
			mathFunc(pkg, "core."+type+"._mod", " MOD ");
			mathFunc(pkg, "core."+type+"._pow", "^");
			
			mathFunc(pkg, "core."+type+"._eq", "==");
			mathFunc(pkg, "core."+type+"._neq", "<>");
			mathFunc(pkg, "core."+type+"._lt", "<");
			mathFunc(pkg, "core."+type+"._gt", ">");
			mathFunc(pkg, "core."+type+"._le", "<=");
			mathFunc(pkg, "core."+type+"._ge", ">=");
			
		}
		
		mathFunc(pkg, "core.?._concat", "+");
		
		mathFunc(pkg, "core.bool._and", " AND ");
		mathFunc(pkg, "core.bool._or", " OR ");
		pkg.getFunction("core.bool._not").data.put("onAssemble", (CustomFunction) (ad,op,sb) -> {
			sb.append("(NOT ");
			sb.append(ad.getInline(op.op.args[2]));
			sb.append(")");
			return null;
		});
		
		for (TypeDef type : LibraryCore.INT_TYPES) {
			pkg.getFunction("core."+type.name+"._bnot").data.put("compName", "BITNOT");
			pkg.getFunction("core."+type.name+"._band").data.put("compName", "BITAND");
			pkg.getFunction("core."+type.name+"._bor").data.put("compName", "BITOR");
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
