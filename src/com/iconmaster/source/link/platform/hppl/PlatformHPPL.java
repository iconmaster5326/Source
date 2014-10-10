package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.assemble.AssemblyUtils;
import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.link.Platform;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.Variable;
import java.util.ArrayList;

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
		for (Variable var : pkg.getVariables()) {
			if (var.isCompiled()) {
				sb.append(var.getName());
				sb.append(";");
			}
		}
		sb.append("\n");
		for (Function fn : pkg.getFunctions()) {
			if (fn.isCompiled()) {
				sb.append(fn.getName());
				sb.append("();");
			}
		}
		sb.append("\n\n");
		for (Variable var : pkg.getVariables()) {
			if (var.isCompiled()) {
				sb.append(assembleField(pkg,var));
			}
		}
		sb.append("\n\n");
		for (Function fn : pkg.getFunctions()) {
			if (fn.isCompiled()) {
				sb.append(assembleFunction(pkg,fn));
			}
		}
		return sb.toString();
	}
	
	private String assembleFunction(SourcePackage pkg, Function fn) {
		StringBuilder sb = new StringBuilder();
		sb.append(fn.getName());
		sb.append("()\nBEGIN\n ");
		if (fn.getCode()!=null) {
			sb.append(assembleCode(pkg,fn.getCode()).replace("\n", "\n "));
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("\nEND;\n\n");
		return sb.toString();
	}
	
	private String assembleField(SourcePackage pkg, Variable var) {
		StringBuilder sb = new StringBuilder();
		if (var.getValue()!=null) {
			sb.append(assembleCode(pkg,var.getValue()));
		} else {
			sb.append(var.getName());
		}
		return sb.toString();
	}
	
	private String assembleCode(SourcePackage pkg, ArrayList<Operation> expr) {
		StringBuilder sb = new StringBuilder();
		for (Operation op : expr) {
			switch (op.op) {
				case MOVN:
				case MOV:
					addLocal(pkg,expr,op,sb);
					sb.append(op.args[0]);
					sb.append(":=");
					sb.append(op.args[1]);
					break;
				case ADD:
					addLocal(pkg,expr,op,sb);
					sb.append(op.args[0]);
					sb.append(":=");
					sb.append(op.args[1]);
					sb.append("+");
					sb.append(op.args[2]);
					break;
			}
			sb.append(";\n");
		}
		return sb.toString();
	}
	
	public void addLocal(SourcePackage pkg, ArrayList<Operation> code, Operation thisOp, StringBuilder sb) {
		if (AssemblyUtils.isFirstRef(pkg, code, thisOp, thisOp.args[0])) {
			sb.append("LOCAL ");
		}
	}
}
