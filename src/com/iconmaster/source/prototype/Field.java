package com.iconmaster.source.prototype;

import com.iconmaster.source.parse.Token;
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
	public List<Directive> dirs = new ArrayList<>();

	public Field(String name) {
		this.name = name;
	}
}
