package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.TypeDef;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Expression extends ArrayList<Operation> {
	public String retVar;
	public DataType type = new DataType(TypeDef.UNKNOWN,true);
}
