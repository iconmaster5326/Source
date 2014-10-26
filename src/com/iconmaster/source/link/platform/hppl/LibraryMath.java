package com.iconmaster.source.link.platform.hppl;

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
		
		this.addFunction(Function.libraryFunction("sin", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL));
		this.addFunction(Function.libraryFunction("cos", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL));
		this.addFunction(Function.libraryFunction("tan", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL));
		this.addFunction(Function.libraryFunction("log", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL));
		this.addFunction(Function.libraryFunction("ln", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL));
		this.addFunction(Function.libraryFunction("sqrt", new String[] {"n"}, new TypeDef[] {TypeDef.REAL}, TypeDef.REAL));
	}
}
