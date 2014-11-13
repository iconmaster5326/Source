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
	public boolean dirsMatter = true;
	
	public boolean isIter = false;
	public ArrayList<DataType> rets;

	public FunctionCall(String name, ArrayList<DataType> args, DataType ret, ArrayList<String> dirs) {
		this.name = name;
		this.args = args;
		this.ret = ret;
		this.dirs = dirs;
	}
	
	public FunctionCall(String name, ArrayList<DataType> args, ArrayList<DataType> rets, ArrayList<String> dirs) {
		this.name = name;
		this.args = args;
		this.isIter = true;
		this.ret = new DataType(true);
		this.rets = rets;
		this.dirs = dirs;
	}
}
