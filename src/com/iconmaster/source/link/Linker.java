package com.iconmaster.source.link;

import com.iconmaster.source.compile.SourceCompiler;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.exception.SourceImportException;
import com.iconmaster.source.link.platform.hppl.PlatformHPPL;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.Import;
import com.iconmaster.source.prototype.SourcePackage;
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
		registerPlatform(new PlatformHPPL());
	}
	
	public static void registerPlatform(Platform p) {
		platforms.put(p.name,p);
	}
	
	public LinkGraph links = new LinkGraph();
	public Platform platform;
	public SourcePackage inputPackage;
	public SourcePackage outputPackage = new SourcePackage();
	public ArrayList<SourceException> errs = new ArrayList<>();
	
	public Linker(String platform, SourcePackage inputPackage) {
		this.platform = platforms.get(platform);
		this.inputPackage = inputPackage;
	}
	
	public void resolveLinks() {
		resolveLinks(this.inputPackage);
	}
	
	public void resolveLinks(SourcePackage pkg) {
		if (links.getImport(pkg.getName())==null) {
			Import nimp = new Import(pkg.getName(), null, false, null);
			nimp.pkg = pkg;
			nimp.resolved = true;
			links.addNode(nimp);
		}
		
		for (Import imp : pkg.getImports()) {
			if (links.getImport(imp.name)==null) {
				links.addNode(imp);
			} else {
				imp = links.getImport(imp.name);
			}
			
			links.link(pkg.getName(), imp.name);
			
			if (imp.pkg==null) {
				if (platform.pkgs.containsKey(imp.name)) {
					imp.pkg = platform.pkgs.get(imp.name);
				} else {
					errs.add(new SourceImportException(imp.range, "Unresolved import "+imp.name, imp.name));
				}
			}
			
			if (imp.pkg!=null && !imp.resolved) {
				resolveLinks(imp.pkg);
			}
			
			imp.resolved = true;
		}
	}
	
	public void compile() {
		errs.clear();
		
		int n = 0;
		int max = links.getHighestDep();
		System.out.println("* MAX "+max);
		while (n<=max) {
			ArrayList<Import> imps = links.getLinksWithDepsOf(n);
			System.out.println("* N "+n);
			System.out.println("* IMPS "+imps);
			if (imps.isEmpty()) {
				n++;
			} else {
				for (Import imp : (ArrayList<Import>) imps.clone()) {
					SourcePackage compPkg = new SourcePackage();
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
					
					System.out.println("* COMP "+imp);
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
		for (Import imp : pkg2.getImports()) {
			if (!has.contains(imp.name)) {
				System.out.println("* CADD "+imp);
				Import imp2 = links.getImport(imp.name);
				pkg.addContents(imp2.pkg);
				has.add(imp2.name);
				addNeeded(pkg, imp2.pkg, has);
			}
		}
	}
	
	public static Linker link(String plat,SourcePackage pkg) {
		Linker linker = new Linker(plat,pkg);
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
