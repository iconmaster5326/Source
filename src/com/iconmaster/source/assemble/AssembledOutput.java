package com.iconmaster.source.assemble;

import com.iconmaster.source.SourceOptions;
import com.iconmaster.source.exception.SourceException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iconmaster
 */
public abstract class AssembledOutput {
	public ArrayList<SourceException> errs = new ArrayList<>();
	
	public void saveToFile(SourceOptions opts) {
		try {
			(new FileWriter(opts.outputFile)).append(getOutputString()).close();
		} catch (IOException ex) {
			Logger.getLogger(AssembledOutput.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public String getOutputString() {
		return "(no output)";
	}
}
