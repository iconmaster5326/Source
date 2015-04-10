package com.iconmaster.source.link;

import com.iconmaster.source.SourceInput;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author iconmaster
 */
public class LinkSpace {
	public SourceInput si;
	public Map<String,SourcePackage> loaded = new HashMap<>();

	public LinkSpace(SourceInput si) {
		this.si = si;
	}
}
