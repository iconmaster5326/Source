package com.iconmaster.source.prototype;

import com.iconmaster.source.compile.Expression;
import com.iconmaster.source.compile.Operation;
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
	
	protected boolean compiled;
	protected Expression value;

	public Variable(String name) {
		this(name,null);
	}

	public Variable(String name, DataType type) {
		this.name = name;
		this.type = type;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name+"="+type);
		if (value!=null) {
			sb.append(". CODE:");
			
			for (Operation op : value) {
				sb.append("\n\t");
				sb.append(op.toString());
			}
		}
		return sb.toString();
	}

	public String getName() {
		return name;
	}
	
	public void setCompiled(Expression code) {
		this.compiled = true;
		this.value = code;
	}

	public Element rawData() {
		return rawValue;
	}
	
	
}
