package com.iconmaster.source;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.tokenize.Tokenizer;
import com.iconmaster.source.util.CLAHelper;
import com.iconmaster.source.util.CLAHelper.CLA;
import com.iconmaster.source.util.Debug;
import com.iconmaster.source.xml.ElementXML;
import com.iconmaster.source.xml.XMLHelper;
import java.io.File;
import java.io.FileNotFoundException;
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
		CLA cla = CLAHelper.getArgs(args);
		if (cla.containsKey("d")) {
			Debug.debugMode = true;
		}
		if (cla.containsKey("i")) {
			interactivePrompt();
		}
		if (cla.containsKey("code")) {
			printInput(cla.get("code"));
		}
		if (cla.unmatched.length == 0) {
			return;
		} else {
			cla.put("f",cla.unmatched[0]);
		}
		if (cla.containsKey("f")) {
			String input = cla.get("f");
			File file = new File(input);
			if (!file.canRead()) {
				System.out.println("ERROR: file "+input+" can't be read from!");
				return;
			}
			try {
				input = "";
				Scanner fileScanner = new Scanner(new File(cla.get("f")));
				while (fileScanner.hasNext()){
				   input+="\n"+fileScanner.next();
				}
				
				
			} catch (FileNotFoundException ex) {
				System.out.println("ERROR: file "+input+" can't be read from!");
				return;
			}
			printInput(input);
		}
	}
	
	public static void interactivePrompt() {
		Scanner kbd = new Scanner(System.in);
		while (true) {
			ArrayList<Element> a = null;
			System.out.print("Enter an expression: ");
			printInput(kbd.next());
		}
	}
	
	public static void printInput(String input) {
		try {
			ArrayList<Element> a = Parser.parse(Tokenizer.tokenize(input));
			System.out.println(a);

			Document doc = XMLHelper.blankDoc();
			ElementXML.toXML(doc, XMLHelper.addTag(doc, "parse_result", ""), a);
			System.out.println(XMLHelper.toString(doc));
		} catch (SourceException ex) {
			Logger.getLogger(Source.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
}
