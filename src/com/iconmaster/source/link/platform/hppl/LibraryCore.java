package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;

/**
 *
 * @author iconmaster
 */
public class LibraryCore extends SourcePackage {
	public LibraryCore() {
		this.name = "core";
		
		this.addType(TypeDef.UNKNOWN);
		this.addType(TypeDef.REAL);
		this.addType(TypeDef.STRING);
		this.addType(TypeDef.LIST);
		
		this.addFunction(Function.libraryFunction("print", new String[] {"item"}, new TypeDef[] {}, null));
		Function fn = Function.libraryFunction("range", new String[] {"begin","end"}, new TypeDef[] {TypeDef.REAL,TypeDef.REAL}, TypeDef.LIST);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("MAKELIST(X,X,");
			ctx.sb.append(ctx.plat.getInlineString(pkg, ctx.expr, ctx.op.args[2], ctx.vs));
			ctx.sb.append(",");
			ctx.sb.append(ctx.plat.getInlineString(pkg, ctx.expr, ctx.op.args[3], ctx.vs));
			return ")";
		};
		this.addFunction(fn);
		
	}
}
