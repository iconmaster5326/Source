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
		String platform = "HPPL";
		if (cla.containsKey("p")) {
			platform = cla.get("p");
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
			SourceOutput so = execute(new SourceOptions(input, platform, !cla.containsKey("run")));
			if (output!=null) {
				try (PrintWriter pw = new PrintWriter(output)) {
					pw.print(so.output);
					pw.flush();
				}
			}
		}
	}
	
	@Deprecated
	public static SourceOutput compile(String input, String platform, OutputStream output) {
		return execute(input, platform, output, false);
	}
	
	@Deprecated
	public static SourceOutput run(String input, String platform, OutputStream output) {
		return execute(input, platform, output, true);
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
		
		System.out.println(so.operationLog);
		return so;
	}
	
	@Deprecated
	public static SourceOutput execute(String input, String platform, OutputStream output, boolean run) {
		return execute(new SourceOptions(input, platform, !run).setStreams(output, System.in, System.err));
	}
}
