package com.iconmaster.source.compile;

import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class CompileData {
	public ArrayList<String> dirs;
	public ArrayList<SourceException> errs = new ArrayList<>();
	public SourcePackage pkg;
	public ScopeFrame frame;
	public Object workingOn;

	public CompileData(SourcePackage pkg) {
		this.pkg = pkg;
		this.frame = new ScopeFrame(pkg);
	}
	
	
}
