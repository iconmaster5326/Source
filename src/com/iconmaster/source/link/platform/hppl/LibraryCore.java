package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Field;
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
		this.addType(TypeDef.INT);
		this.addType(TypeDef.STRING);
		this.addType(TypeDef.LIST);
		this.addType(TypeDef.BOOLEAN);
		
		Function fn = Function.libraryFunction("print", new String[] {"item"}, new TypeDef[] {}, null);
		fn.compileName = "PRINT";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("range", new String[] {"begin","end"}, new TypeDef[] {TypeDef.REAL,TypeDef.REAL}, TypeDef.LIST);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("MAKELIST(X,X,");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("range", new String[] {"begin","end","step"}, new TypeDef[] {TypeDef.REAL,TypeDef.REAL,TypeDef.REAL}, TypeDef.LIST);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("MAKELIST(X,X,");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[4]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("list.size", new String[] {"list"}, new TypeDef[] {TypeDef.LIST}, TypeDef.REAL);
		fn.compileName = "SIZE";
		this.addFunction(fn);
		fn = Function.libraryFunction("list.append", new String[] {"list","item"}, new TypeDef[] {TypeDef.LIST,TypeDef.UNKNOWN}, TypeDef.LIST);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("CONCAT(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",{");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			return "})";
		};
		this.addFunction(fn);
		fn = Function.libraryFunction("list.join", new String[] {"list1","list2"}, new TypeDef[] {TypeDef.LIST,TypeDef.LIST}, TypeDef.LIST);
		fn.compileName = "CONCAT";
		this.addFunction(fn);
		fn = Function.libraryFunction("list.first", new String[] {"list"}, new TypeDef[] {TypeDef.LIST}, TypeDef.UNKNOWN);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			return "[1]";
		};
		this.addFunction(fn);
		fn = Function.libraryFunction("list.last", new String[] {"list"}, new TypeDef[] {TypeDef.LIST}, TypeDef.UNKNOWN);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append("[SIZE(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			return ")]";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("string._cast", new String[] {"item"}, new TypeDef[] {TypeDef.UNKNOWN}, TypeDef.STRING);
		fn.compileName = "STRING";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("real._cast", new String[] {"item"}, new TypeDef[] {TypeDef.STRING}, TypeDef.REAL);
		fn.compileName = "EXPR";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("real._cast", new String[] {"item"}, new TypeDef[] {TypeDef.INT}, TypeDef.REAL);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			return HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]);
		};
		this.addFunction(fn);
		
		Field f = Field.libraryField("list.start", TypeDef.REAL);
		f.onCompile = (pkg,isGet,args) -> {
			return "1";
		};
		this.addField(f);
	}
}
