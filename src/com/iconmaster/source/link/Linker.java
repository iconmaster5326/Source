package com.iconmaster.source.link;

import com.iconmaster.source.Source;
import com.iconmaster.source.SourceOptions;
import com.iconmaster.source.compile.SourceCompiler;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.exception.SourceImportException;
import com.iconmaster.source.link.platform.test.PlatformTest;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.Import;
import com.iconmaster.source.prototype.ImportAlias;
import com.iconmaster.source.prototype.SourcePackage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author iconmaster
 */
public class Linker {
	public static HashMap<String,Platform> platforms = new HashMap<>();
	
	static {
		//registerPlatform(new PlatformHPPL());
		registerPlatform(new PlatformTest());
	}
	
	public static void registerPlatform(Platform p) {
		platforms.put(p.name,p);
	}
	
	public LinkGraph links = new LinkGraph();
	public Platform platform;
	public SourcePackage inputPackage;
	public SourcePackage outputPackage = new SourcePackage();
	public ArrayList<SourceException> errs = new ArrayList<>();
	public SourceOptions op;
	public HashMap<String,SourcePackage> userLibs = new HashMap<>();
	
	public Linker(String platform, SourcePackage inputPackage, SourceOptions op) {
		this.platform = platforms.get(platform);
		this.inputPackage = inputPackage;
		this.op = op;
	}
	
	public void loadUserLibs() {
		if (op.libs!=null) {
			for (File child : op.libs.listFiles((File dir, String name) -> name.endsWith(".src"))) {
				SourcePackage userLib = Source.prototypeFile(child, errs);
				userLibs.put(userLib.getName(), userLib);
			}
		}
	}
	
	public void resolveLinks() {
		resolveLinks(this.inputPackage);
	}
	
	public void resolveLinks(SourcePackage pkg) {
		//System.out.println("* RES "+pkg.getName());
		if (links.getImport(pkg.getName())==null) {
			//System.out.println("* NIMP "+pkg.getName());
			Import nimp = new Import(pkg.getName(), null, false, null);
			nimp.pkg = pkg;
			nimp.resolved = true;
			links.addNode(nimp);
		}
		
		for (Import imp : pkg.getImports()) {
			//System.out.println("* IMP "+imp);
			if (links.getImport(imp.name)==null) {
				links.addNode(imp);
			} else {
				imp = links.getImport(imp.name);
			}
			
			links.link(pkg.getName(), imp.name);
			
			if (imp.pkg==null) {
				if (platform.pkgs.containsKey(imp.name)) {
					imp.pkg = platform.pkgs.get(imp.name);
				} else if (userLibs.containsKey(imp.name)) {
					imp.pkg = userLibs.get(imp.name);
				} else if (!imp.isFile) {
					errs.add(new SourceImportException(imp.range, "Unresolved import "+imp.name, imp.name));
				} else {
					if (imp.name.endsWith(".src")) {
						if (op.assets==null) {
							errs.add(new SourceImportException(imp.range, "No assets directory specified to get "+imp.name, imp.name));
						} else {
							File f = new File(op.assets,imp.name);
							if (!f.exists()) {
								errs.add(new SourceImportException(imp.range, "File "+imp.name+" not found", imp.name));
							} else {
								imp.pkg = Source.prototypeFile(f, errs);
							}
						}
					} else if (platform.importHandlers.containsKey(imp.name.substring(imp.name.lastIndexOf(".")+1))) {
						platform.importHandlers.get(imp.name.substring(imp.name.lastIndexOf(".")+1)).handle(pkg, op, imp);
					} else {
						errs.add(new SourceImportException(imp.range, "File "+imp.name+" not supported by platform", imp.name));
					}
				}
				
				if (imp.alias!=null && imp.pkg!=null) {
					pkg.getAliases().add(new ImportAlias(imp.pkg.getName(), imp.alias));
				}
			}
			
			imp.resolved = true;
			
			if (imp.pkg!=null && !imp.resolved) {
				//System.out.println("* REC "+imp);
				resolveLinks(imp.pkg);
			}
		}
	}
	
	public void compile() {
		errs.clear();
		
		int n = 0;
		int max = links.getHighestDep();
		//System.out.println("* MAX "+max);
		while (n<=max) {
			ArrayList<Import> imps = links.getLinksWithDepsOf(n);
			//System.out.println("* N "+n);
			//System.out.println("* IMPS "+imps);
			if (imps.isEmpty()) {
				n++;
			} else {
				for (Import imp : (ArrayList<Import>) imps.clone()) {
					SourcePackage compPkg = new SourcePackage();
					
					if (imp.pkg!=null) {
						compPkg.addContents(imp.pkg);
						compPkg.setName(imp.pkg.getName());

						HashSet<String> has = new HashSet<>();
						has.add(imp.name);

						for (Import imp2 : imps) {
							if (imp2!=imp) {
								if (imp2.pkg!=null) {
									if (!has.contains(imp2.name)) {
										compPkg.addContents(imp2.pkg);
										has.add(imp2.name);
									}
								}
							}
						}

						addNeeded(compPkg,compPkg,has);

						compPkg.addContents(platform.pkgs.get("core"));

						errs.addAll(SourceCompiler.compile(compPkg));
					}
					
					//System.out.println("* COMP "+imp);
					imp.compiled = true;
					n = 0;
				}
			}
		}
		
		outputPackage.setName(inputPackage.getName());
		for (Import imp : links.getAllLinks()) {
			outputPackage.addContents(imp.pkg);
		}
		outputPackage.addContents(platform.pkgs.get("core"));
		
		giveCompileNames();
	}
	
	public void addNeeded(SourcePackage pkg, SourcePackage pkg2, HashSet<String> has) {
		for (Import imp : (ArrayList<Import>) pkg2.getImports().clone()) {
			if (imp.pkg!=null && !has.contains(imp.name)) {
				//System.out.println("* CADD "+imp);
				Import imp2 = links.getImport(imp.name);
				pkg.addContents(imp2.pkg);
				has.add(imp2.name);
				addNeeded(pkg, imp2.pkg, has);
			}
		}
	}
	
	public static Linker link(String plat,SourcePackage pkg, SourceOptions op) {
		Linker linker = new Linker(plat,pkg,op);
		linker.loadUserLibs();
		linker.resolveLinks();
		return linker;
	}
	
	public void giveCompileNames() {
		for (Function fn : outputPackage.getFunctions()) {
			if (fn.compileName==null) {
				fn.compileName = platform.getCompileName(outputPackage, fn, fn.getName());
			}
		}
		for (Field fn : outputPackage.getFields()) {
			if (fn.compileName==null) {
				fn.compileName = platform.getCompileName(outputPackage, fn, fn.getName());
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Linker (platform ");
		sb.append(platform.name).append("):\n\t");
		sb.append(outputPackage.toString().replace("\n", "\n\t"));
		return sb.toString();
	}
}
