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

/**
 *
 * @author iconmaster
 */
public class Source {

	public static void main(String[] args) {
		CLA cla = CLAHelper.getArgs(args);
		OutputStream output = null;
		if (cla.containsKey("d")) {
			Debug.debugMode = true;
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
				   input+="\n"+fileScanner.nextLine();
				}
				
				
			} catch (FileNotFoundException ex) {
				System.out.println("ERROR: file "+input+" can't be read from!");
				return;
			}
			String platform = "HPPL";
			if (cla.containsKey("p")) {
				platform = cla.get("p");
			}
			SourceOutput so = compile(input,platform,System.out);
			if (output!=null) {
				try (PrintWriter pw = new PrintWriter(output)) {
					pw.print(so.output);
					pw.flush();
				}
			}
		}
	}
	
	public static SourceOutput compile(String input, String platform, OutputStream output) {
		PrintWriter out = new PrintWriter(output);
		ArrayList<ErrorDetails> dets = new ArrayList<>();
		ArrayList<SourceException> errs = new ArrayList<>();
		SourceOutput so = new SourceOutput();
		out.println("Tokenizing...");
		ArrayList<Element> a = null;
		try {
			a = Tokenizer.tokenize(input);
		} catch (Exception ex) {
			if (ex instanceof SourceException) {
				errs.add((SourceException) ex);
			}
			dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Tokenization"));
		}
		out.println(a);
		out.println("Parsing...");
		try {
			a = Parser.parse(a);
		} catch (Exception ex) {
			if (ex instanceof SourceException) {
				errs.add((SourceException) ex);
			}
			dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Parsing"));
		}
		out.println("Validating...");
		try {
			ArrayList<SourceException> errs2 = Validator.validate(a);
			errs.addAll(errs2);
			for (SourceException ex : errs2) {
				dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Validation"));
			}
		} catch (Exception ex) {
			dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Validation"));
		}
		out.println("Prototyping...");
		Prototyper.PrototypeResult res = null;
		try {
			res = Prototyper.prototype(a);
			errs.addAll(res.errors);
			for (SourceException ex : res.errors) {
				dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Prototyping"));
			}
			out.println(res.result);
		} catch (Exception ex) {
			dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Prototyping"));
		}
		out.println("Linking...");
		Linker linker = null;
		try {
			linker = Linker.link(platform, res.result);
		} catch (Exception ex) {
			dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Linking"));
		}
		out.println(linker);
		out.println("Compiling...");
		try {
			ArrayList<SourceException> errs2 = SourceCompiler.compile(linker.pkg);
			errs.addAll(errs2);
			for (SourceException ex : errs2) {
				dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Compiling"));
			}
		} catch (Exception ex) {
			dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Compiling"));
		}
		out.println(linker);
		out.println("Assembling...");
		try {
			so.output = Assembler.assemble(platform, linker.pkg);
		} catch (Exception ex) {
			dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Assembling"));
		}
		out.println(so.output);
		out.println("Done!");
		so.errs = errs;
		so.dets = dets;
		so.errMsgs = "";
		for (ErrorDetails det : dets) {
			so.errMsgs+=det.errorType+" in "+det.phase+": "+det.errorMsg+"\n";
		}
		if (!dets.isEmpty()) {
			out.print("There were errors detected:\n\t");
			out.println(so.errMsgs.replace("\n","\n\t"));
		}
		out.flush();
		out.close();
		return so;
	}
	
}
