package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;

/**
 *
 * @author iconmaster
 */
public class HPPLAssembler {
	public static String assemble(SourcePackage pkg) {
		AssemblyData ad = new AssemblyData(pkg);
		
		for (Function fn : pkg.getFunctions()) {
			if (shouldAssemble(ad, fn)) {
				String output = assembleFunction(ad, fn);
			}
		}
		
		StringBuilder sb = new StringBuilder("#pragma mode( separator(.,;) integer(h32) )\n//This program compiled with Source: www.github.com/iconmaster5326/Source\n\n");
		
		sb.append(HPPLCharacters.VAR_BEGIN);
		sb.append("var");
		return sb.toString();
	}
	
	public static String assembleFunction(AssemblyData ad, Function fn) {
		return "";
	}
	
	public static boolean shouldAssemble(AssemblyData ad, Function fn) {
		return !fn.isLibrary() && fn.isCompiled();
	}
}
