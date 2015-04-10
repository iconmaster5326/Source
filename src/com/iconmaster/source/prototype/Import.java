package com.iconmaster.source.prototype;

import com.iconmaster.source.util.Range;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public class Import {
	public Range range;
	public List<String> name;
	public boolean file;
	public List<String> alias;
	public SourcePackage pkg;
	public List<String> dirs = new ArrayList<>();

	public Import() {
		
	}
}
