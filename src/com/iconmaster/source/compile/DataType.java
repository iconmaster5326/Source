package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.TypeDef;

/**
 *
 * @author iconmaster
 */
public class DataType {
	public TypeDef type = TypeDef.UNKNOWN;
	public boolean weak = false;

	@Override
	public String toString() {
		return ""+type;
	}
}
