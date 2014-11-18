package com.iconmaster.source;

import com.iconmaster.source.assemble.Assembler;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.link.Linker;
import com.iconmaster.source.link.platform.PlatformLoader;
import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.prototype.Prototyper;
import com.iconmaster.source.tokenize.Tokenizer;
import com.iconmaster.source.util.CLAHelper;
import com.iconmaster.source.util.CLAHelper.CLA;
import com.iconmaster.source.validate.Validator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author iconmaster
 */
public class Source {

	public static void main(String[] args) {
		CLA cla = CLAHelper.getArgs(args);
		SourceOptions op = new SourceOptions(null, "HPPL", true);
		if (cla.containsKey("p")) {
			op.platform = cla.get("p");
		}
		if (cla.containsKey("o")) {
			op.outputFile = new File(cla.get("o"));
		}
		if (cla.containsKey("libs")) {
			File f = new File(cla.get("libs"));
			for (File child : f.listFiles((File dir, String name) -> name.endsWith(".jar"))) {
				try {
					PlatformLoader.loadPlatform(child);
				} catch (Exception ex) {
					Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "error in loading library "+f, ex);
				}
			}
		}
		if (cla.containsKey("run")) {
			op.compile = false;
		}
		if (cla.containsKey("src")) {
			op.libs = new File(cla.get("src"));
		}
		if (cla.containsKey("assets")) {
			op.assets = new File(cla.get("assets"));
		}
		
		for (String f : cla.unmatched) {
			System.out.println("Compiling "+f+"...");
			try {
				op.input = new BufferedReader(new FileReader(f)).lines().collect(Collectors.joining("\n"));
			} catch (IOException ex) {
				Logger.getLogger(Source.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		if (op.input == null) {
			System.out.println("This is Source, written by iconmaster.");
			System.out.println("Give the input file on the command line.");
			System.out.println("Some options are:");
			System.out.println("\tp:\t\tThe platform to compile to");
			System.out.println("\to:\t\tThe output file");
			System.out.println("\tlibs:\t\tThe external platform folder");
			System.out.println("\tsrc:\t\tThe Source libraries folder");
			System.out.println("\tassets:\t\tThe assets folder");
			System.out.println("\trun:\t\tInclude to run program (i.e. SourceBox)");
			System.out.println("\tv:\t\tVerbose compile mode switch");
			System.out.println("\tshow:\t\tShow compiled output");
			System.exit(0);
		}
		
		SourceOutput so = execute(op);
		
		if (so.dets.isEmpty()) {
			if (cla.containsKey("v")) {
				System.out.println(so.operationLog);
			} else if (cla.containsKey("show")) {
				System.out.println();
				System.out.println(so.output);
			}
			System.out.print("Compiled sucsessfully");
			if (op.outputFile!=null) {
				System.out.println(" into "+op.outputFile+".");
			} else {
				System.out.println(".");
			}
		} else {
			if (cla.containsKey("v")) {
				System.out.println(so.operationLog);
			}
			System.out.println("There were errors in compiling:");
			System.out.println(so.errMsgs.replace("\n", "\n\t"));
		}
	}
	
	public static SourceOutput execute(SourceOptions opts) {
		SourceOutput so = new SourceOutput();
		so.operationLog = "";
		try {
			ArrayList<ErrorDetails> dets = new ArrayList<>();
			ArrayList<SourceException> errs = new ArrayList<>();

			so.operationLog += "Tokenizing...\n";
			ArrayList<Element> a = null;
			try {
				a = Tokenizer.tokenize(opts.input);
			} catch (Exception ex) {
				if (ex instanceof SourceException) {
					errs.add((SourceException) ex);
				} else {
					Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "Source error in tokenization", ex);
				}
				dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Tokenization"));
			}
			so.operationLog += a + "\n";
			so.operationLog += "Parsing...\n";
			try {
				a = Parser.parse(a);
			} catch (Exception ex) {
				if (ex instanceof SourceException) {
					errs.add((SourceException) ex);
				} else {
					Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "Source error in parsing", ex);
				}
				dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Parsing"));
			}
			so.operationLog += "Validating...\n";
			try {
				ArrayList<SourceException> errs2 = Validator.validate(a);
				errs.addAll(errs2);
				for (SourceException ex : errs2) {
					dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Validation"));
				}
			} catch (Exception ex) {
				Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "Source error in validation", ex);
				dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Validation"));
			}
			so.operationLog += "Prototyping...\n";
			Prototyper.PrototypeResult res = null;
			try {
				res = Prototyper.prototype(a);
				errs.addAll(res.errors);
				for (SourceException ex : res.errors) {
					dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Prototyping"));
				}
				so.operationLog += res.result + "\n";
				res.result.options = opts;
			} catch (Exception ex) {
				Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "Source error in prototyping", ex);
				dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Prototyping"));
			}
			so.operationLog += "Linking...\n";
			Linker linker = null;
			try {
				linker = Linker.link(opts.platform, res.result);
				
				errs.addAll(linker.errs);
				for (SourceException ex :  linker.errs) {
					dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Linking"));
				}
			} catch (Exception ex) {
				Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "Source error in linking", ex);
				dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Linking"));
			}

			so.operationLog += linker + "\n";
			so.operationLog += "Compiling...\n";
			try {
				linker.compile();
				
				errs.addAll(linker.errs);
				for (SourceException ex :  linker.errs) {
					dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Compiling"));
				}
			} catch (Exception ex) {
				Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "Source error in compiling", ex);
				dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Compiling"));
			}

			so.operationLog += linker + "\n";
			if (opts.compile) {
				so.operationLog += "Assembling...\n";
				try {
					so.output = Assembler.assemble(opts.platform, linker.outputPackage);
					
					if (opts.outputFile!=null) {
						(new FileWriter(opts.outputFile)).append(so.output).close();
					}
				} catch (Exception ex) {
					Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "Source error in assembly", ex);
					dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Assembling"));
				}
				so.operationLog += so.output + "\n";
			} else {
				so.operationLog += "Running...\n";
				try {
					Assembler.run(opts.platform, linker.outputPackage);
				} catch (Exception ex) {
					Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "Source error in running", ex);
					dets.add(new ErrorDetails(ex.getClass().getSimpleName(), ex.getMessage(), "Running"));
				}
			}
			so.operationLog += "Done!\n";
			so.errs = errs;
			so.dets = dets;
			so.errMsgs = "";
			for (ErrorDetails det : dets) {
				so.errMsgs+=det.errorType+" in "+det.phase+": "+det.errorMsg+"\n";
			}
			if (!dets.isEmpty()) {
				so.operationLog += "There were errors detected:\n\t";
				so.operationLog += so.errMsgs.replace("\n","\n\t") + "\n";
			}
		} catch (Exception ex) {
			Logger.getLogger(Source.class.getName()).log(Level.SEVERE, "general Source error", ex);
		}
		
		return so;
	}
}
