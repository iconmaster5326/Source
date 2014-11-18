package com.iconmaster.source;

import com.iconmaster.source.exception.SourceException;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class SourceOutput {
	public String output;
	public ArrayList<SourceException> errs = new ArrayList<>();
	public ArrayList<ErrorDetails> dets = new ArrayList<>();
	public String errMsgs;
	public String operationLog;
}
