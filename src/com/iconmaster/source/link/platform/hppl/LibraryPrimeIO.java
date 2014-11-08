package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;

/**
 *
 * @author iconmaster
 */
public class LibraryPrimeIO extends SourcePackage {
	public static final double TIME_DELTA = Double.MIN_VALUE;

	public LibraryPrimeIO() {
		
		this.name = "prime.io";
		
		Function fn = Function.libraryFunction("wait", new String[] {"time"}, new TypeDef[] {TypeDef.REAL}, null);
		fn.compileName = "WAIT";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("wait", new String[] {}, new TypeDef[] {}, null);
		fn.onCompile = (pkg,args)->{
			return "WAIT("+TIME_DELTA+")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("keywait", new String[] {}, new TypeDef[] {}, TypeDef.REAL);
		fn.onCompile = (pkg,args)->{
			return "WAIT(0)";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("keywait", new String[] {}, new TypeDef[] {}, TypeDef.REAL);
		fn.getDirectives().add("mouse");
		fn.onCompile = (pkg,args)->{
			return "WAIT(-1)";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("msgbox", new String[] {"message"}, new TypeDef[] {TypeDef.UNKNOWN}, null);
		fn.compileName = "MSGBOX";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("msgbox", new String[] {"message"}, new TypeDef[] {TypeDef.UNKNOWN}, TypeDef.REAL);
		fn.getDirectives().add("yesno");
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("MSGBOX(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			return ",1)";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("cls", new String[] {}, new TypeDef[] {}, null);
		fn.compileName = "PRINT";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("getKey", new String[] {}, new TypeDef[] {}, TypeDef.REAL);
		fn.compileName = "GETKEY";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("isKeyDown", new String[] {"key"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL);
		fn.compileName = "ISKEYDOWN";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("getMouse", new String[] {}, new TypeDef[] {}, TypeDef.LIST);
		fn.compileName = "MOUSE";
		this.addFunction(fn);
	}
}
