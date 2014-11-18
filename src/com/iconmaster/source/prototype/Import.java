package com.iconmaster.source.prototype;

import com.iconmaster.source.util.Range;

/**
 *
 * @author iconmaster
 */
public class Import {
	public String name;
	public String alias;
	public boolean isFile;
	public boolean resolved = false;
	public boolean compiled = false;
	public Range range;
	public SourcePackage pkg = null;

	public Import(String name, String alias, boolean isFile, Range range) {
		this.name = name;
		this.alias = alias;
		this.isFile = isFile;
		this.range = range;
	}

	@Override
	public String toString() {
		return name;
	}
}
