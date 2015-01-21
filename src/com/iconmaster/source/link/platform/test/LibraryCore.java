package com.iconmaster.source.link.platform.test;

import com.iconmaster.source.compile.DataType;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.ParamTypeDef;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;

/**
 *
 * @author iconmaster
 */
public class LibraryCore extends SourcePackage {

	public LibraryCore() {
		this.name = "core";
		
		//Add all the base data types in core:
		this.addType(TypeDef.UNKNOWN);
		this.addType(TypeDef.REAL);
		this.addType(TypeDef.STRING);
		this.addType(TypeDef.LIST);
		this.addType(TypeDef.INT);
		this.addType(TypeDef.BOOLEAN);
		
		//define list parameter types here, becuse it's kind of complex:
		DataType ltdt = new DataType(TypeDef.LIST); //this is list[T]
		TypeDef ltt = new ParamTypeDef("T", 0); //this is T
		ltdt.params = new DataType[] {new DataType(ltt)};
		
		//add functions:
		Function fn;
		
		fn = Function.libraryFunction("print", new String[] {"item"}, new TypeDef[] {TypeDef.UNKNOWN}, null);
		this.addFunction(fn);
		
		fn = Function.libraryFunction("int._add", new String[] {"item1","item2"}, new TypeDef[] {TypeDef.INT,TypeDef.INT}, null);
		this.addFunction(fn);
	}
}
