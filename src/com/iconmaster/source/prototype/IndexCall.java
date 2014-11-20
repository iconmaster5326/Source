package com.iconmaster.source.prototype;

import com.iconmaster.source.compile.DataType;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class IndexCall extends FunctionCall {

	public IndexCall(String name, ArrayList<DataType> args, DataType ret, ArrayList<String> dirs) {
		super(name, args, ret, dirs);
	}
	
}
