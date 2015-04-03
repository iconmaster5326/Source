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
	
	public Map<String, SourcePackage> subPackages = new HashMap<>();
	public Map<String, List<Function>> functions = new HashMap<>();
	public Map<String, List<Field>> fields = new HashMap<>();

	public SourcePackage(Range range) {
		this.range = range;
	}

	public SourcePackage() {
		this(null);
	}

	public SourcePackage(String name, Range range) {
		this.range = range;
		this.name = name;
	}
	
	public SourcePackage getPackage(String name) {
		return getPackage(name, range);
	}

	public SourcePackage getPackage(String name, Range rn) {
		if (subPackages.containsKey(name)) {
			return subPackages.get(name);
		} else {
			SourcePackage pkg = new SourcePackage(name, rn);
			pkg.parent = this;
			subPackages.put(name, pkg);
			return pkg;
		}
	}
	
	public void addFunction(Function fn) {
		if (!functions.containsKey(fn.name)) {
			functions.put(fn.name, new ArrayList<>());
		}
		functions.get(fn.name).add(fn);
	}

	@Override
	public String toString() {
		return "SourcePackage{" + "range=" + range + ", name=" + name + ", dirs=" + dirs + ", subPackages=" + subPackages + ", functions=" + functions + ", fields=" + fields + '}';
	}
}
