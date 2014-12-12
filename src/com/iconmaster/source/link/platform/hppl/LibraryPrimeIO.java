package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;

/**
 *
 * @author iconmaster
 */
public class LibraryPrimeIO extends SourcePackage {
	public static final double TIME_DELTA = Double.MIN_VALUE;
	
	public static Function fnChoose1;
	public static Function fnChoose2;

	public LibraryPrimeIO() {
		
		this.name = "prime.io";
		
//		Function fn = Function.libraryFunction("wait", new String[] {"time"}, new TypeDef[] {TypeDef.REAL}, null);
//		fn.compileName = "WAIT";
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("wait", new String[] {"time"}, new TypeDef[] {TypeDef.INT}, null);
//		fn.compileName = "WAIT";
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("wait", new String[] {}, new TypeDef[] {}, null);
//		fn.onCompile = (pkg,args)->{
//			return "WAIT("+TIME_DELTA+")";
//		};
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("waitForKey", new String[] {}, new TypeDef[] {}, TypeDef.INT);
//		fn.onCompile = (pkg,args)->{
//			return "WAIT(0)";
//		};
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("waitForInput", new String[] {}, new TypeDef[] {}, TypeDef.INT);
//		fn.onCompile = (pkg,args)->{
//			return "WAIT(-1)";
//		};
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("msgbox", new String[] {"message"}, new TypeDef[] {TypeDef.UNKNOWN}, null);
//		fn.compileName = "MSGBOX";
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("msgbox", new String[] {"message"}, new TypeDef[] {TypeDef.UNKNOWN}, TypeDef.INT);
//		fn.getDirectives().add("yesno");
//		fn.onCompile = (pkg,args)->{
//			PlatformContext ctx = (PlatformContext) args[0];
//			ctx.sb.append("MSGBOX(");
//			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
//			return ",1)";
//		};
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("cls", new String[] {}, new TypeDef[] {}, null);
//		fn.compileName = "PRINT";
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("getKey", new String[] {}, new TypeDef[] {}, TypeDef.INT);
//		fn.getDirectives().add("pure");
//		fn.compileName = "GETKEY";
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("isKeyDown", new String[] {"key"}, new TypeDef[] {TypeDef.INT}, TypeDef.INT);
//		fn.getDirectives().add("pure");
//		fn.compileName = "ISKEYDOWN";
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("getMouse", new String[] {}, new TypeDef[] {}, TypeDef.LIST);
//		fn.getDirectives().add("pure");
//		fn.compileName = "MOUSE";
//		this.addFunction(fn);
//		
//		fn = Function.libraryFunction("choose", new String[] {"items"}, new TypeDef[] {TypeDef.LIST}, TypeDef.INT);
//		fn.onCompile = (pkg,args)->{
//			PlatformContext ctx = (PlatformContext) args[0];
//			ctx.sb.append("CHOOSE(");
//			ctx.sb.append(ctx.op.args[2]);
//			ctx.sb.append(",\"\",");
//			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
//			return ")";
//		};
//		this.addFunction(fn);
//		fnChoose1 = fn;
//		
//		fn = Function.libraryFunction("choose", new String[] {"title","items"}, new TypeDef[] {TypeDef.STRING, TypeDef.LIST}, TypeDef.INT);
//		fn.onCompile = (pkg,args)->{
//			PlatformContext ctx = (PlatformContext) args[0];
//			ctx.sb.append("CHOOSE(");
//			ctx.sb.append(ctx.op.args[2]);
//			ctx.sb.append(",");
//			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
//			ctx.sb.append(",");
//			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[4]));
//			return ")";
//		};
//		this.addFunction(fn);
//		fnChoose2 = fn;
//		
//		Field f = Field.libraryField("key.esc", TypeDef.INT);
//		f.onCompile = (pkg,isGet,args)->{
//			return "4";
//		};
//		this.addField(f);
	}
}
