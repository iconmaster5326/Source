package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.assemble.AssemblyUtils;
import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.compile.Operation.OpType;
import static com.iconmaster.source.compile.Operation.OpType.CONCAT;
import static com.iconmaster.source.compile.Operation.OpType.GE;
import static com.iconmaster.source.compile.Operation.OpType.LE;
import com.iconmaster.source.link.Platform;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.Variable;
import com.iconmaster.source.util.Directives;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author iconmaster
 */
public class PlatformHPPL extends Platform {

	public PlatformHPPL() {
		this.name = "HPPL";
		
		this.registerLibrary(new LibraryCore());
	}
	
	@Override
	public String assemble(SourcePackage pkg) {
		StringBuilder sb = new StringBuilder("#pragma mode( separator(.,;) integer(h32) )\n\n");
		for (Function fn : pkg.getFunctions()) {
			if (fn.isCompiled() && !fn.isLibrary() && !Directives.has(fn, "inline")) {
				sb.append(fn.getName());
				sb.append("();");
			}
		}
		sb.append("\n");
		for (Variable var : pkg.getVariables()) {
			if (var.isCompiled()) {
				sb.append(assembleField(pkg,var));
			}
		}
		sb.append("\n");
		for (Function fn : pkg.getFunctions()) {
			if (fn.isCompiled() && !fn.isLibrary() && !Directives.has(fn, "inline")) {
				sb.append(assembleFunction(pkg,fn));
			}
		}
		return sb.toString();
	}
	
	private String assembleFunction(SourcePackage pkg, Function fn) {
		StringBuilder sb = new StringBuilder();
		if (Directives.has(fn, "export")) {
			sb.append("EXPORT ");
		}
		sb.append(fn.getName());
		sb.append("(");
		if (!fn.getArguments().isEmpty()) {
			for (Variable arg : fn.getArguments()) {
				sb.append(arg.getName());
				sb.append(',');
			}
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append(")\nBEGIN\n ");
		if (fn.getCode()!=null) {
			sb.append(assembleCode(pkg,fn.getCode()).replace("\n", "\n "));
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("\nEND;\n");
		return sb.toString();
	}
	
	private String assembleField(SourcePackage pkg, Variable var) {
		StringBuilder sb = new StringBuilder();
		if (Directives.has(var, "export")) {
			sb.append("EXPORT ");
		}
		if (var.getValue()!=null) {
			sb.append(assembleCode(pkg,var.getValue()));
		} else {
			sb.append(var.getName());
		}
		return sb.toString();
	}
	
	private String assembleCode(SourcePackage pkg, ArrayList<Operation> expr) {
		StringBuilder sb = new StringBuilder();
		Stack<Operation> blockOp = new Stack<>();
		Operation lastBlockOp = null;
		for (Operation op : expr) {
			boolean append = true;
			if (canRemove(pkg, expr, op)) {
				append = false;
			} else if (op.op.hasLVar()) {
				
				String s = assembleExpression(pkg, expr, op);
				if (s==null) {
					append = false;
				} else {
					if (!ditchLValue(pkg, expr, op)) {
						addLocal(pkg, expr, op, sb);
						sb.append(op.args[0]);
						sb.append(":=");
					}
					sb.append(s);
				}
			} else {
				switch (op.op) {
					case RET:
						sb.append("RETURN");
						if (op.args.length>0) {
							sb.append(" ");
							sb.append(getInlineString(pkg, expr, op.args[0]));
						}
						break;
					case IF:
						sb.append("IF ");
						sb.append(getInlineString(pkg, expr, op.args[0]));
						sb.append(" THEN\n");
						append = false;
						blockOp.push(op);
						break;
					case ELSE:
						int di = sb.lastIndexOf("END;");
						sb.delete(di-1,di+4);
						sb.append("ELSE\n");
						append = false;
						blockOp.push(op);
						break;
					case WHILE:
						sb.append("WHILE ");
						sb.append(getInlineString(pkg, expr, op.args[0]));
						sb.append(" DO\n");
						append = false;
						blockOp.push(op);
						break;
					case ENDB:
						lastBlockOp = blockOp.pop();
						sb.append("END");
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
	
	private String assembleExpression(SourcePackage pkg, ArrayList<Operation> expr, Operation op) {
		StringBuilder sb = new StringBuilder();
		if (op.op.isMathOp()) {
			sb.append(getInlineString(pkg, expr, op.args[1]));
			sb.append(getMathOp(op.op));
			sb.append(getInlineString(pkg, expr, op.args[2]));
		} else {
			switch (op.op) {
				case MOVN:
				case MOV:
					sb.append(getInlineString(pkg, expr, op.args[1]));
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
							sb.append(op.args[i]);
							sb.append(",");
						}
						sb.deleteCharAt(sb.length()-1);
					}
					sb.append("}");
					break;
				case CALL:
					sb.append(op.args[1]);
					if (op.args.length > 2) {
						sb.append("(");
						for (int i=2;i<op.args.length;i++) {
							sb.append(getInlineString(pkg, expr, op.args[i]));
							sb.append(",");
						}
						sb.deleteCharAt(sb.length()-1);
						sb.append(")");
					}
					break;
				default:
					return null;
			}
		}
		return sb.toString();
	}
	
	public void addLocal(SourcePackage pkg, ArrayList<Operation> code, Operation thisOp, StringBuilder sb) {
		if (AssemblyUtils.isFirstRef(pkg, code, thisOp, thisOp.args[0])) {
			sb.append("LOCAL ");
		}
	}
	
	public static String getMathOp(OpType type) {
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
	
	public static boolean isInlinable(SourcePackage pkg, ArrayList<Operation> code, String var) {
		ArrayList<Operation> lref = AssemblyUtils.getLReferences(pkg, code, var);
		ArrayList<Operation> rref = AssemblyUtils.getRReferences(pkg, code, var);
		if (lref.size()==1 && rref.size()==1) {
			Operation lop = lref.get(0);
			Operation rop = rref.get(0);
			if (lop.op == OpType.MOV && rop.op == OpType.MOV) {
				ArrayList<Operation> sub = new ArrayList<>();
				sub.addAll(code.subList(Math.min(code.indexOf(rop)-1,code.indexOf(lop)+1), Math.max(code.indexOf(rop)-1,code.indexOf(lop)+1)));
				sub = AssemblyUtils.getReferences(pkg, sub, rop.args[0]);
				if (sub.isEmpty()) {
					return false;
				}
			}
		}
		return lref.size()<2 && rref.size()<2;
	}
	
	public String getInlineString(SourcePackage pkg, ArrayList<Operation> code, String var) {
		if (isInlinable(pkg, code, var)) {
			ArrayList<Operation> a = AssemblyUtils.getLReferences(pkg, code, var);
			if (a.isEmpty()) {
				return var;
			}
			return assembleExpression(pkg, code, a.get(0));
		} else {
			return var;
		}
	}
	
	public boolean canRemove(SourcePackage pkg, ArrayList<Operation> ops, Operation op) {
		if (!op.op.hasLVar()) {
			return false;
		}
		if (AssemblyUtils.getRReferences(pkg, ops, op.args[0]).isEmpty()) {
			return false;
		}
		return isInlinable(pkg, ops, op.args[0]);
//		return false;
	}
	
	public boolean ditchLValue(SourcePackage pkg, ArrayList<Operation> ops, Operation op) {
		if (!op.op.hasLVar()) {
			return false;
		}
		if (pkg.getField(op.args[0])!=null) {
			return false;
		}
		if (AssemblyUtils.getReferences(pkg, ops, op.args[0]).size()<=1) {
			return true;
		}
		return false;
	}
}
