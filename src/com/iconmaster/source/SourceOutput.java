package com.iconmaster.source;

import com.iconmaster.source.exception.SourceError;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public class SourceOutput {
	public SourceInput input;
	public boolean failed;
	public List<SourceError> errors = new ArrayList<>();
	public List<Exception> exceptions = new ArrayList<>();
	public SourcePackage pkg;
	public Object output;
	
	public void addErrors(SourceError... errs) {
		for (SourceError err : errs) {
			errors.add(err);
		}
	}
}
