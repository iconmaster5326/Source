package com.iconmaster.source.compile;

import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.prototype.ParamTypeDef;
import com.iconmaster.source.prototype.TypeDef;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class Parameterizer {
	public static ArrayList<DataType> parameterize(CompileData cd, Range range, ArrayList<DataType> callTypes, ArrayList<DataType> gotTypes) {
		HashMap<String,DataType> map = parameterize(cd, range, callTypes, gotTypes, new HashMap<>());
		ArrayList<DataType> a = new ArrayList<>();
		for (DataType dt : callTypes) {
			a.add(replaceWithParams(dt, map));
		}
		return a;
	}
	
	public static HashMap<String,DataType> parameterize(CompileData cd, Range range, ArrayList<DataType> callTypes, ArrayList<DataType> gotTypes, HashMap<String,DataType> map) {
		int i = 0;
		for (DataType dt : callTypes) {
			DataType got = i >= gotTypes.size() ? new DataType(true) : gotTypes.get(i);
			
			if (got==null) {
				got = new DataType();
			}
			
			if (dt.type instanceof ParamTypeDef) {
				TypeDef maxParent = dt.type.parent;
				if (map.containsKey(dt.type.name)) {
					DataType pType = map.get(dt.type.name);
					TypeDef mutual = TypeDef.getCommonParent(pType.type, got.type);
					if (!DataType.canCastTo(new DataType(pType.type), new DataType(mutual))) {
						cd.errs.add(new SourceException(range, "Cannot parameterize data type "+dt+" to type "+got));
					}
					map.put(dt.type.name, new DataType(mutual));
				} else {
					map.put(dt.type.name, got);
				}
			}
			
			if (dt.params.length>0 || got.params.length>0) {
				ArrayList<DataType> ct2 = new ArrayList<>();
				ArrayList<DataType> gt2 = new ArrayList<>();
				int max = Math.max(dt.params.length,got.params.length);
				for (int j=0; j<max; j++) {
					ct2.add(j>=dt.params.length?new DataType():dt.params[j]);
					gt2.add(j>=got.params.length?new DataType():got.params[j]);
				}
				parameterize(cd, range, ct2, gt2, map);
			}
			i++;
		}
		return map;
	}
	
	public static DataType replaceWithParams(DataType type, HashMap<String,DataType> map) {
		if (type==null) {
			return null;
		}
		
		if (type.type instanceof ParamTypeDef) {
			return map.get(type.type.name);
		}
		
		int i = 0;
		for (DataType param : type.params) {
			type.params[i] = replaceWithParams(param, map);
			i++;
		}
		
		return type;
	}
	
	public static boolean canParameterize(CompileData cd, Range range, ArrayList<DataType> callTypes, ArrayList<DataType> gotTypes) {
		ArrayList<SourceException> errs = cd.errs;
		ArrayList<SourceException> newErrs = new ArrayList<>();
		cd.errs = newErrs;
		
		parameterize(cd, range, callTypes, gotTypes, new HashMap<>());
		
		cd.errs = errs;
		
		return newErrs.isEmpty();
	}
	
	public static Object test() {
		ArrayList<DataType> a1 = new ArrayList<>();
		DataType dt = new DataType(TypeDef.LIST);
		dt.params = new DataType[] {new DataType(new ParamTypeDef("T", 0, TypeDef.REAL))};
		a1.add(dt);
		a1.add(new DataType(new ParamTypeDef("T", 0, TypeDef.REAL)));
		ArrayList<DataType> a2 = new ArrayList<>();
		dt = new DataType(TypeDef.LIST);
		dt.params = new DataType[] {new DataType(TypeDef.REAL)};
		a2.add(dt);
		a2.add(new DataType(TypeDef.INT));
		CompileData cd = new CompileData(null);
		ArrayList<DataType> r = Parameterizer.parameterize(cd, null, a1, a2);
		System.out.println(cd.errs);
		return r;
	}
}
