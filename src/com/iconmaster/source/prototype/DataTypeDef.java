package com.iconmaster.source.prototype;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.tokenize.TokenRule;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class DataTypeDef {
	String name;
	ArrayList<String> params = new ArrayList<>();

	public DataTypeDef(Element e) throws SourceException {
		if (e==null) {
			name = "?";
			return;
		}
		if (e.type==TokenRule.WORD) {
			name = (String) e.args[0];
		} else if (e.type==Rule.ICALL) {
			if (e.args[0] instanceof String) {
				name = (String) e.args[0];
			} else if (e.args[0] instanceof Element && ((Element)e.args[0]).type==TokenRule.WORD) {
				name = (String) ((Element)e.args[0]).args[0];
			} else {
				throw new SourceException(e.range,"Invalid data type");
			}
			//add params here
			for (Element e2 : (ArrayList<Element>) e.args[1]) {
				if (e2.type==TokenRule.WORD) {
					params.add((String) e2.args[0]);
				} else {
					throw new SourceException(e.range,"Invalid data type");
				}
			}
		} else {
			throw new SourceException(e.range,"Invalid data type");
		}
	}
	
	public static ArrayList<DataTypeDef> getFuncReturn(Element e) throws SourceException {
		ArrayList<DataTypeDef> a = new ArrayList<>();
		if (e==null) {
			a.add(new DataTypeDef(null));
			return a;
		}
		if (e.type==Rule.TUPLE) {
			for (Element e2 : (ArrayList<Element>) e.args[0]) {
				a.add(new DataTypeDef(e2));
			}
		} else {
			a.add(new DataTypeDef(e));
		}
		return a;
	}

	@Override
	public String toString() {
		return name+(params.isEmpty()?"":params.toString());
	}
}
