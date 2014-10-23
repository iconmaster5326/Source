package com.iconmaster.source.element;

import com.iconmaster.source.util.IDirectable;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Element implements IDirectable {
	public IElementType type;
	public Object[] args = new Object[10];
	public Element dataType = null;
	public ArrayList<String> directives = new ArrayList<>();
	public Range range;

	public Element(Range range, IElementType type) {
		this.range = range;
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append(type).append(" as ").append(dataType).append(": [").append(args[0]).append(" ").append(args[1]).append("]");
		return sb.append("}").toString();
	}

	@Override
	public ArrayList<String> getDirectives() {
		return directives;
	}
}
