package com.iconmaster.source.compile;

import com.iconmaster.source.element.Rule;
import com.iconmaster.source.prototype.TypeDef;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Operation {
	public enum OpType {
		MOV,MOVN,MOVS,MOVL,MOVI,CALL,INDEX,RET,BRK,
		DEF,BEGIN,END,PROP,NOP,TYPE,
		IF,ELSE,FORR,FORE,FORP,DO,WHILE,REP,
		ENDB,NATIVE,
		LABEL,GOTO,GOTOT,GOTOF,TRUE,FALSE,
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
				case BIT_AND:
					return OpType.BAND;
				case BIT_OR:
					return OpType.BOR;
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
				case CONCAT:
					return OpType.CONCAT;
				default:
					return null;
			}
		}
		
		public boolean hasLVar() {
			if (this.isBlockStarter()) {
				return false;
			}
			switch (this) {
				case RET:
				case BRK:
				case DEF:
				case PROP:
				case BEGIN:
				case END:
				case ENDB:
				case MOVI:
				case TYPE:
				case DO:
				case LABEL:
				case GOTO:
				case GOTOT:
				case GOTOF:
				case NATIVE:
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
		
		public boolean isBlockStarter() {
			switch (this) {
				case IF:
				case ELSE:
				case WHILE:
				case REP:
				case FORR:
				case FORE:
				case FORP:
					return true;
				default:
					return false;
			}
		}
		
		public boolean isBooleanMathOp() {
			switch (this) {
				case AND:
				case OR:
				case NOT:
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
	public Range range;
	public TypeDef type;

	public Operation(OpType op, String... args) {
		this.op = op;
		this.args = args;
	}
	
	public Operation(OpType op, Range range, String... args) {
		this.op = op;
		this.range = range;
		this.args = args;
	}
	
	public Operation(OpType op, TypeDef type, Range range, String... args) {
		this.op = op;
		this.type = type;
		this.range = range;
		this.args = args;
	}
	
	public Operation(OpType op, DataType type, Range range, String... args) {
		this.op = op;
		this.type = type==null?null:type.type;
		this.range = range;
		this.args = args;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(op.toString());
		sb.append("\t");
		for (String arg : args) {
			sb.append(arg).append("\t");
		}
		if (type!=null) {
			sb.append("(");
			sb.append(type);
			sb.append(")");
		}
		return sb.toString();
	}
	
	public String[] getVarNames() {
		switch (this.op) {
			case DEF:
			case PROP:
				return new String[] {};
			case MOVN:
			case MOVS:
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
