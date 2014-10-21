package com.iconmaster.source.compile;

import com.iconmaster.source.element.Rule;
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

		public static OpType MathToOpType(Rule e) {
			switch (e) {
				case ADD:
					return OpType.ADD;
				case SUB:
					return OpType.SUB;
				case MUL:
					return OpType.MUL;
				case DIV:
					return OpType.DIV;
				case MOD:
					return OpType.MOD;
				case POW:
					return OpType.POW;
				case AND:
					return OpType.AND;
				case OR:
					return OpType.OR;
				case NOT:
					return OpType.NOT;
				case NEG:
					return OpType.NEG;
				case BIT_AND:
					return OpType.BAND;
				case BIT_OR:
					return OpType.BOR;
				case BIT_NOT:
					return OpType.BNOT;
				case EQ:
					return OpType.EQ;
				case NEQ:
					return OpType.NEQ;
				case LT:
					return OpType.LT;
				case GT:
					return OpType.GT;
				case LTE:
					return OpType.LE;
				case GTE:
					return OpType.GE;
				default:
					return null;
			}
		}
		
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
		
		public boolean isMathOp() {
			switch (this) {
				case ADD:
				case SUB:
				case MUL:
				case DIV:
				case MOD:
				case POW:
				case AND:
				case OR:
				case BAND:
				case BOR:
				case CONCAT:
				case EQ:
				case NEQ:
				case LT:
				case GT:
				case LE:
				case GE:
					return true;
				default:
					return false;
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
			case DEF:
				return new String[] {};
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
