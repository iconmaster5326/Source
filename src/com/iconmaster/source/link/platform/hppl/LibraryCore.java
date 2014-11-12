package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.compile.DataType;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.ParamTypeDef;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;
import java.util.ArrayList;

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
		
		fn = Function.libraryFunction("list.size", new String[] {"list"}, new TypeDef[] {TypeDef.LIST}, TypeDef.INT);
		fn.compileName = "SIZE";
		this.addFunction(fn);
		
		DataType ltdt = new DataType(TypeDef.LIST);
		ltdt.params = new DataType[] {new DataType(new ParamTypeDef("T", 0))};
		
		fn = Function.libraryFunction("list.append", new String[] {"list","item"}, new Object[] {ltdt,new ParamTypeDef("T", 0)}, ltdt);
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("T"));
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("CONCAT(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",{");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			return "})";
		};
		this.addFunction(fn);
		fn = Function.libraryFunction("list.join", new String[] {"list1","list2"}, new Object[] {ltdt,ltdt}, ltdt);
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("T"));
		fn.compileName = "CONCAT";
		this.addFunction(fn);
		fn = Function.libraryFunction("list.first", new String[] {"list"}, new Object[] {ltdt}, new ParamTypeDef("T", 0));
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("T"));
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			return "[1]";
		};
		this.addFunction(fn);
		fn = Function.libraryFunction("list.last", new String[] {"list"}, new Object[] {ltdt}, new ParamTypeDef("T", 0));
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("T"));
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
		
		fn = Function.libraryFunction("int._cast", new String[] {"item"}, new TypeDef[] {TypeDef.STRING}, TypeDef.INT);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("IP(EXPR(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			return "))";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("int._cast", new String[] {"item"}, new TypeDef[] {TypeDef.REAL}, TypeDef.INT);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("IP(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			return "))";
		};
		this.addFunction(fn);
		
		Field f = Field.libraryField("list.start", TypeDef.INT);
		f.onCompile = (pkg,isGet,args) -> {
			return "1";
		};
		this.addField(f);
	}
}
