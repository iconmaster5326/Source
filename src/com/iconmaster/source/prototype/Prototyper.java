package com.iconmaster.source.prototype;

import com.iconmaster.source.parse.Token;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Prototyper {
	public static class PrototyperContext {
		public SourcePackage root;
		public SourcePackage pkg;
		public ArrayList<String> dirs = new ArrayList<>();

		public PrototyperContext(SourcePackage root) {
			this.root = root;
			this.pkg = root;
		}
	}
	
	public static SourcePackage prototype(Token code) {
		SourcePackage pkg = new SourcePackage(code.range);
		prototype(code, new PrototyperContext(pkg));
		return pkg;
	}
	
	public static void prototype(Token code, PrototyperContext ctx) {
		switch (code.type) {
			case STATEMENT:
				prototype(code.r,ctx);
				prototype(code.l,ctx);
				break;
			case PACKAGE:
				break;
			case IMPORT:
				break;
			case FUNCTION:
				ctx.dirs.clear();
				break;
			case FIELD:
				break;
			case GLOBAL_DIR:
				ctx.pkg.dirs.add(code.data);
				break;
			case LOCAL_DIR:
				ctx.dirs.add(code.data);
				prototype(code.l,ctx);
				break;
			default:
				//error
				break;
		}
	}
}
