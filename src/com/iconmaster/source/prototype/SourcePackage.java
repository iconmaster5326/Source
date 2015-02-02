package com.iconmaster.source.prototype;

import com.iconmaster.source.compile.DataType;
import com.iconmaster.source.compile.NameProvider;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.tokenize.TokenRule;
import com.iconmaster.source.util.Directives;
import com.iconmaster.source.util.IDirectable;
import com.iconmaster.source.util.StringUtils;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class SourcePackage implements IDirectable {	
	protected String name;
	protected ArrayList<Field> fields = new ArrayList<>();
	protected ArrayList<Function> functions = new ArrayList<>();
	protected ArrayList<Import> imports = new ArrayList<>();
	protected ArrayList<TypeDef> types = new ArrayList<>();
	protected ArrayList<Iterator> iters = new ArrayList<>();
	protected ArrayList<CustomType> customTypes = new ArrayList<>();
	private ArrayList<ImportAlias> aliases = new ArrayList<>();
	
	public NameProvider nameProvider = NameProvider.instance;
	
	private ArrayList<String> directives = new ArrayList<>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PACKAGE ");
		sb.append(name).append(":\n\tIMPORTS:");
		for (Import imp : imports) {
			sb.append("\n\t\t");
			sb.append(imp);
		}
		sb.append("\n\tTYPES:");
		for (TypeDef t : types) {
			sb.append("\n\t\t");
			sb.append(t.toString().replace("\n", "\n\t"));
		}
		sb.append("\n\tFIELDS:");
		for (Field field : fields) {
			sb.append("\n\t\t");
			sb.append(field.toString().replace("\n", "\n\t"));
		}
		sb.append("\n\tFUNCTIONS:");
		for (Function func : functions) {
			sb.append("\n\t\t");
			sb.append(func.toString().replace("\n", "\n\t"));
		}
		sb.append("\n\tITERATORS:");
		for (Iterator iter : iters) {
			sb.append("\n\t\t");
			sb.append(iter.toString().replace("\n", "\n\t"));
		}
		return sb.toString();
	}
	
	public boolean shouldAdd(ArrayList<String> item) {
		ArrayList<String> defs = Directives.getValues(this, "def");
		ArrayList<String> undefs = Directives.getValues(this, "undef");
		ArrayList<String> ifdefs = Directives.getValues(item, "ifdef");
		ArrayList<String> ifndefs = Directives.getValues(item, "ifndef");
		
		for (String s : undefs) { //hooray for the lazy remove!
			defs.remove(s);
		}
		
		for (String s : ifdefs) {
			if (!defs.contains(s)) {
				return false;
			}
		}
		
		for (String s : ifndefs) {
			if (defs.contains(s)) {
				return false;
			}
		}
		
		return true;
	}
	
	public ArrayList<SourceException> parse(ArrayList<Element> a) {
		ArrayList<SourceException> errors = new ArrayList<>();
		for (Element e : a) {
			if (!shouldAdd(e.directives)) {
				continue;
			}
			switch ((Rule)e.type) {
				case PACKAGE:
					if (name!=null) {
						errors.add(new SourceException(e.range,"Cannot have multiple package declarations"));
					} else {
						name = StringUtils.nameString((Element) e.args[0]);
						if (name == null) {
							errors.add(new SourceException(e.range,"Invalid package name"));
						}
					}
					break;
				case IMPORT:
					String imp = StringUtils.nameString((Element) e.args[0]);
					if (imp == null) {
						errors.add(new SourceException(e.range,"Invalid import package"));
					} else {
						imports.add(new Import(imp, StringUtils.nameString(((Element)e.args[0]).dataType), ((Element)e.args[0]).type==TokenRule.STRING, e.range));
					}
					break;
				case FIELD_ASN:
					int i=0;
					ArrayList<Element> vals = ((ArrayList<Element>) e.args[1]);
					for (Element e2 : (ArrayList<Element>) e.args[0]) {
						Field var = new Field((String)e2.args[0], e2.dataType);
						if (i < vals.size()) {
							var.rawValue = vals.get(i);
						}
						var.getDirectives().addAll(e.directives);
						var.getDirectives().addAll(directives);
						addField(var);
						i++;
					}
					break;
				case FIELD:
					for (Element e2 : (ArrayList<Element>) e.args[0]) {
						Field var = new Field((String)e2.args[0], e2.dataType);
						var.getDirectives().addAll(e.directives);
						var.getDirectives().addAll(directives);
						addField(var);
					}
					break;
				case ITERATOR:
				case FUNC:
					String fname = (String) e.args[0];
					ArrayList<Field> args = new ArrayList<>();
					Element rets = e.dataType;
					for (Element e2 : (ArrayList<Element>) e.args[1]) {
						if (e2.args[0] instanceof ArrayList) {
							for (Element e3 : (ArrayList<Element>) e2.args[0]) {
								args.add(new Field((String)e3.args[0], e3.dataType));
							}
						} else {
							args.add(new Field((String)e2.args[0], e2.dataType));
						}
					}
					
					Function fn;
					if (e.type==Rule.ITERATOR) {
						fn = new Iterator(fname, args,rets);
					} else {
						fn = new Function(fname,args,rets);
					}
					
					fn.getDirectives().addAll(e.directives);
					fn.getDirectives().addAll(directives);
					fn.rawCode = (ArrayList<Element>) e.args[2];
					
					if (e.args[3]!=null) {
						ArrayList<Element> es = (ArrayList<Element>) e.args[3];
						if (es.size()>0 && es.get(0).type==Rule.TUPLE) {
							es = (ArrayList<Element>) es.get(0).args[0];
						}
						fn.rawParams = new ArrayList<>();
						for (Element e2 : es) {
							Field param = new Field((String) e2.args[0], e2.dataType);
							fn.rawParams.add(param);
						}
					}
					
					if (e.type==Rule.ITERATOR) {
						addIterator((Iterator)fn);
					} else {
						addFunction(fn);
					}
					
					if (Directives.has(fn, "get") || Directives.has(fn, "set")) {
						if (this.getField(fn.getName())==null) {
							Field f = new Field(fn.getName(), fn.getReturn());
							f.pkgName = fn.pkgName;
							this.addField(f);
						}
					}
					break;
				case GLOBAL_DIR:
					String s = (String) e.args[0];
					directives.add(s.substring(1));
					break;
				case TYPE_EXT:
					Element typeExtend = (Element) e.args[1];
					String typeName = (String) e.args[0];
					ArrayList<Element> code = (ArrayList<Element>) e.args[2];
					errors.addAll(parseType(code, typeName, typeExtend));
					break;
				case TYPE:
					typeName = (String) e.args[0];
					code = (ArrayList<Element>) e.args[2];
					errors.addAll(parseType(code, typeName, null));
					break;
			}
		}
		return errors;
	}
	
	public ArrayList<SourceException> parseType(ArrayList<Element> code, String name, Element extend) {
		ArrayList<SourceException> errs = new ArrayList<>();
		
		CustomType td = new CustomType(name, extend);
		this.addCustomType(td);
		
		for (Element e : code) {
			if (e.type instanceof Rule) {
				switch ((Rule)e.type) {
					case ITERATOR:
					case FUNC:
						String fname = name+"."+(String) e.args[0];
						ArrayList<Field> args = new ArrayList<>();
						Element rets = e.dataType;
						for (Element e2 : (ArrayList<Element>) e.args[1]) {
							if (e2.args[0] instanceof ArrayList) {
								for (Element e3 : (ArrayList<Element>) e2.args[0]) {
									args.add(new Field((String)e3.args[0], e3.dataType));
								}
							} else {
								args.add(new Field((String)e2.args[0], e2.dataType));
							}
						}

						Function fn;
						if (e.type==Rule.ITERATOR) {
							fn = new Iterator(fname, args,rets);
						} else {
							fn = new Function(fname,args,rets);
						}

						fn.getDirectives().addAll(e.directives);
						fn.getDirectives().addAll(directives);
						fn.rawCode = (ArrayList<Element>) e.args[2];

						if (e.args[3]!=null) {
							ArrayList<Element> es = (ArrayList<Element>) e.args[3];
							if (es.size()>0 && es.get(0).type==Rule.TUPLE) {
								es = (ArrayList<Element>) es.get(0).args[0];
							}
							fn.rawParams = new ArrayList<>();
							for (Element e2 : es) {
								Field param = new Field((String) e2.args[0], e2.dataType);
								fn.rawParams.add(param);
							}
						}

						if (e.type==Rule.ITERATOR) {
							addIterator((Iterator)fn);
						} else {
							addFunction(fn);
						}

						if (Directives.has(fn, "get") || Directives.has(fn, "set")) {
							if (this.getField(fn.getName())==null) {
								Field f = new Field(fn.getName(), fn.getReturn());
								f.pkgName = fn.pkgName;
								this.addField(f);
							}
						}
						break;
				}
			}
		}
		
		return errs;
	}
	
	public void addContents(SourcePackage other) {
		if (other==null) {
			return;
		}
		
		fields.addAll(other.fields);
		functions.addAll(other.functions);
		imports.addAll(other.imports);
		types.addAll(other.types);
		customTypes.addAll(other.customTypes);
		iters.addAll(other.iters);
		directives.addAll(other.directives);
		aliases.addAll(other.aliases);
	}
	
	public void addFunction(Function fn) {
		fn.pkgName = this.getName();
		fn.order = getFunctions(fn.name).size();
		functions.add(fn);
	}
	
	public void addField(Field f) {
		f.pkgName = this.getName();
		fields.add(f);
	}

	public String getName() {
		if (name==null) {
			name = nameProvider.getTempName();
		}
		return name;
	}

	public ArrayList<Import> getImports() {
		return imports;
	}

	public ArrayList<Function> getFunctions() {
		return functions;
	}
	
	public ArrayList<Function> getFunctionsAndIterators() {
		ArrayList<Function> a = new ArrayList<>();
		a.addAll(functions);
		a.addAll(iters);
		return a;
	}
	
	public ArrayList<Function> getFunctions(String name) {
		ArrayList<Function> a = new ArrayList<>();
		for (Function v : functions) {
			if (v.getName().equals(name) || (v.pkgName+"."+v.getName()).equals(name)) {
				a.add(v);
			}
		}
		return a;
	}
	
	public ArrayList<Field> getFields() {
		return fields;
	}
	
	public Field getField(String name) {
		for (Field v : fields) {
			if (v.getName().equals(name) || (v.pkgName+"."+v.getName()).equals(name)) {
				return v;
			}
		}
		return null;
	}

	public Function getFunction(String name) {
		for (Function v : functions) {
			if (v.getName().equals(name) || (v.pkgName+"."+v.getName()).equals(name) || (v.pkgName+"."+v.getName()+"%"+v.order).equals(name) || (v.getName()+"%"+v.order).equals(name)) {
				return v;
			}
		}
		return null;
	}
	
	public boolean isFunctionCallCompatible(Function v, FunctionCall call) {
		if (call.isIter || (call.ret == null || call.ret.type==TypeDef.UNKNOWN || DataType.canCastTo(call.ret, v.getReturnType()))) {
			if (call.args.size()==v.args.size()) {
				boolean dirsMatch = true;
				if (call.dirsMatter) {
					for (String dir : call.dirs) {
						if (!Directives.has(v, dir)) {
							dirsMatch = false;
							break;
						}
					}
				}
				if (dirsMatch) {
					int i = 0;
					boolean argsMatch = true;
					for (Field arg : v.args) {
						if (call.args.get(i)!= null && !DataType.canCastTo(arg.getType(), call.args.get(i))) {
							argsMatch = false;
							break;
						}
						i++;
					}
					if (argsMatch) {
						if (call.isIter) {
							return true;
						} else {
							return true;
						}
					}
				}
			}
		}
		if (call.dirsMatter) {
			call.dirsMatter = false;
			return isFunctionCallCompatible(v, call);
		}
		return false;
	}
	
	public Function getFunction(String name, FunctionCall call) {
		ArrayList fns = call.isIter?iters:functions;
		for (Object rawv : fns) {
			Function v = (Function) rawv;
			if (v.getName().equals(name) || (v.pkgName+"."+v.getName()).equals(name) || (v.pkgName+"."+v.getName()+"%"+v.order).equals(name) || (v.getName()+"%"+v.order).equals(name)) {
				if (isFunctionCallCompatible(v, call)) {
					return v;
				}
			}
		}
		return null;
	}

	public ArrayList<TypeDef> getTypes() {
		return types;
	}
	
	public void addType(TypeDef def) {
		def.pkgName = this.getName();
		types.add(def);
	}
	
	public TypeDef getType(String name) {
		for (TypeDef v : types) {
			if (v.name.equals(name) || (v.pkgName+"."+v.name).equals(name)) {
				return v;
			}
		}
		return null;
	}
	
	public ArrayList<CustomType> getCustomTypes() {
		return customTypes;
	}
	
	public void addCustomType(CustomType def) {
		def.pkgName = this.getName();
		customTypes.add(def);
		types.add(def);
	}
	
	public TypeDef getCustomType(String name) {
		for (CustomType v : customTypes) {
			if (v.name.equals(name) || (v.pkgName+"."+v.name).equals(name)) {
				return v;
			}
		}
		return null;
	}

	@Override
	public ArrayList<String> getDirectives() {
		return directives;
	}
	
	public ArrayList<Iterator> getIterators(String name) {
		ArrayList<Iterator> a = new ArrayList<>();
		for (Iterator v : iters) {
			if (v.getName().equals(name) || (v.pkgName+"."+v.getName()).equals(name)) {
				a.add(v);
			}
		}
		return a;
	}
	
	public ArrayList<Iterator> getIterators() {
		return iters;
	}
	
	public void addIterator(Iterator def) {
		def.pkgName = this.getName();
		def.order = getIterators(def.name).size();
		iters.add(def);
	}
	
	public Iterator getIterator(String name) {
		for (Iterator v : iters) {
			if (v.getName().equals(name) || (v.pkgName+"."+v.getName()).equals(name) || (v.pkgName+"."+v.getName()+"%"+v.order).equals(name) || (v.getName()+"%"+v.order).equals(name)) {
				return v;
			}
		}
		return null;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<ImportAlias> getAliases() {
		return aliases;
	}
} 
