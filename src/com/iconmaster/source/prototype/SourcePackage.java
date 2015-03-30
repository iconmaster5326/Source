package com.iconmaster.source.prototype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author iconmaster
 */
public class SourcePackage {
	public Map<String, List<SourcePackage>> subPackages = new HashMap<>();
	public Map<String, List<Function>> functions = new HashMap<>();
	public Map<String, List<Field>> fields = new HashMap<>();
}
