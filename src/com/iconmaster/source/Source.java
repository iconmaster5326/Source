package com.iconmaster.source;

import com.iconmaster.source.SourceInput.VerboseLevel;
import com.iconmaster.source.util.CLAHelper;
import com.iconmaster.source.util.CLAHelper.CLA;
import java.io.File;

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
			System.out.println("\t-v setting: change verbose setting");
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
		
		
	}

}
