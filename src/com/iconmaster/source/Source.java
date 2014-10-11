package com.iconmaster.source;

import com.iconmaster.source.assemble.Assembler;
import com.iconmaster.source.compile.SourceCompiler;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.link.Linker;
import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.prototype.Prototyper;
import com.iconmaster.source.tokenize.Tokenizer;
import com.iconmaster.source.util.CLAHelper;
import com.iconmaster.source.util.CLAHelper.CLA;
import com.iconmaster.source.util.Debug;
import com.iconmaster.source.validate.Validator;
import com.iconmaster.source.xml.ElementXML;
import com.iconmaster.source.xml.XMLHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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
		OutputStream output = System.out;
		if (cla.containsKey("d")) {
			Debug.debugMode = true;
		}
		if (cla.containsKey("i")) {
			interactivePrompt();
		}
		if (cla.containsKey("o")) {
			File f = new File(cla.get("o"));
			try {
				f.createNewFile();
			} catch (IOException ex) {
				Logger.getLogger(Source.class.getName()).log(Level.SEVERE, null, ex);
			}
			try {
				output = new FileOutputStream(f);
			} catch (FileNotFoundException ex) {
				Logger.getLogger(Source.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (cla.containsKey("code")) {
			printInput(cla.get("code"),output);
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
			printInput(input,output);
		}
	}
	
	public static void interactivePrompt() {
		Scanner kbd = new Scanner(System.in);
		while (true) {
			ArrayList<Element> a = null;
			System.out.print("Enter an expression: ");
			printInput(kbd.next(),System.out);
		}
	}
	
	public static void printInput(String input, OutputStream output) {
		try {
			System.out.println("Tokenizing...");
			ArrayList<Element> a = Tokenizer.tokenize(input);
			System.out.println(a);
			System.out.println("Parsing...");
			a = Parser.parse(a);
			System.out.println(a);
			
			Debug.println("The XML product is:");
			Document doc = XMLHelper.blankDoc();
			ElementXML.toXML(doc, XMLHelper.addTag(doc, "parse_result", ""), a);
			Debug.println(XMLHelper.toString(doc));
			
			System.out.println("Validating...");
			ArrayList<SourceException> errs = Validator.validate(a);
			if (!errs.isEmpty()) {
				System.out.println("There were errors detected:");
				for (SourceException err : errs) {
					System.out.print("\t");
					System.out.println(err);
				}
				System.out.println("Compilation could not be completed.");
				return;
			}
			System.out.println("Validation complete. No errors found!");
			System.out.println("Prototyping...");
			Prototyper.PrototypeResult pkg = Prototyper.prototype(a);
			System.out.println(pkg.result);
			if (!pkg.errors.isEmpty()) {
				System.out.println("There were errors detected:");
				for (SourceException err : pkg.errors) {
					System.out.print("\t");
					System.out.println(err);
				}
				System.out.println("Compilation could not be completed.");
				return;
			}
			System.out.println("Prototyping complete. No errors found!");
			System.out.println("Linking...");
			Linker linker = Linker.link("HPPL", pkg.result);
			System.out.println(linker);
			if (!linker.unresolvedImports.isEmpty()) {
				System.out.println("There were unresolved imports:");
				for (String err : linker.unresolvedImports) {
					System.out.print("\t");
					System.out.println(err);
				}
				System.out.println("Compilation could not be completed.");
				return;
			}
			System.out.println("Prototyping complete. No errors found!");
			System.out.println("Compiling...");
			SourceCompiler.compile(linker.pkg);
			System.out.println(linker.pkg);
			System.out.println("Compiling done!");
			System.out.println("Assembling...");
			String outputStr = Assembler.assemble("HPPL", linker.pkg);
			System.out.println("Got the following code:");
			System.out.println(outputStr);
			System.out.println("Done!");
			PrintWriter pw = new PrintWriter(output);
			pw.print(outputStr);
			pw.flush();
			pw.close();
		} catch (SourceException ex) {
			Logger.getLogger(Source.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
}
