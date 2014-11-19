package com.iconmaster.source.link;

import com.iconmaster.source.SourceOptions;
import com.iconmaster.source.prototype.Import;
import com.iconmaster.source.prototype.SourcePackage;

/**
 *
 * @author iconmaster
 */
public interface ImportHandler {
	public void handle(SourcePackage pkg, SourceOptions op, Import imp);
}
