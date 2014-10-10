package com.iconmaster.source.compile;

/**
 *
 * @author iconmaster
 */
public class Operation {
	public enum OpType {
		MOV,MOVN,ADD;
		
		public static boolean hasLVar(OpType type) {
			switch (type) {
				case MOV:
				case MOVN:
				case ADD:
					return true;
				default:
					return false;
			}
		}
	}
	
	public OpType op;
	public String[] args;
	public VarSpace vspace;

	public Operation(OpType op, String... args) {
		this.op = op;
		this.args = args;
	}
	
	public VarSpace createVarspace(VarSpace parent) {
		this.vspace = new VarSpace(parent);
		return vspace;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(op.toString());
		sb.append("\t");
		for (String arg : args) {
			sb.append(arg).append("\t");
		}
		return sb.toString();
	}
}
