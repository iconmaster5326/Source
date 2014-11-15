package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;

/**
 *
 * @author iconmaster
 */
public class LibraryMath extends SourcePackage {
	public LibraryMath() {
		this.name = "math";
		Function fn = Function.libraryFunction("sin", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		fn = Function.libraryFunction("cos", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		fn = Function.libraryFunction("tan", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		fn = Function.libraryFunction("log", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		fn = Function.libraryFunction("ln", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		fn = Function.libraryFunction("sqrt", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		
		Field f = Field.libraryField("pi", TypeDef.REAL);
		f.onCompile = (pkg,isGet,args)->{
			return HPPLCharacters.PI;
		};
		this.addField(f);
		
		f = Field.libraryField("e", TypeDef.REAL);
		f.onCompile = (pkg,isGet,args)->{
			return "e";
		};
		this.addField(f);
	}
}
