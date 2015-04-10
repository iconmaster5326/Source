package com.iconmaster.source.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public class FileUtils {
	public static List<File> getAllFiles(File input) {
		ArrayList<File> a = new ArrayList<>();
		
		if (!input.isDirectory()) {
			a.add(input);
			return a;
		}
		
		for (File f : input.listFiles()) {
			a.addAll(getAllFiles(f));
		}
		return a;
	}
}
