package com.iconmaster.source.prototype;

import com.iconmaster.source.compile.DataType;
import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.element.Element;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Iterator extends Function {
	public ArrayList<DataType> iterReturns;
	
	public Iterator(String name, ArrayList<Field> args, Element returns) {
		super(name, args, returns);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getFullName()+args+" as "+iterReturns);
		if (code!=null) {
			sb.append(". CODE:");
			
			for (Operation op : code) {
				sb.append("\n\t");
				sb.append(op.toString());
			}
		}
		return sb.toString();
	}
	
	public static Iterator libraryIterator(String name, String[] args, Object[] argTypes, Object[] rets) {
		ArrayList<Field> argList = new ArrayList<>();
		ArrayList<DataType> retList = new ArrayList<>();
		
		int i = 0;
		for (String arg : args) {
			Element e = null;
			Field f = new Field(arg, null);
			if (i<argTypes.length) {
				DataType dt = null;
				if (argTypes[i] instanceof TypeDef) {
					dt = new DataType((TypeDef)argTypes[i],false);
				} else if (argTypes[i] instanceof DataType) {
					dt = (DataType) argTypes[i];
				}
				f.setType(dt);
			}
			argList.add(f);
			i++;
		}
		

		Iterator fn = new Iterator(name, argList, null);
		for (Object ret : rets) {
			DataType dt = null;
			if (ret instanceof TypeDef) {
				dt = new DataType((TypeDef)ret,false);
			} else if (ret instanceof DataType) {
				dt = (DataType) ret;
			}
			retList.add(dt);
		}
		fn.iterReturns = retList;
		fn.library = true;
		fn.compiled = true;
		return fn;
	}
}
