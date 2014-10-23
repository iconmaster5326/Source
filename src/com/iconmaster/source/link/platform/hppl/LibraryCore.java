package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;

/**
 *
 * @author iconmaster
 */
public class LibraryCore extends SourcePackage {
	public LibraryCore() {
		this.name = "CORE";
		
		this.addFunction(Function.libraryFunction("print", new String[] {"item"}, new String[] {}, new String[] {}));
		
		Function fn = Function.libraryFunction("range", new String[] {"begin","end"}, new String[] {"real","real"}, new String[] {"list"});
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
