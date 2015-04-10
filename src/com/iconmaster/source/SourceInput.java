package com.iconmaster.source;

import java.io.File;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public class SourceInput {
	public static enum VerboseLevel {
		NONE,MINIMAL,DEFAULT,VERBOSE,DEBUG;
		
		public boolean shouldPrint(VerboseLevel level) {
			return this.compareTo(level)>=0;
		}
	}
	
	File inputFile;
	File outputFile;
	List<File> libFiles;
	File assetsFile;
	List<File> platformFiles;
	String code;
	String platformName;
	boolean assemble = true;
	VerboseLevel verbose = VerboseLevel.DEFAULT;
	
	public void println(VerboseLevel level) {
		if (verbose.shouldPrint(level)) {
			System.out.println();
		}
	}
	
	public void println(VerboseLevel level, Object str) {
		if (verbose.shouldPrint(level)) {
			System.out.println(str);
		}
	}
	
	public void print(VerboseLevel level, Object str) {
		if (verbose.shouldPrint(level)) {
			System.out.print(str);
		}
	}
	
	public void printStackTrace(VerboseLevel level, Exception ex) {
		if (verbose.shouldPrint(level)) {
			ex.printStackTrace();
		}
	}
}
