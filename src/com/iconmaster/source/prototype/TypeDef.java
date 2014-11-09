package com.iconmaster.source.prototype;

/**
 *
 * @author iconmaster
 */
public class TypeDef {
	public static final TypeDef UNKNOWN = new TypeDef();
	public static final TypeDef REAL = new TypeDef("real");
	public static final TypeDef INT = new TypeDef("int", TypeDef.REAL);
	public static final TypeDef STRING = new TypeDef("string");
	public static final TypeDef LIST = new TypeDef("list").setIndexSettings(TypeDef.INT, 1);
	
	public String name;
	public String pkgName;
	
	public TypeDef parent = null;
	public TypeDef indexableBy = null;
	public int indices = -1;
	
	public TypeDef() {
		this.name = "?";
	}

	public TypeDef(String name) {
		this(name,TypeDef.UNKNOWN);
	}
	
	public TypeDef(String name, TypeDef parent) {
		this.name = name;
		this.parent = parent;
	}
	
	public TypeDef setIndexSettings(TypeDef indexableBy, int indicies) {
		this.indexableBy = indexableBy;
		this.indices = indicies;
		return this;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static TypeDef getCommonParent(TypeDef type1,TypeDef type2) {
		if (type1==null || type2==null) {
			return TypeDef.UNKNOWN;
		}
		if (type1==type2) {
			return type1;
		}
		if (type1.parent==type2) {
			return type2;
		}
		if (type1==type2.parent) {
			return type1;
		}
		return getCommonParent(type1.parent,type2.parent);
	}
}
