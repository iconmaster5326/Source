package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.compile.DataType;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.Iterator;
import com.iconmaster.source.prototype.ParamTypeDef;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class LibraryCore extends SourcePackage {
	
	public static TypeDef[] INT_TYPES = new TypeDef[] {TypeDef.INT, TypeDef.INT8, TypeDef.INT16, TypeDef.INT32, TypeDef.INT64};
	public static TypeDef[] REAL_TYPES = new TypeDef[] {TypeDef.REAL, TypeDef.REAL32, TypeDef.REAL64};
	public static TypeDef[] MATH_TYPES = new TypeDef[] {TypeDef.INT, TypeDef.REAL, TypeDef.INT8, TypeDef.INT16, TypeDef.INT32, TypeDef.INT64, TypeDef.REAL32, TypeDef.REAL64};
	
	public static String[] MATH_OPS = new String[] {"_add","_sub","_mul","_div","_mod","_pow"};
	public static String[] BIT_OPS = new String[] {"_bit_and","_bit_or"};
	public static String[] BOOL_OPS = new String[] {"_eq","_neq","_lt","_gt","_le","_ge","_and","_or"};
	
	public LibraryCore() {
		this.name = "core";
		
		//Add all the base data types in core:
		TypeDef.addBaseTypes(this);
		
		Function fn;
		Field f;
		Iterator iter;
		
		fn = Function.libraryFunction("print", new String[] {"item"}, new Object[] {TypeDef.UNKNOWN}, null);
		this.addFunction(fn);
		
		fn = Function.libraryFunction("input", new String[] {}, new Object[] {}, TypeDef.STRING);
		this.addFunction(fn);
		
		fn = Function.libraryFunction("error", new String[] {}, new Object[] {}, null);
		this.addFunction(fn);
		
		fn = Function.libraryFunction("error", new String[] {"msg"}, new Object[] {TypeDef.UNKNOWN}, null);
		this.addFunction(fn);
		
		TypeDef iftet = new ParamTypeDef("T", 0);
		fn = Function.libraryFunction("ifte", new String[] {"cond","then","else"}, new Object[] {TypeDef.BOOLEAN,iftet,iftet}, iftet);
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("T"));
		this.addFunction(fn);
		
		fn = Function.libraryFunction("typeof", new String[] {"item"}, new Object[] {TypeDef.UNKNOWN}, null);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		
		fn = Function.libraryFunction("sizeof", new String[] {"item"}, new Object[] {TypeDef.UNKNOWN}, null);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		
		for (TypeDef type : MATH_TYPES) {
			DataType array = new DataType(TypeDef.ARRAY);
			array.params = new DataType[] {new DataType(type)};
			
			fn = Function.libraryFunction("range", new String[] {"begin","end"}, new Object[] {type,type}, array);
			fn.getDirectives().add("pure");
			this.addFunction(fn);
			
			fn = Function.libraryFunction("range", new String[] {"begin","end","step"}, new Object[] {type,type,type}, array);
			fn.getDirectives().add("pure");
			this.addFunction(fn);
			
			iter = Iterator.libraryIterator("range", new String[] {"begin","end"}, new Object[] {type,type}, new Object[] {type});
			fn.getDirectives().add("pure");
			this.addIterator(iter);
		
			iter = Iterator.libraryIterator("range", new String[] {"begin","end","step"}, new Object[] {type,type,type}, new Object[] {type});
			fn.getDirectives().add("pure");
			this.addIterator(iter);
		}
		
		//overloads
		for (TypeDef type : MATH_TYPES) {
			for (String op : MATH_OPS) {
				fn = Function.libraryFunction(type.name+"."+op, new String[] {"a1","a2"}, new Object[] {type,type}, type);
				fn.getDirectives().add("pure");
				this.addFunction(fn);
			}
		}
		
		for (TypeDef type : MATH_TYPES) {
			for (String op : BOOL_OPS) {
				fn = Function.libraryFunction(type.name+"."+op, new String[] {"a1","a2"}, new Object[] {type,type}, TypeDef.BOOLEAN);
				fn.getDirectives().add("pure");
				this.addFunction(fn);
			}
		}
		
		for (TypeDef type : INT_TYPES) {
			for (String op : BIT_OPS) {
				fn = Function.libraryFunction(type.name+"."+op, new String[] {"a1","a2"}, new Object[] {type,type}, type);
				fn.getDirectives().add("pure");
				this.addFunction(fn);
			}
		}
		
		for (TypeDef type : MATH_TYPES) {
			fn = Function.libraryFunction(type.name+"._neg", new String[] {"a1"}, new Object[] {type}, type);
			fn.getDirectives().add("pure");
			this.addFunction(fn);
		}
		
		fn = Function.libraryFunction("?._concat", new String[] {"a1","a2"}, new Object[] {TypeDef.UNKNOWN,TypeDef.UNKNOWN}, TypeDef.STRING);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		
		for (TypeDef type1 : MATH_TYPES) {
			for (TypeDef type2 : MATH_TYPES) {
				if (type1!=type2) {
					fn = Function.libraryFunction(type2.name+"._cast", new String[] {"from"}, new Object[] {type1}, type2);
					fn.getDirectives().add("pure");
					this.addFunction(fn);
				}
			}
		}
		
		//int/real type functions
		for (TypeDef type : MATH_TYPES) {
			f = Field.libraryField(type.name+".minValue", type);
			this.addField(f);
			f = Field.libraryField(type.name+".maxValue", type);
			this.addField(f);
		}
		
		//array functions
		TypeDef att = new ParamTypeDef("T", 0); //this is T
		DataType atdt = new DataType(TypeDef.ARRAY); //this is array[T]
		atdt.params = new DataType[] {new DataType(att)};
		
		for (TypeDef type : INT_TYPES) {
			fn = Function.libraryFunction("array._getindex", new String[] {"a","i"}, new Object[] {atdt,type}, att);
			fn.getDirectives().add("pure");
			fn.rawParams = new ArrayList<>();
			fn.rawParams.add(new Field("T"));
			this.addFunction(fn);

			fn = Function.libraryFunction("array._setindex", new String[] {"a","v","i"}, new Object[] {atdt,att,type}, null);
			fn.rawParams = new ArrayList<>();
			fn.rawParams.add(new Field("T"));
			this.addFunction(fn);
		}
		
		iter = Iterator.libraryIterator("array._iter", new String[] {"a"}, new Object[] {atdt}, new Object[] {att});
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("T"));
		this.addIterator(iter);
		
		fn = Function.libraryFunction("array._cast", new String[] {"a"}, new Object[] {TypeDef.LIST}, TypeDef.ARRAY);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		
		//list functions
		DataType ltdt = new DataType(TypeDef.LIST); //this is list[T]
		TypeDef ltt = new ParamTypeDef("T", 0); //this is T
		ltdt.params = new DataType[] {new DataType(ltt)};
		
		for (TypeDef type : INT_TYPES) {
			fn = Function.libraryFunction("list._getindex", new String[] {"a","i"}, new Object[] {ltdt,type}, att);
			fn.getDirectives().add("pure");
			fn.rawParams = new ArrayList<>();
			fn.rawParams.add(new Field("T"));
			this.addFunction(fn);

			fn = Function.libraryFunction("list._setindex", new String[] {"a","v","i"}, new Object[] {ltdt,ltt,type}, null);
			fn.rawParams = new ArrayList<>();
			fn.rawParams.add(new Field("T"));
			this.addFunction(fn);
		}
		
		iter = Iterator.libraryIterator("list._iter", new String[] {"a"}, new Object[] {ltdt}, new Object[] {ltt});
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("T"));
		this.addIterator(iter);
		
		fn = Function.libraryFunction("list._cast", new String[] {"a"}, new Object[] {TypeDef.ARRAY}, TypeDef.LIST);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		
		//string functions
		for (TypeDef type : INT_TYPES) {
			fn = Function.libraryFunction("string._getindex", new String[] {"s","i"}, new Object[] {TypeDef.STRING,type}, TypeDef.CHAR);
			fn.getDirectives().add("pure");
			this.addFunction(fn);

			fn = Function.libraryFunction("string._setindex", new String[] {"s","v","i"}, new Object[] {TypeDef.STRING,TypeDef.CHAR,type}, null);
			this.addFunction(fn);
		}
		
		iter = Iterator.libraryIterator("string._iter", new String[] {"s"}, new Object[] {TypeDef.STRING}, new Object[] {TypeDef.CHAR});
		this.addIterator(iter);
		
		for (TypeDef type : MATH_TYPES) {
			fn = Function.libraryFunction("string._cast", new String[] {"s"}, new Object[] {type}, TypeDef.STRING);
			fn.getDirectives().add("pure");
			this.addFunction(fn);
			
			fn = Function.libraryFunction(type.name+"._cast", new String[] {"s"}, new Object[] {TypeDef.STRING}, type);
			fn.getDirectives().add("pure");
			this.addFunction(fn);
		}
		
		fn = Function.libraryFunction("string._cast", new String[] {"s"}, new Object[] {TypeDef.UNKNOWN}, TypeDef.STRING);
		fn.getDirectives().add("pure");
		this.addFunction(fn);
		
		//map functions
		TypeDef mkt = new ParamTypeDef("K", 0); //this is K
		TypeDef mvt = new ParamTypeDef("V", 1); //this is V
		DataType mkvdt = new DataType(TypeDef.ARRAY); //this is map[K,V]
		atdt.params = new DataType[] {new DataType(mkt),new DataType(mvt)};
		
		iter = Iterator.libraryIterator("map._iter", new String[] {"m"}, new Object[] {mkvdt}, new Object[] {mkt,mvt});
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("K"));
		fn.rawParams.add(new Field("V"));
		this.addIterator(iter);
		
		fn = Function.libraryFunction("map._getindex", new String[] {"m","i"}, new Object[] {mkvdt,mkt}, mvt);
		fn.getDirectives().add("pure");
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("K"));
		fn.rawParams.add(new Field("V"));
		this.addFunction(fn);
		
		fn = Function.libraryFunction("map._setindex", new String[] {"m","v","i"}, new Object[] {mkvdt, mvt, mkt}, null);
		fn.rawParams = new ArrayList<>();
		fn.rawParams.add(new Field("K"));
		fn.rawParams.add(new Field("V"));
		this.addFunction(fn);
	}
}