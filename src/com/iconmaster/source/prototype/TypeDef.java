package com.iconmaster.source.prototype;

/**
 *
 * @author iconmaster
 */
public class TypeDef {
	public static final TypeDef UNKNOWN = new TypeDef("?");
	public static final TypeDef REAL = new TypeDef("real");
	
	public String name;
	public String pkgName;

	public TypeDef(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
