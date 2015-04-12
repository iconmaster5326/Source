package com.iconmaster.source.link;

import com.iconmaster.source.SourceInput;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public class LinkSpace {
	public SourceInput si;
	public List<SourcePackage> loaded = new ArrayList<>();

	public LinkSpace(SourceInput si) {
		this.si = si;
	}
	
	public void addPackage(SourcePackage pkg) {
		loaded.add(pkg);
	}
	
	public SourcePackage findPackage(String name) {
		return findPackage(Arrays.asList(name.split("\\.")));
	}
	
	public SourcePackage findPackage(List<String> name) {
		name = new ArrayList<>(name);
		String called = null;
		List<SourcePackage> looking = new ArrayList<>(loaded);
		while (!name.isEmpty()) {
			called = name.remove(0);
			List<SourcePackage> newList = new ArrayList<>();
			for (SourcePackage pkg : looking) {
				if (pkg.subPackages.containsKey(called)) {
					newList.add(pkg);
				}
			}
			if (looking.isEmpty()) {
				return null;
			}
			looking = newList;
		}
		SourcePackage pkg = SourcePackage.merge(looking);
		pkg.name = called;
		return pkg;
	}
}
