package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.link.platform.hppl.HPPLCustomFunctions.CustomFunction;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Directives;
import com.iconmaster.source.util.IDirectable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class AssemblyData implements IDirectable {
	public static class Frame {
		public Frame parent;
		public HashMap<String,String> inlines = new HashMap<>();
		public ArrayList<HPPLVariable> localVars = new ArrayList<>();
		public String blockEnd;
		
		public ArrayList<String> dirs = new ArrayList<>();

		public Frame(Frame parent) {
			this.parent = parent;
		}
		
		public String getInline(String name) {
			return inlines.containsKey(name) ? inlines.get(name) : (parent==null ? null : parent.getInline(name));
		}
		
		public String getLocalVar(String name) {
			for (HPPLVariable v : localVars) {
				if (v.name.equals(name)) {
					return v.compileName;
				}
			}
			return parent==null? name : parent.getLocalVar(name);
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
		String local = frame().getLocalVar(name);
		if (!local.equals(name)) {
			return local;
		}
		for (HPPLField f : fields) {
			if (f.f.getName().equals(name)) {
				return f.compileName;
			}
		}
		for (HPPLVariable v : vars) {
			if (v.name.equals(name)) {
				return v.compileName;
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
	
	public boolean exists(String name) {
		for (HPPLField f : fields) {
			if (f.f.getName().equals(name)) {
				return true;
			}
		}
		for (HPPLVariable v : vars) {
			if (v.name.equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ArrayList<String> getDirectives() {
		ArrayList<String> a = new ArrayList<>();
		a.addAll(pkg.getDirectives());
		Frame f = frame();
		do {
			a.addAll(f.dirs);
			f = f.parent;
		} while (f!=null);
		return a;
	}
}
