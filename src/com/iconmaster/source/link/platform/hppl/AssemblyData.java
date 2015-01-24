package com.iconmaster.source.link.platform.hppl;

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
	
	public ArrayList<Frame> frames;
	
	public AssemblyData(SourcePackage pkg) {
		this.pkg = pkg;
		pushFrame();
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
}
