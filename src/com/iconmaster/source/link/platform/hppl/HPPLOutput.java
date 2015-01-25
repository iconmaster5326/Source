package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.assemble.AssembledOutput;

/**
 *
 * @author iconmaster
 */
public class HPPLOutput extends AssembledOutput {
	public String output;

	public HPPLOutput(String output) {
		this.output = output;
	}

	@Override
	public String getOutputString() {
		return output;
	}
}
