package com.iconmaster.source.link;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author iconmaster
 */
public abstract class Platform {
	public static Map<String, Platform> plats = new HashMap<>();
	
	protected String name;

	public String name() {
		return name;
	}
}
