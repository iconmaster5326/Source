package com.iconmaster.source.prototype;

import com.iconmaster.source.compile.DataType;
import com.iconmaster.source.compile.Expression;
import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.util.IDirectable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class Field implements IDirectable {
	protected String name;
	private Element rawType;
	protected Element rawValue;
	protected ArrayList<String> directives = new ArrayList<>();;
	
	protected boolean compiled;
	protected boolean library;
	protected Expression value;
	private DataType type;
	
	public String pkgName;
	
	/**
	 * A map used to store item information for the platform.
	 */
	public HashMap data = new HashMap<>();

	public Field(String name) {
		this(name,null);
	}

	public Field(String name, Element type) {
		this.name = name;
		this.rawType = type;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name+"="+getType());
		if (value!=null) {
			sb.append(". CODE:");
			
			for (Operation op : value) {
				sb.append("\n\t");
				sb.append(op.toString());
			}
		}
		return sb.toString();
	}

	public String getName() {
		return name;
	}
	
	public void setCompiled(Expression code) {
		this.compiled = true;
		this.value = code;
	}

	public Element rawData() {
		return rawValue;
	}

	public boolean isCompiled() {
		return compiled;
	}

	public Expression getValue() {
		return value;
	}
	
	@Override
	public ArrayList<String> getDirectives() {
		return directives;
	}
	
	public boolean isLibrary() {
		return library;
	}

	public Element getRawType() {
		return rawType;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}
	
	public static Field libraryField(String name, TypeDef type) {
		Field f = new Field(name);
		if (type!=null) {
			f.setType(new DataType(type,false));
		}
		f.library = true;
		return f;
	}
	
	public static interface OnCompile {
		public String compile(SourcePackage pkg, boolean isGet, Object... args);
	}
	
	public static interface OnRun {
		public Object run(SourcePackage pkg, boolean isGet, Object... args);
	}
}
