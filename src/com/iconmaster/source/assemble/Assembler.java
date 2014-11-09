package com.iconmaster.source.assemble;

import com.iconmaster.source.compile.CompileUtils;
import com.iconmaster.source.compile.CompileUtils.CodeTransformer;
import com.iconmaster.source.link.Linker;
import com.iconmaster.source.link.Platform;
import com.iconmaster.source.prototype.SourcePackage;

/**
 *
 * @author iconmaster
 */
public class Assembler {
	public static String assemble(String platform, SourcePackage pkg) {
		Platform p = Linker.platforms.get(platform);
		p.registerCoreTypeHeirarchy();
		for (CodeTransformer t : p.transforms) {
			CompileUtils.transform(pkg, t);
		}
		return p.assemble(pkg);
	}
	
	public static void run(String platform, SourcePackage pkg) {
		Platform p = Linker.platforms.get(platform);
		p.run(pkg);
	}
}
