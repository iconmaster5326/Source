package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.link.platform.hppl.HPPLCustomFunctions.CustomFunction;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Directives;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class AssemblyData {
	public static class Frame {
		public Frame parent;
		public HashMap<String,String> inlines = new HashMap<>();

		public Frame(Frame parent) {
			this.parent = parent;
		}
		
		public String getInline(String name) {
			return inlines.containsKey(name) ? inlines.get(name) : (parent==null ? null : parent.getInline(name));
		}
	}
	
	public SourcePackage pkg;
	public boolean minify;
	
	public ArrayList<HPPLFunction> funcs = new ArrayList<>();
	public ArrayList<HPPLField> fields = new ArrayList<>();
	public ArrayList<HPPLVariable> vars = new ArrayList<>();
	
	public ArrayList<Frame> frames = new ArrayList<>();
	
	public AssemblyData(SourcePackage pkg) {
		this.pkg = pkg;
		frames.add(new Frame(null));
		minify = !Directives.has(pkg, "!minify");
	}
	
	public Frame frame() {
		return frames.get(frames.size()-1);
	}
	
	public void pushFrame() {
		frames.add(new Frame(frame()));
	}
	
	public Frame popFrame() {
		return frames.remove(frames.size()-1);
	}
	
	public String getFuncMap(String name) {
		Function rfn = pkg.getFunction(name);
		if (rfn.data.containsKey("compName")) {
			return (String) rfn.data.get("compName");
		}
		for (HPPLFunction fn : funcs) {
			if (fn.fn==rfn) {
				return fn.compileName;
			}
		}
		
		return name;
	}
	
	public String getVarMap(String name) {
		for (HPPLVariable v : vars) {
			if (v.name.equals(name)) {
				return v.compileName;
			}
		}
		for (HPPLField f : fields) {
			if (f.f.getName().equals(name)) {
				return f.compileName;
			}
		}
		return name;
	}
	
	public String getInline(String name) {
		String compName = getVarMap(name);
		String s = frame().getInline(name);
		return s==null ? compName : s;
	}
	
	public void addInline(String name, String code) {
		String compName = getVarMap(name);
		frame().inlines.put(compName, code);
	}
	
	public CustomFunction getFuncAssembler(String name) {
		Function rfn = pkg.getFunction(name);
		return (CustomFunction) rfn.data.get("onAssemble");
	}
}
