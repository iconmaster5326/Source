package com.iconmaster.source.element;

import com.iconmaster.source.exception.SourceException;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public interface ISpecialRule {
	public class RuleResult {
		public Element ret;
		public int del;

		public RuleResult(Element ret, int del) {
			this.ret = ret;
			this.del = del;
		}		
	}
	
	public RuleResult match(ArrayList<Element> a, int i) throws SourceException;
}
