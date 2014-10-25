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
}
