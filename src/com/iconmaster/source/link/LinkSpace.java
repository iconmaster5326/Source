package com.iconmaster.source.link;

import com.iconmaster.source.SourceInput;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.TokenUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author iconmaster
 */
public class LinkSpace {
	public SourceInput si;
	public List<SourcePackage> defaults = new ArrayList<>();
	public Map<String,SourcePackage> loaded = new HashMap<>();

	public LinkSpace(SourceInput si) {
		this.si = si;
	}
	
	public void addPackage(String name, SourcePackage pkg) {
		loaded.put(name,pkg);
	}
	
	public void addPackage(List<String> name, SourcePackage pkg) {
		loaded.put(TokenUtils.condense(name, "."),pkg);
	}
	
	public SourcePackage getPackage(String name) {
		return loaded.get(name);
	}
	
	public SourcePackage getPackage(List<String> name) {
		return loaded.get(TokenUtils.condense(name, "."));
	}
}
