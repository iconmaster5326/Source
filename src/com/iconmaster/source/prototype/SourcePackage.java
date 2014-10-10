package com.iconmaster.source.prototype;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.util.ElementHelper;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class SourcePackage {
	String name;
	ArrayList<Variable> fields = new ArrayList<>();
	ArrayList<Function> functions = new ArrayList<>();
	ArrayList<String> imports = new ArrayList<>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PACKAGE ");
		sb.append(name).append(":\n\tIMPORTS:");
		for (String imp : imports) {
			sb.append("\n\t\t");
			sb.append(imp);
		}
		sb.append("\n\tFIELDS:");
		for (Variable field : fields) {
			sb.append("\n\t\t");
			sb.append(field.toString().replace("\n", "\n\t"));
		}
		sb.append("\n\tFUNCTIONS:");
		for (Function func : functions) {
			sb.append("\n\t\t");
			sb.append(func.toString().replace("\n", "\n\t"));
		}
		return sb.toString();
	}
	
	public ArrayList<SourceException> parse(ArrayList<Element> a) {
		ArrayList<SourceException> errors = new ArrayList<>();
		for (Element e : a) {
			switch ((Rule)e.type) {
				case PACKAGE:
					if (name!=null) {
						errors.add(new SourceException(e.range,"Cannot have multiple package declarations"));
					} else {
						name = ElementHelper.nameString((Element) e.args[0]);
						if (name == null) {
							errors.add(new SourceException(e.range,"Invalid package name"));
						}
					}
					break;
				case IMPORT:
					String imp = ElementHelper.nameString((Element) e.args[0]);
					if (name == null) {
						errors.add(new SourceException(e.range,"Invalid import package"));
					} else {
						imports.add(imp);
					}
					break;
				case FIELD_ASN:
					int i=0;
					ArrayList<Element> vals = ((ArrayList<Element>) e.args[1]);
					for (Element e2 : (ArrayList<Element>) e.args[0]) {
						try {
							Variable var = new Variable((String)e2.args[0], new DataType(e2.dataType));
							if (i < vals.size()) {
								var.rawValue = vals.get(i);
							}
							fields.add(var);
						} catch (SourceException ex) {
							errors.add(ex);
						}
						i++;
					}
					break;
				case FIELD:
					for (Element e2 : (ArrayList<Element>) e.args[0]) {
						try {
							Variable var = new Variable((String)e2.args[0], new DataType(e2.dataType));
							fields.add(var);
						} catch (SourceException ex) {
							errors.add(ex);
						}
					}
					break;
				case FUNC:
					try {
						String fname = (String) e.args[0];
						ArrayList<Variable> args = new ArrayList<>();
						ArrayList<DataType> rets = DataType.getFuncReturn(e.dataType);
						
						for (Element e2 : (ArrayList<Element>) e.args[1]) {
							if (e2.args[0] instanceof ArrayList) {
								for (Element e3 : (ArrayList<Element>) e2.args[0]) {
									args.add(new Variable((String)e3.args[0], new DataType(e3.dataType)));
								}
							} else {
								args.add(new Variable((String)e2.args[0], new DataType(e2.dataType)));
							}
						}
						
						Function fn = new Function(fname,args,rets);
						fn.rawCode = (ArrayList<Element>) e.args[2];
						functions.add(fn);
					} catch (SourceException ex) {
						errors.add(ex);
					}
					break;
				case ENUM:
				case STRUCT:
				case GLOBAL_DIR:
			}
		}
		return errors;
	}
	
	public void addContents(SourcePackage other) {
		fields.addAll(other.fields);
		functions.addAll(other.functions);
	}
	
	public void addFunction(Function fn) {
		functions.add(fn);
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getImports() {
		return imports;
	}
}
