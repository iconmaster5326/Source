package com.iconmaster.source.prototype;

/**
 *
 * @author iconmaster
 */
public class TypeDef {
	public static final TypeDef UNKNOWN = new TypeDef();
	public static final TypeDef REAL = new TypeDef("real");
	public static final TypeDef INT = new TypeDef("int");
	public static final TypeDef STRING = new TypeDef("string").setIndexSettings(new TypeDef[] {TypeDef.INT}, false);
	public static final TypeDef LIST = new TypeDef("list").setIndexSettings(new TypeDef[] {TypeDef.INT}, false).setParamSettings(new TypeDef[] {TypeDef.UNKNOWN}, false);
	public static final TypeDef BOOLEAN = new TypeDef("bool");
	
	public String name;
	public String pkgName;
	
	public TypeDef parent = null;
	
	public boolean indexable = false;
	public TypeDef[] indexableBy = new TypeDef[0];
	public boolean varargIndex = false;
	
	public boolean hasParams = false;
	public TypeDef[] params = new TypeDef[0];
	public boolean varargParams = false;
	
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
	
	public TypeDef setIndexSettings(TypeDef[] indexableBy, boolean varargIndex) {
		indexable = true;
		this.indexableBy = indexableBy;
		this.varargIndex = varargIndex;
		return this;
	}
	
	public TypeDef setParamSettings(TypeDef[] params, boolean varargParams) {
		hasParams = true;
		this.params = params;
		this.varargParams = varargParams;
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
