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
	}
}
