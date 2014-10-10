package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;

/**
 *
 * @author iconmaster
 */
public class LibraryCore extends SourcePackage {
	public LibraryCore() {
		this.name = "source.core";
		
		this.addFunction(Function.libraryFunction("range", new String[] {"begin","end"}, new String[] {"real","real"}, new String[] {"list"}));
	}
}
