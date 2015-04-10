package com.iconmaster.source.link;

import com.iconmaster.source.SourceInput;
import com.iconmaster.source.prototype.Import;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Result;

/**
 *
 * @author iconmaster
 */
public class Linker {
	public static Result<LinkSpace> link(SourceInput si, SourcePackage pkg) {
		if (si.platform==null && si.platformName!=null) {
			si.platform = Platform.plats.get(si.platformName);
		}
		
		LinkSpace ls = new LinkSpace(si);
		
		Result res;
		
		res = addLibs(ls);
		if (res.failed) {
			return res;
		}
		
		res = link(ls, pkg);
		if (res.failed) {
			return res;
		}
		
		return new Result<>(ls);
	}
	
	public static Result addLibs(LinkSpace ls) {
		for (Import imp : ls.si.libraries) {
			ls.addPackage(imp.name, imp.pkg);
		}
		
		return new Result(true);
	}
	
	public static Result link(LinkSpace ls, SourcePackage pkg) {
		for (Import imp : pkg.imports) {
			
		}
		
		return new Result(true);
	}
}
