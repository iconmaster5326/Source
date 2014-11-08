package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.assemble.AssemblyUtils;
import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Directives;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author iconmaster
 */
public class HPPLAssembler {
	public static String assemble(SourcePackage pkg) {
		StringBuilder sb = new StringBuilder("#pragma mode( separator(.,;) integer(h32) )\n\n//This program compiled with Source: www.github.com/iconmaster5326/Source\n\n");
		AssemblyData ad = new AssemblyData();
		ad.pkg = pkg;
		ad.vs = new Stack<>();
		ad.vs.add(new AssembleVarSpace());
		for (Function fn : pkg.getFunctions()) {
			if (shouldIncludeFunction(fn)) {
				sb.append(fn.compileName);
				sb.append("(");
				if (!fn.getArguments().isEmpty()) {
					for (Field arg : fn.getArguments()) {
						sb.append(arg.getName());
						sb.append(',');
					}
					sb.deleteCharAt(sb.length()-1);
				}
				sb.append(");");
			}
		}
		sb.append("\n");
		for (Field var : pkg.getFields()) {
			if (shouldIncludeField(var)) {
				ad.workingOn = var;
				ad.dirs = var.getDirectives();
				ad.vs.add(new AssembleVarSpace());
				sb.append(assembleField(ad,var));
				ad.vs.pop();
			}
		}
		sb.append("\n");
		for (Function fn : pkg.getFunctions()) {
			if (shouldIncludeFunction(fn)) {
				ad.workingOn = fn;
				ad.dirs = fn.getDirectives();
				ad.vs.add(new AssembleVarSpace());
				sb.append(assembleFunction(ad,fn));
				ad.vs.pop();
			}
		}
		return sb.toString();
	}
	
