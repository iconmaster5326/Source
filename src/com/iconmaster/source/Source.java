package com.iconmaster.source;

import com.iconmaster.source.SourceInput.VerboseLevel;
import com.iconmaster.source.util.CLAHelper;
import com.iconmaster.source.util.CLAHelper.CLA;
import com.iconmaster.source.util.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
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
		} else {
			input.platformName = ""; //TODO: add default platform
		}
		
		if (cla.containsKey("exts")) {
			input.platformFiles = FileUtils.getAllFiles(new File(cla.get("exts")));
		}
		
		if (cla.containsKey("libs")) {
			input.libFiles = FileUtils.getAllFiles(new File(cla.get("libs")));
		}
		
		if (cla.containsKey("assets")) {
			input.assetsFile = new File(cla.get("assets"));
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
	}
}
