package com.iconmaster.source.compile;

import com.iconmaster.source.util.Range;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Operation {
	public enum OpType {
		MOV,MOVN,MOVS,MOVL,MOVI,CALL,INDEX,RET,BRK,DEF,
		IF,ELSE,ELIF,FORR,FORE,FORP,WHILE,REP,END,
		ADD,SUB,MUL,DIV,MOD,POW,AND,OR,NOT,NEG,BAND,BOR,BNOT,CONCAT,EQ,NEQ,LT,GT,LE,GE;
		
		public boolean hasLVar() {
			switch (this) {
				case RET:
				case BRK:
				case DEF:
					return false;
				default:
					return true;
			}
		}
	}
	
	public OpType op;
	public String[] args;
	public VarSpace vspace;
	public Range range;

	public Operation(OpType op, Range range, String... args) {
		this.op = op;
		this.range = range;
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
	
	public String[] getVarNames() {
		switch (this.op) {
			case MOVN:
			case MOVS:
			case MOVL:
				return new String[] {this.args[0]};
			case CALL:
				ArrayList<String> a = new ArrayList<>();
				a.add(args[0]);
				for (int i=2;i<args.length;i++) {
					a.add(args[i]);
				}
				return a.toArray(new String[] {});
			default:
				return this.args;
		}
	}
}
