package com.iconmaster.source;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author iconmaster
 */
public class SourceOptions {
	public String input;
	public String platform;
	public boolean compile;
	
	public File assets;
	public File libs;
	public File outputFile;
	
	public OutputStream sout = System.out;
	public InputStream sin = System.in;
	public OutputStream serr = System.err;

	public SourceOptions(String input, String platform, boolean compile) {
		this.input = input;
		this.platform = platform;
		this.compile = compile;
	}

	public SourceOptions setFiles(File assets, File libs, File outputFile) {
		this.assets = assets;
		this.libs = libs;
		this.outputFile = outputFile;
		return this;
	}
	
	public SourceOptions setStreams(OutputStream sout, InputStream sin, OutputStream serr) {
		this.sout = sout;
		this.sin = sin;
		this.serr = serr;
		return this;
	}
}
