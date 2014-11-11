package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.TypeDef;

/**
 *
 * @author iconmaster
 */
public class DataType {
	public TypeDef type;
	public boolean weak;

	@Override
	public String toString() {
		return ""+type+(weak?"?":"");
	}

	public DataType() {
		this(TypeDef.UNKNOWN,false);
	}
	
	public DataType(TypeDef def) {
		this(def,false);
	}
	
	public DataType(boolean def) {
		this(TypeDef.UNKNOWN,def);
	}
	
	public DataType(TypeDef def, boolean w) {
		type = def;
		weak = w;
	}
	
	public static DataType getNewType(DataType thisType, DataType other) {
		if (thisType==null) {
			return other;
		}
		if (other==null) {
			other = new DataType(true);
		}
		if (!thisType.weak) {
			return thisType;
		}
		return commonType(thisType, other);
	}
	
	public static DataType commonType(DataType type1, DataType type2) {
		if (type1==null) {
			type1 = new DataType(true);
		}
		if (type2==null) {
			type2 = new DataType(true);
		}
		return new DataType(TypeDef.getCommonParent(type1.type, type2.type),true);
	}
	
	public static boolean canCastTo(DataType thisType, DataType other) {
		if (thisType==null) {
			thisType = new DataType(true);
		}
		if (other==null) {
			other = new DataType(true);
		}
		if (thisType.weak) {
			return TypeDef.getCommonParent(thisType.type, other.type)!=null;
		} else {
			return TypeDef.getCommonParent(thisType.type, other.type)==thisType.type;
		}
	}
}
