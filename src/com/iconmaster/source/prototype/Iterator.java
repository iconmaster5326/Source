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
}
