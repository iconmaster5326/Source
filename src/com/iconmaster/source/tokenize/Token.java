package com.iconmaster.source.tokenize;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.IElementType;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Token extends Element {
	public Token(Range range, IElementType type, Object value) {
		super(range,type);
		this.args[0] = value;
	}
	
	@Override
	public String toString() {
		return "["+type+" "+args[0]+"]";
	}
	
	public String string() {
		return (String) args[0];
	}
	
	public ArrayList<Element> array() {
		return (ArrayList<Element>) args[0];
	}
	
	public boolean isString() {
		return args[0] instanceof String;
	}
	
	public boolean isArray() {
		return args[0] instanceof ArrayList;
	}
}
