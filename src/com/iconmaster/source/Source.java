package com.iconmaster.source;

import com.iconmaster.source.SourceInput.VerboseLevel;
import com.iconmaster.source.exception.SourceError;
import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.parse.Token;
import com.iconmaster.source.parse.Tokenizer;
import com.iconmaster.source.prototype.Prototyper;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.CLAHelper;
import com.iconmaster.source.util.CLAHelper.CLA;
import com.iconmaster.source.util.FileUtils;
import com.iconmaster.source.util.Result;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author iconmaster
 */
public class Source {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		CLA cla = CLAHelper.getArgs(args);
		
		if (cla.unmatched.length==0) {
			System.out.println("Welcome to the Source compiler, version @VERSION@.");
			System.out.println();
			System.out.println("Usage: source filename");
			System.out.println("Possible flags include:");
			System.out.println("\t-v setting: change verbose output setting [default: 2]");
			System.out.println("\t-p platform: change compilation target [default: ]"); //TODO: default lang?
			System.out.println("\t-exts file: speciify Source compiler extensions folder/file [default: null]");
			System.out.println("\t-libs file: speciify Source library folder/file [default: null]");
			System.out.println("\t-assets file: speciify assets folder/file [default: null]");
			System.out.println("\t-o file: speciify output folder/file [default: null]");
			System.out.println("\t-show: Displays the output to console if specified");
			return;
		}
		
		SourceInput input = new SourceInput();
		input.inputFile = new File(cla.unmatched[0]);
		
		if (cla.containsKey("v")) {
			VerboseLevel lvl = null;
			
			for (VerboseLevel i : VerboseLevel.values()) {
				if (i.toString().startsWith(cla.get("v"))) {
					lvl = i;
				}
				if (String.valueOf(i.ordinal()).equals(cla.get("v"))) {
					lvl = i;
				}
			}
			
			if (lvl==null) {
				System.out.println("WARNING: unknown verbose level specified, defaulting to default verbosity");
				System.out.println();
			} else {
				input.verbose = lvl;
			}
		}
		
		if (cla.containsKey("p")) {
			input.platformName = cla.get("p");
		}
		
		if (cla.containsKey("exts")) {
			List<File> platformFiles = FileUtils.getAllFiles(new File(cla.get("exts")));
		}
		
		if (cla.containsKey("libs")) {
			List<File> libFiles = FileUtils.getAllFiles(new File(cla.get("libs")));
			for (File f : libFiles) {
				try {
					input.println(VerboseLevel.VERBOSE, "Compiling library "+f+"...");
					Scanner in = new Scanner(f);
					String code = "";
					while (in.hasNext()) {
						code += in.nextLine()+"\n";
					}
					
					SourceInput si = new SourceInput();
					si.verbose = VerboseLevel.NONE;
					si.inputFile = f;
					si.code = code;
					si.platformName = input.platformName;
					si.assetsFile = input.assetsFile;
					si.assemble = false;
					
					SourceOutput so = getPrototype(si);
					
					if (so.failed) {
						input.println(VerboseLevel.VERBOSE, "Error in compiling "+f+"!");
						printError(so);
						return;
					} else {
						input.println(VerboseLevel.VERBOSE, "Compiled "+f+" succsessfully.");
						input.libraries.add(so.pkg);
					}
				} catch (FileNotFoundException ex) {}
			}
		}
		
		if (cla.containsKey("assets")) {
			input.assetsFile = new File(cla.get("assets"));
		}
		
		if (cla.containsKey("o")) {
			input.outputFile = new File(cla.get("o"));
		}
		
		try {
			input.println(VerboseLevel.VERBOSE,"Reading input file "+input.inputFile+"...");
			Scanner in = new Scanner(input.inputFile);
			input.code = "";
			while (in.hasNext()) {
				input.code += in.nextLine()+"\n";
			}
		} catch (FileNotFoundException ex) {
			input.println(VerboseLevel.MINIMAL,"ERROR: File "+input.inputFile+" not found!");
			return;
		}
		
		input.println(VerboseLevel.VERBOSE,"Got the following input:");
		input.println(VerboseLevel.VERBOSE,input.code);
		
		input.println(VerboseLevel.MINIMAL,"Compiling "+input.inputFile+"...");
		SourceOutput so = compile(input);
		
		if (so.failed) {
			printError(so);
		} else {
			input.println(VerboseLevel.MINIMAL,"Compilation completed sucsessfully!");
			if (input.outputFile!=null) {
				input.println(VerboseLevel.MINIMAL,"Wrote output to "+input.outputFile+".");
			}
			if (cla.containsKey("show")) {
				input.println(VerboseLevel.NONE);
				input.println(VerboseLevel.NONE, "Got the following output:");
				input.println(VerboseLevel.NONE, so.output);
			}
		}
	}
	
	public static SourceOutput compile(SourceInput input) {
		return getPrototype(input);
	}
	
	public static SourceOutput getPrototype(SourceInput input) {
		SourceOutput so = new SourceOutput();
		
		try {
			input.println(VerboseLevel.VERBOSE, "Tokenizing...");
			Result<List<Token>> res1 = Tokenizer.tokenize(input.code);
			if (res1.failed) {
				so.addErrors(res1.errors);
				so.failed = true;
				return so;
			}
			input.println(VerboseLevel.DEBUG, "Got "+res1.item);

			input.println(VerboseLevel.VERBOSE, "Parsing...");
			Result<Token> res2 = Parser.parse(res1.item);
			if (res2.failed) {
				so.addErrors(res2.errors);
				so.failed = true;
				return so;
			}
			input.println(VerboseLevel.DEBUG, "Got "+res2.item);

			input.println(VerboseLevel.VERBOSE, "Prototyping...");
			Result<SourcePackage> res3 = Prototyper.prototype(res2.item);
			if (res3.failed) {
				so.addErrors(res3.errors);
				so.failed = true;
				return so;
			}
			input.println(VerboseLevel.DEBUG, "Got "+res3.item);
			
			so.pkg = res3.item;
			
		} catch (Exception ex) {
			so.exceptions.add(ex);
			so.failed = true;
			return so;
		}
		
		return so;
	}
	
	public static void printError(SourceOutput so) {
		if (!so.exceptions.isEmpty()) {
			so.input.println(VerboseLevel.MINIMAL,"The following internal errors occured:");
			for (Exception ex : so.exceptions) {
				so.input.printStackTrace(VerboseLevel.MINIMAL, ex);
				so.input.println(VerboseLevel.MINIMAL);
			}
		}
		if (!so.errors.isEmpty()) {
			so.input.println(VerboseLevel.MINIMAL,"The following errors occured:");
			for (SourceError ex : so.errors) {
				so.input.println(VerboseLevel.MINIMAL, ex);
			}
		}
		so.input.println(VerboseLevel.MINIMAL,"Compilation could not be completed.");
	}
}
