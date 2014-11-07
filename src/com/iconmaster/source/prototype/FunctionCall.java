package com.iconmaster.source.prototype;

import com.iconmaster.source.compile.DataType;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class FunctionCall {
	public String name;
	public ArrayList<DataType> args;
	public DataType ret;
	public ArrayList<String> dirs;

	public FunctionCall(String name, ArrayList<DataType> args, DataType ret, ArrayList<String> dirs) {
		this.name = name;
		this.args = args;
		this.ret = ret;
		this.dirs = dirs;
	}
}
