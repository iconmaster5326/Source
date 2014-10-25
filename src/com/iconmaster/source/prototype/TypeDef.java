package com.iconmaster.source.prototype;

/**
 *
 * @author iconmaster
 */
public class TypeDef {
	public static final TypeDef UNKNOWN = new TypeDef("?");
	public static final TypeDef REAL = new TypeDef("real");
	public static final TypeDef STRING = new TypeDef("string");
	public static final TypeDef LIST = new TypeDef("list");
	
	public String name;
	public String pkgName;

	public TypeDef(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public TypeDef getHighestType(TypeDef other, boolean weak) {
		if (this==TypeDef.UNKNOWN) {
			return this;
		}
		if (this==other) {
			return this;
		}
		return weak?TypeDef.UNKNOWN:null;
	}
}
