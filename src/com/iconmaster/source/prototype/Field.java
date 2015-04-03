package com.iconmaster.source.prototype;

import com.iconmaster.source.parse.Token;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public class Field {
	public String name;
	public Token rawCode = null;
	public Token rawDataType = null;
	public List<String> dirs = new ArrayList<>();
	public Range range;

	public Field(String name) {
		this.name = name;
	}

	public Field() {
		
	}

	@Override
	public String toString() {
		return "Field{" + "name=" + name + ", rawCode=" + rawCode + ", rawDataType=" + rawDataType + ", dirs=" + dirs + '}';
	}
}
