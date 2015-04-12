package com.iconmaster.source.link;

import com.iconmaster.source.SourceInput;
import com.iconmaster.source.SourceInput.VerboseLevel;
import com.iconmaster.source.exception.SourceError;
import com.iconmaster.source.prototype.Import;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Result;
import com.iconmaster.source.util.TokenUtils;
import java.util.ArrayList;
import java.util.List;

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
	
	public static Result<Boolean> addLibs(LinkSpace ls) {
		for (SourcePackage pkg : ls.si.libraries) {
			ls.si.println(VerboseLevel.DEBUG, "Loading package: "+pkg);
			
			ls.addPackage(pkg);
		}
		
		return new Result<>(true);
	}
	
	public static Result<Boolean> link(LinkSpace ls, SourcePackage pkg) {
		List<SourceError> errs = new ArrayList<>();
		
		for (Import imp : pkg.imports) {
			if (imp.file) {
				
			} else {
				SourcePackage found = ls.findPackage(imp.name);
				if (found==null) {
					errs.add(new SourceError(SourceError.ErrorType.UNRESOLVED_IMPORT, imp.range, "Unresolved import for package "+TokenUtils.condense(imp.name, ".")));
				} else {
					imp.pkg = found;
				}
			}
		}
		
		if (!errs.isEmpty()) {
			return new Result<>(errs.toArray(new SourceError[0]));
		}
		return new Result<>(true);
	}
}
