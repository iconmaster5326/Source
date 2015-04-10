package com.iconmaster.source;

import com.iconmaster.source.link.Platform;
import com.iconmaster.source.prototype.SourcePackage;
import java.io.File;
import java.util.ArrayList;
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
	
	public File inputFile;
	public File outputFile;
	public List<File> libFiles;
	public File assetsFile;
	public String code;
	public String platformName = ""; //TODO: set default platform
	public boolean assemble = true;
	public VerboseLevel verbose = VerboseLevel.DEFAULT;
	
	public Platform platform;
	public List<SourcePackage> libraries = new ArrayList<>();
	
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
