package com.iconmaster.source;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.tokenize.Tokenizer;
import com.iconmaster.source.xml.ElementXML;
import com.iconmaster.source.xml.XMLHelper;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 *
 * @author iconmaster
 */
public class Source {

	public static void main(String[] args) {
		ArrayList<Element> a = null;
		try {
			a = Tokenizer.tokenize("2,-3,-4,5");
			//System.out.println(a);
			a = Parser.parse(a);
			//System.out.println(a);
		} catch (SourceException ex) {
			Logger.getLogger(Source.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		Document doc = XMLHelper.blankDoc();
		ElementXML.toXML(doc, XMLHelper.addTag(doc, "parse_result", ""), a);
		System.out.println(XMLHelper.toString(doc));
	}
	
}
