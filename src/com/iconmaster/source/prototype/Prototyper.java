package com.iconmaster.source.prototype;

import com.iconmaster.source.parse.Token;
import com.iconmaster.source.parse.TokenType;
import com.iconmaster.source.util.TokenUtils;
import java.util.ArrayList;
import java.util.List;

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
				List<Token> names = TokenUtils.getTokens(code, TokenType.LINK);
				for (Token t : names) {
					if (t.type==TokenType.WORD) {
						SourcePackage oldPkg = ctx.pkg;
						ctx.pkg = new SourcePackage(oldPkg.range);
						ctx.pkg.name = t.data;
						ctx.pkg.parent = oldPkg;
						oldPkg.addSubPackage(ctx.pkg);
					} else {
						//error
					}
				}
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
