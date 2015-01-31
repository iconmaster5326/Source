package com.iconmaster.source.prototype;

import com.iconmaster.source.element.Element;

/**
 *
 * @author iconmaster
 */
public class CustomType extends TypeDef {
	public Element rawType;

	public CustomType(String name, Element type) {
		super(name);
		this.rawType = type;
	}
}
