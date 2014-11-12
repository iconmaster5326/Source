package com.iconmaster.source.prototype;

import com.iconmaster.source.compile.DataType;
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
}
