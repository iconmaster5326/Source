package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.link.Platform;

/**
 *
 * @author iconmaster
 */
public class PlatformHPPL extends Platform {

	public PlatformHPPL() {
		this.name = "HPPL";
		
		this.registerLibrary(new LibraryCore());
	}
	
}
