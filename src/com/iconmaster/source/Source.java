package com.iconmaster.source;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.tokenize.Tokenizer;
import com.iconmaster.source.xml.ElementXML;
import com.iconmaster.source.xml.XMLHelper;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 *
 * @author iconmaster
 */
public class Source {

	public static void main(String[] args) {
		Scanner kbd = new Scanner(System.in);
		while (true) {
			ArrayList<Element> a = null;
			try {
				System.out.print("Enter an expression: ");
				String input = kbd.next();
				a = Tokenizer.tokenize(input);
				a = Parser.parse(a);
			} catch (SourceException ex) {
				Logger.getLogger(Source.class.getName()).log(Level.SEVERE, null, ex);
			}

			System.out.println(a);
			Document doc = XMLHelper.blankDoc();
			ElementXML.toXML(doc, XMLHelper.addTag(doc, "parse_result", ""), a);
			System.out.println(XMLHelper.toString(doc));
		}
	}
	
}
