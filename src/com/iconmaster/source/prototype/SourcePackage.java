package com.iconmaster.source.prototype;

import com.iconmaster.source.util.Range;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author iconmaster
 */
public class SourcePackage {
	public Range range;
	public String name;
	public List<String> dirs = new ArrayList<>();
	SourcePackage parent = null;
	
	public Map<String, List<SourcePackage>> subPackages = new HashMap<>();
	public Map<String, List<Function>> functions = new HashMap<>();
	public Map<String, List<Field>> fields = new HashMap<>();

	public SourcePackage(Range range) {
		this.range = range;
	}

	public SourcePackage() {
		this(null);
	}

	public void addSubPackage(SourcePackage pkg) {
		if (!subPackages.containsKey(pkg.name)) {
			subPackages.put(pkg.name, new ArrayList<>());
		}
		subPackages.get(pkg.name).add(pkg);
	}
}
