package com.iconmaster.source.prototype;

import com.iconmaster.source.element.Element;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Variable {
	protected String name;
	protected DataType type;
	protected Element rawValue;
	protected ArrayList<String> directives;

	public Variable(String name) {
		this(name,null);
	}

	public Variable(String name, DataType type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return name+"="+type;
	}
	
	
}
