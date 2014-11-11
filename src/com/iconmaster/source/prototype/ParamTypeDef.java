package com.iconmaster.source.prototype;

/**
 *
 * @author iconmaster
 */
public class ParamTypeDef extends TypeDef {
	public int paramNo;
	public TypeDef baseType;
	
	public ParamTypeDef(String name, int paramNo) {
		this(name,paramNo,TypeDef.UNKNOWN);
	}

	public ParamTypeDef(String name, int paramNo, TypeDef baseType) {
		super(name,baseType);
		this.paramNo = paramNo;
		this.baseType = baseType;
		
		if (baseType.hasParams) {
			this.setParamSettings(params, varargParams);
		}
		
		if (baseType.indexable) {
			this.setIndexSettings(indexableBy, indexReturns, varargIndex);
		}
	}
}
