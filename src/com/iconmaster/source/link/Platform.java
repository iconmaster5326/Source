package com.iconmaster.source.link;

import com.iconmaster.source.prototype.SourcePackage;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author iconmaster
 */
public abstract class Platform {
	public static Map<String, Platform> plats = new HashMap<>();
	
	protected String name;
	protected SourcePackage pkg;

	public String name() {
		return name;
	}
	
	public SourcePackage packages() {
		return pkg;
	}
}
