package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;

/**
 *
 * @author iconmaster
 */
public class LibraryPrimeDraw extends SourcePackage {
	public static TypeDef GROB_TYPE = new TypeDef("grob");
	
	public LibraryPrimeDraw() {
		this.name = "prime.draw";
		
		this.addType(GROB_TYPE);
		
		Function fn = Function.libraryFunction("grob.width", new String[] {"g"}, new TypeDef[] {GROB_TYPE}, TypeDef.REAL);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("GROBW_P(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			return ")";
		};
		this.addFunction(fn);
		
		Field f = Field.libraryField("screen", GROB_TYPE);
		f.onCompile = (pkg,isGet,args)->{
			return "G0";
		};
		this.addField(f);
	}
}
