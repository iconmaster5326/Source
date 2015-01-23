package com.iconmaster.source.prototype;

/**
 *
 * @author iconmaster
 */
public class TypeDef {
	public static final TypeDef UNKNOWN = new TypeDef();
	public static final TypeDef STRING = new TypeDef("string");
	public static final TypeDef ARRAY = new TypeDef("array");
	public static final TypeDef LIST = new TypeDef("list");
	public static final TypeDef BOOLEAN = new TypeDef("bool");
	public static final TypeDef MAP = new TypeDef("map");
	public static final TypeDef PTR = new TypeDef("ptr");
	public static final TypeDef FPTR = new TypeDef("fptr");
	
	public static final TypeDef INT8 = new TypeDef("int8");
	public static final TypeDef INT16 = new TypeDef("int16");
	public static final TypeDef INT32 = new TypeDef("int32");
	public static final TypeDef INT64 = new TypeDef("int64");
	
	public static final TypeDef REAL32 = new TypeDef("real32");
	public static final TypeDef REAL64 = new TypeDef("real64");
	
	public static final TypeDef REAL = new TypeDef("real", TypeDef.REAL32);
	public static final TypeDef INT = new TypeDef("int", TypeDef.INT32);
	public static final TypeDef CHAR = new TypeDef("char", TypeDef.INT8);
	
	public String name;
	public String pkgName;
	
	public TypeDef parent = null;
	
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
	
	public static void addBaseTypes(SourcePackage pkg) {
		pkg.addType(TypeDef.UNKNOWN);
		pkg.addType(TypeDef.INT);
		pkg.addType(TypeDef.REAL);
		pkg.addType(TypeDef.STRING);
		pkg.addType(TypeDef.ARRAY);
		pkg.addType(TypeDef.LIST);
		pkg.addType(TypeDef.CHAR);
		pkg.addType(TypeDef.BOOLEAN);
		pkg.addType(TypeDef.MAP);
		pkg.addType(TypeDef.INT8);
		pkg.addType(TypeDef.INT16);
		pkg.addType(TypeDef.INT32);
		pkg.addType(TypeDef.INT64);
		pkg.addType(TypeDef.REAL32);
		pkg.addType(TypeDef.REAL64);
	}
}
