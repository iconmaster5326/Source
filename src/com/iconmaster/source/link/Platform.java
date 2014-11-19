package com.iconmaster.source.link;

import com.iconmaster.source.compile.CompileUtils.CodeTransformer;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Directives;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public abstract class Platform {
	public String name;
	public HashMap<String,SourcePackage> pkgs = new HashMap<>();
	public ArrayList<CodeTransformer> transforms = new ArrayList<>();
	public HashMap<String,ImportHandler> importHandlers = new HashMap<>();
	
	protected void registerLibrary(SourcePackage pkg) {
		pkgs.put(pkg.getName(),pkg);
	}
	
	public abstract boolean canAssemble();
	public abstract boolean canRun();

	public abstract String assemble(SourcePackage pkg);
	public abstract void run(SourcePackage pkg);
	
	public String getCompileName(SourcePackage pkg, Function fn, String name) {
		return name;
	}

	public String getCompileName(SourcePackage pkg, Field fn, String name) {
		return name;
	}
	
	public static boolean shouldIncludeFunction(Function fn) {
		if (Directives.has(fn, "keep") || Directives.has(fn, "export") || Directives.has(fn, "main")) {
			return true;
		}
		if (Directives.has(fn, "inline") || Directives.has(fn, "native") || Directives.has(fn, "!keep")) {
			return false;
		}
		if (fn.isCompiled() && !fn.isLibrary()) {
			if (fn.references!=0) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean shouldIncludeField(Field fn) {
		return !fn.isLibrary() && !Directives.has(fn, "inline") && !Directives.has(fn, "native");
	}
}
