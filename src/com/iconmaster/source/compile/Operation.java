package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.TypeDef;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author iconmaster
 */
public class Operation {
	public enum OpType {
		MOV,MOVN,MOVS,MOVL,MOVA,CALL,RET,BRK,CONT,
		BEGIN,END,PROP,NOP,DEF,
		IF,ELSE,FOR,ITER,DO,WHILE,REP,
		ENDB,NATIVE,
		LABEL,GOTO,GOTOT,GOTOF,TRUE,FALSE;
		
		public boolean hasLVar() {
			if (this.isBlockStarter()) {
				return false;
			}
			switch (this) {
				case ELSE:
				case RET:
				case BRK:
				case CONT:
				case PROP:
				case BEGIN:
				case END:
				case ENDB:
				case DO:
				case LABEL:
				case GOTO:
				case GOTOT:
				case GOTOF:
				case NATIVE:
				case DEF:
				case ITER:
					return false;
				default:
					return true;
			}
		}
		
		public boolean isBlockStarter() {
			switch (this) {
				case IF:
				case WHILE:
				case REP:
				case FOR:
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
			case PROP:
			case DEF:
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
	
	public Boolean[] getVarSlots() {
		switch (this.op) {
			case PROP:
			case DEF:
			case FOR:
				return new Boolean[] {};
			case MOVN:
			case MOVS:
				return new Boolean[] {true,false};
			case CALL:
				ArrayList<Boolean> a = new ArrayList<>();
				a.add(true);
				a.add(false);
				for (int i=2;i<args.length;i++) {
					a.add(true);
				}
				return a.toArray(new Boolean[0]);
			case ITER:
				a = new ArrayList<>();
				a.add(false);
				for (int i=1;i<args.length;i++) {
					a.add(true);
				}
				return a.toArray(new Boolean[0]);
			default:
				a = new ArrayList<>();
				for (int i=0;i<args.length;i++) {
					a.add(true);
				}
				return a.toArray(new Boolean[0]);
		}
	}
	
	Operation cloneOp() {
		return new Operation(op, type, range, Arrays.copyOf(args, args.length));
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 67 * hash + Objects.hashCode(this.op);
		hash = 67 * hash + Arrays.deepHashCode(this.args);
		hash = 67 * hash + Objects.hashCode(this.range);
		hash = 67 * hash + Objects.hashCode(this.type);
		return hash;
	}
}
