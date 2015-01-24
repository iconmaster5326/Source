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
	}
}
