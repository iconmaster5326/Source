package com.iconmaster.source;

import com.iconmaster.source.assemble.AssembledOutput;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class SourceOutput {
	public AssembledOutput output;
	public ArrayList<SourceException> errs = new ArrayList<>();
	public ArrayList<ErrorDetails> dets = new ArrayList<>();
	public String errMsgs;
	public String operationLog;
	public SourcePackage pkg;
}
