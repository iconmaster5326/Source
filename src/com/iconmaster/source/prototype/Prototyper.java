package com.iconmaster.source.prototype;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.exception.SourceException;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Prototyper {
	public static class PrototypeResult {
		public SourcePackage result;
		public ArrayList<SourceException> errors;

		public PrototypeResult(SourcePackage result, ArrayList<SourceException> errors) {
			this.result = result;
			this.errors = errors;
		}
	}
	
	public static PrototypeResult prototype(ArrayList<Element> a) {
		SourcePackage pkg = new SourcePackage();
		ArrayList<SourceException> errors = pkg.parse(a);
		return new PrototypeResult(pkg,errors);
	}
}
