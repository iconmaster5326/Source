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
		fn.compileName = "GROBW_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.height", new String[] {"g"}, new TypeDef[] {GROB_TYPE}, TypeDef.REAL);
		fn.compileName = "GROBH_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.resize", new String[] {"g","w","h"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "DIMGROB_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.resize", new String[] {"g","w","h","color"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "DIMGROB_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.copyTo", new String[] {"g1","g2"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL}, null);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("BLIT_P(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.copyRegionTo", new String[] {"g1","g2","x1","y1","x2","y2"}, new TypeDef[] {GROB_TYPE, GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("SUBGROB_P(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[4]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[5]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[6]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[7]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.copyScaledRegionTo", new String[] {"g1","g2","sx1","sy1","sx2","sy2","dx1","dy1","dx2","dy2"}, new TypeDef[] {GROB_TYPE, GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("BLIT_P(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[8]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[9]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[10]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[11]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[4]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[5]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[6]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[7]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawArc", new String[] {"g","x","y","r"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "ARC_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawArc", new String[] {"g","x","y","r","color"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "ARC_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawArc", new String[] {"g","x","y","r","a1","a2"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "ARC_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawArc", new String[] {"g","x","y","r","a1","a2","color"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "ARC_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawPoly", new String[] {"g","coords","color"}, new TypeDef[] {GROB_TYPE, TypeDef.LIST, TypeDef.REAL}, null);
		fn.compileName = "FILLPOLY_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawPoly", new String[] {"g","coords","color","alpha"}, new TypeDef[] {GROB_TYPE, TypeDef.LIST, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "FILLPOLY_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.getPixel", new String[] {"g","x","y"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "GETPIX_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.setPixel", new String[] {"g","x","y"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "PIXON_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.setPixel", new String[] {"g","x","y","color"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "PIXON_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.fill", new String[] {"g","color"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL}, null);
		fn.compileName = "RECT_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.clear", new String[] {"g"}, new TypeDef[] {GROB_TYPE}, null);
		fn.compileName = "RECT_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawRect", new String[] {"g","x1","y1","x2","y2"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "RECT_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawRect", new String[] {"g","x1","y1","x2","y2","color"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "RECT_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawRect", new String[] {"g","x1","y1","x2","y2","edgeColor","fillColor"}, new TypeDef[] {GROB_TYPE, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.compileName = "RECT_P";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawString", new String[] {"g","text","x","y"}, new TypeDef[] {GROB_TYPE,TypeDef.STRING, TypeDef.REAL, TypeDef.REAL}, null);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("TEXTOUT_P(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[4]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[5]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawString", new String[] {"g","text","x","y","font"}, new TypeDef[] {GROB_TYPE,TypeDef.STRING, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("TEXTOUT_P(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[4]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[5]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[6]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawString", new String[] {"g","text","x","y","font","textColor"}, new TypeDef[] {GROB_TYPE,TypeDef.STRING, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("TEXTOUT_P(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[4]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[5]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[6]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[7]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawString", new String[] {"g","text","x","y","font","textColor","backColor"}, new TypeDef[] {GROB_TYPE,TypeDef.STRING, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("TEXTOUT_P(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[4]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[5]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[6]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[7]));
			ctx.sb.append(",999,");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[8]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("grob.drawString", new String[] {"g","text","x","y","font","textColor","backColor","width"}, new TypeDef[] {GROB_TYPE,TypeDef.STRING, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, null);
		fn.onCompile = (pkg,args)->{
			PlatformContext ctx = (PlatformContext) args[0];
			ctx.sb.append("TEXTOUT_P(");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[3]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[2]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[4]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[5]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[6]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[7]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[9]));
			ctx.sb.append(",");
			ctx.sb.append(HPPLAssembler.getInlineString(ctx.ad, ctx.expr, ctx.op.args[8]));
			return ")";
		};
		this.addFunction(fn);
		
		fn = Function.libraryFunction("rgb", new String[] {"r","g","b"}, new TypeDef[] {TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, TypeDef.REAL);
		fn.compileName = "RGB";
		this.addFunction(fn);
		
		fn = Function.libraryFunction("rgb", new String[] {"r","g","b","a"}, new TypeDef[] {TypeDef.REAL, TypeDef.REAL, TypeDef.REAL, TypeDef.REAL}, TypeDef.REAL);
		fn.compileName = "RGB";
		this.addFunction(fn);
		
		Field f = Field.libraryField("screen", GROB_TYPE);
		f.onCompile = (pkg,isGet,args)->{
			return "G0";
		};
		this.addField(f);
		
		for (int i=1;i<=9;i++) {
			final int ii = i;
			f = Field.libraryField("buffer"+i, GROB_TYPE);
			f.onCompile = (pkg,isGet,args)->{
				return "G"+ii;
			};
			this.addField(f);
		}
	}
}
