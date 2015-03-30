package com.iconmaster.source.prototype;

import com.iconmaster.source.parse.Token;

/**
 *
 * @author iconmaster
 */
public class Prototyper {
	public static SourcePackage prototype(Token code) {
		SourcePackage pkg = new SourcePackage(code.range);
		return prototype(code, pkg);
	}
	
	public static SourcePackage prototype(Token code, SourcePackage pkg) {
		switch (code.type) {
			case STATEMENT:
				prototype(code.r,pkg);
				prototype(code.l,pkg);
				break;
			case PACKAGE:
				break;
			case IMPORT:
				break;
			case FUNCTION:
				break;
			case FIELD:
				break;
			default:
				//error
				break;
		}
		return pkg;
	}
}
