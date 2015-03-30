package com.iconmaster.source.prototype;

/**
 *
 * @author iconmaster
 */
public class Directive {
	public String name;
	public boolean negate;
	public String arg;

	public Directive(String name, boolean negate, String arg) {
		this.name = name;
		this.negate = negate;
		this.arg = arg;
	}
}
