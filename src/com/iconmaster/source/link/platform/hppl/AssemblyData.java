package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author iconmaster
 */
public class AssemblyData {
	public SourcePackage pkg;
	public Stack<AssembleVarSpace> vs;
	public ArrayList<String> dirs;
	public Object workingOn;
}