	private static String assembleFunction(AssemblyData ad, Function fn) {
		StringBuilder sb = new StringBuilder();
		if (Directives.has(fn, "export")) {
			sb.append("EXPORT ");
		}
		sb.append(fn.compileName);
		sb.append("(");
		if (!fn.getArguments().isEmpty()) {
			for (Field arg : fn.getArguments()) {
				sb.append(arg.getName());
				sb.append(',');
			}
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append(")\nBEGIN\n ");
		if (fn.getCode()!=null) {
			sb.append(assembleCode(ad,fn.getCode()).replace("\n", "\n "));
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("\nEND;\n");
		return sb.toString();
	}
	
	private static String assembleField(AssemblyData ad, Field var) {
		StringBuilder sb = new StringBuilder();
		if (Directives.has(var, "export")) {
			sb.append("EXPORT ");
		}
		if (var.getValue()!=null) {
			sb.append(assembleCode(ad,var.getValue()));
		} else {
			sb.append(var.compileName);
			sb.append(";");
		}
		return sb.toString();
	}
	
	private static String assembleCode(AssemblyData ad, ArrayList<Operation> expr) {
		Stack<AssembleVarSpace> vs = new Stack<>();
		vs.add(new AssembleVarSpace());
		
		StringBuilder sb = new StringBuilder();
		Stack<Operation> blockOp = new Stack<>();
		Operation lastBlockOp = null;
		for (Operation op : expr) {
			boolean append = true;
			if (canRemove(ad, expr, op, vs)) {
				append = false;
			} else if (op.op.hasLVar()) {
				String s = assembleExpression(ad, expr, op);
				if (s==null) {
					append = false;
				} else {
					if (!ditchLValue(ad, expr, op)) {
						addLocal(ad, expr, op, sb);
						Field f = ad.pkg.getField(op.args[0]);
						if (f!=null) {
							if (f.onCompile==null) {
								sb.append(op.args[0]);
								sb.append(":=");
							} else {
								sb.append(f.onCompile.compile(ad.pkg, false, ad));
								sb.append(":=");
							}
						} else {
							sb.append(op.args[0]);
							sb.append(":=");
						}
					}
					sb.append(s);
				}
			} else {
				switch (op.op) {
					case RET:
						sb.append("RETURN");
						if (op.args.length>0) {
							sb.append(" ");
							sb.append(getInlineString(ad, expr, op.args[0]));
						}
						break;
					case IF:
						sb.append("IF ");
						sb.append(getInlineString(ad, expr, op.args[0]));
						sb.append(" THEN\n");
						append = false;
						blockOp.push(op);
						break;
					case ELSE:
						sb.append("ELSE\n");
						append = false;
						blockOp.push(op);
						break;
					case WHILE:
						sb.append("WHILE ");
						sb.append(getInlineString(ad, expr, op.args[0]));
						sb.append(" DO\n");
						append = false;
						blockOp.push(op);
						break;
					case REP:
						sb.append("REPEAT\n");
						append = false;
						blockOp.push(op);
						break;
					case ENDB:
						lastBlockOp = blockOp.pop();
						if (lastBlockOp.op == Operation.OpType.REP) {
							sb.append("UNTIL ");
							sb.append(getInlineString(ad, expr, lastBlockOp.args[0]));
						} else {
							sb.append("END");
						}
						break;
					case BEGIN:
						vs.push(new AssembleVarSpace());
						append=false;
						break;
					case END:
						vs.pop();
						append=false;
						break;
					case DEF:
						for (String arg : op.args) {
							vs.peek().defs.add(arg);
						}
						append=false;
						break;
					case PROP:
						for (int i=1;i<op.args.length;i++) {
							if ("const".equals(op.args[i])) {
								vs.peek().consts.add(op.args[0]);
							}
						}
						append = false;
						break;
					case MOVI:
						sb.append(getInlineString(ad, expr, op.args[0]));
						sb.append("[");
						sb.append(getInlineString(ad, expr, op.args[2]));
						sb.append("]");
						sb.append(":=");
						sb.append(getInlineString(ad, expr, op.args[1]));
						break;
					case NATIVE:
						if (op.args[0].equalsIgnoreCase("hppl")) {
							sb.append(op.args[1]);
							sb.append("  ");
							append = false;
						} else if (op.args[0].equalsIgnoreCase("comment")) {
							sb.append("//");
							sb.append(op.args[1]);
							sb.append("\n");
							append = false;
						} else {
							sb.append("//Native code in unknown language ").append(op.args[0]).append(" specified here\n");
							append = false;
						}
						break;
					default:
						append = false;
				}
			}
			if (append) {
				sb.append(";\n");
			}
		}
		return sb.toString();
	}
	
	private static String assembleExpression(AssemblyData ad, ArrayList<Operation> expr, Operation op) {
		StringBuilder sb = new StringBuilder();
		if (op.op.isMathOp()) {
			sb.append(getInlineString(ad, expr, op.args[1]));
			sb.append(getMathOp(op.op));
			sb.append(getInlineString(ad, expr, op.args[2]));
		} else {
			switch (op.op) {
				case MOVN:
				case MOV:
					sb.append(getInlineString(ad, expr, op.args[1]));
					break;
				case MOVS:
					sb.append("\"");
					sb.append(op.args[1]);
					sb.append("\"");
					break;
				case MOVL:
					sb.append("{");
					if (op.args.length > 1) {
						for (int i=1;i<op.args.length;i++) {
							sb.append(getInlineString(ad, expr, op.args[i]));
							sb.append(",");
						}
						sb.deleteCharAt(sb.length()-1);
					}
					sb.append("}");
					break;
				case TRUE:
					sb.append("1");
					break;
				case FALSE:
					sb.append("0");
					break;
				case NOT:
					sb.append("NOT ");
					sb.append(getInlineString(ad, expr, op.args[1]));
					break;
				case NEG:
					sb.append(HPPLCharacters.NEG);
					sb.append(getInlineString(ad, expr, op.args[1]));
					break;
				case CALL:
					Function fn = ad.pkg.getFunction(op.args[1]);
					if (fn!=null) {
						if (fn.isLibrary()) {
							String oncomp = fn.compileFunction(ad.pkg,new PlatformContext(expr, op, sb, ad));
							if (oncomp!=null) {
								sb.append(oncomp);
								break;
							}
						}
					}
					String fs = fn.compileName;
					sb.append(fs);
					if (op.args.length > 2) {
						sb.append("(");
						for (int i=2;i<op.args.length;i++) {
							sb.append(getInlineString(ad, expr, op.args[i]));
							sb.append(",");
						}
						sb.deleteCharAt(sb.length()-1);
						sb.append(")");
					}
					break;
				case INDEX:
					sb.append(getInlineString(ad, expr, op.args[1]));
					sb.append("[");
					sb.append(getInlineString(ad, expr, op.args[2]));
					sb.append("]");
					break;
				default:
					return null;
			}
		}
		return sb.toString();
	}
	
	public static void addLocal(AssemblyData ad, ArrayList<Operation> code, Operation thisOp, StringBuilder sb) {
		boolean need = true;
		
		if (ad.pkg.getField(thisOp.args[0])!=null) {
			need = false;
		}
		
		for (AssembleVarSpace avs : ad.vs) {
			if (avs.vars.contains(thisOp.args[0])) {
				need = false;
			}
		}
		for (AssembleVarSpace avs : ad.vs) {
			if (avs.defs.contains(thisOp.args[0])) {
				avs.defs.remove(thisOp.args[0]);
				need = true;
			}
		}
		
		if (need) {
			sb.append("LOCAL ");
			ad.vs.peek().vars.add(thisOp.args[0]);
		}
	}
	
	public static String getMathOp(Operation.OpType type) {
			switch (type) {
				case ADD:
					return "+";
				case SUB:
					return "-";
				case MUL:
					return "*";
				case DIV:
					return "/";
				case MOD:
					return "%";
				case POW:
					return "^";
				case AND:
					return " AND ";
				case OR:
					return " OR ";
				case CONCAT:
					return "+";
				case EQ:
					return "==";
				case NEQ:
					return "<>";
				case LT:
					return "<";
				case GT:
					return ">";
				case LE:
					return "<=";
				case GE:
					return ">=";
				default:
					return null;
			}
		}
	
	public static boolean isInlinable(AssemblyData ad, ArrayList<Operation> code, String var) {
		if (Directives.has(ad.dirs, "!optimize")) {
			return false;
		}
		for (AssembleVarSpace avs : ad.vs) {
			if (avs.consts.contains(var)) {
				return true;
			}
		}
		ArrayList<Operation> lref = AssemblyUtils.getLReferences(ad.pkg, code, var);
		ArrayList<Operation> rref = AssemblyUtils.getRReferences(ad.pkg, code, var);
		if (lref.size()==1 && rref.size()==1) {
			Operation lop = lref.get(0);
			Operation rop = rref.get(0);
			if (rop.op == Operation.OpType.INDEX && rop.args[1].equals(var)) {
				return false;
			}
			if (rop.op == Operation.OpType.MOVI && rop.args[0].equals(var)) {
				return false;
			}
			if (lop.op == Operation.OpType.MOV && rop.op == Operation.OpType.MOV) {
				ArrayList<Operation> sub = new ArrayList<>();
				sub.addAll(code.subList(Math.min(code.indexOf(rop)-1,code.indexOf(lop)+1), Math.max(code.indexOf(rop)-1,code.indexOf(lop)+1)));
				sub = AssemblyUtils.getReferences(ad.pkg, sub, rop.args[0]);
				if (sub.isEmpty()) {
					return false;
				}
			}
		}
		return lref.size()<2 && rref.size()<2;
	}
	
	public static String getInlineString(AssemblyData ad, ArrayList<Operation> code, String var) {
		Field f = ad.pkg.getField(var);
		if (f!=null) {
			if (f.onCompile==null) {
				return var;
			} else {
				return f.onCompile.compile(ad.pkg, true, ad);
			}
		}
		if (isInlinable(ad, code, var)) {
			ArrayList<Operation> a = AssemblyUtils.getLReferences(ad.pkg, code, var);
			if (a.isEmpty()) {
				return var;
			}
			return assembleExpression(ad, code, a.get(0));
		} else {
			return var;
		}
	}
	
	public static boolean canRemove(AssemblyData ad, ArrayList<Operation> ops, Operation op, Stack<AssembleVarSpace> vs) {
		if (!op.op.hasLVar()) {
			return false;
		}
		if (AssemblyUtils.getRReferences(ad.pkg, ops, op.args[0]).isEmpty()) {
			return false;
		}
		return isInlinable(ad, ops, op.args[0]);
//		return false;
	}
	
	public static boolean ditchLValue(AssemblyData ad, ArrayList<Operation> ops, Operation op) {
		if (Directives.has(ad.dirs, "!optimize")) {
			return false;
		}
		if (!op.op.hasLVar()) {
			return false;
		}
		if (ad.pkg.getField(op.args[0])!=null) {
			return false;
		}
		if (AssemblyUtils.getReferences(ad.pkg, ops, op.args[0]).size()<=1) {
			return true;
		}
		return false;
	}
	
	public static boolean shouldIncludeFunction(Function fn) {
		return fn.isCompiled() && !fn.isLibrary() && !Directives.has(fn, "inline") && !Directives.has(fn, "native");
	}
	
	public static boolean shouldIncludeField(Field fn) {
		return !fn.isLibrary() && !Directives.has(fn, "inline") && !Directives.has(fn, "native");
	}
}
