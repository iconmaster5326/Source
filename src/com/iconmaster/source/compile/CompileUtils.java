package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class CompileUtils {
	public static ArrayList<Operation> replaceWithGotos(SourcePackage pkg, ArrayList<Operation> code) {
		ArrayList<Operation> a = new ArrayList<>();
		ArrayList<Operation> old = a;
		
		ArrayList<Operation> doBlock = null;
		
		String begin = null;
		String end = null;
		
		boolean isWhile = false;
		ArrayList<Operation> whileBlock = null;
		
		boolean isRep = false;
		String repCond = null;
		ArrayList<Operation> repBlock = null;
		
		boolean isIf = false;
		ArrayList<Operation> ifBlock = null;
		ArrayList<Operation> elseBlock = null;
		String elseLabel = null;
		String endLabel = null;
		
		int depth = 0;
		
		for (int i=0;i<code.size();i++) {
			Operation op = code.get(i);
			switch (op.op) {
				case DO:
					if (isWhile || isRep || isIf) {
						depth++;
						a.add(op);
						break;
					}
					begin = pkg.nameProvider.getTempName();
					doBlock = new ArrayList<>();
					a.add(new Operation(OpType.LABEL, op.range, begin));
					a = doBlock;
					break;
				case WHILE:
					if (isWhile || isRep || isIf) {
						a.add(op);
						break;
					}
					isWhile = true;
					end = pkg.nameProvider.getTempName();
					whileBlock = new ArrayList<>();
					old.addAll(replaceWithGotos(pkg, doBlock));
					old.add(new Operation(OpType.GOTOF, op.range, op.args[0], end));
					a = whileBlock;
					break;
				case REP:
					if (isWhile || isRep || isIf) {
						a.add(op);
						break;
					}
					isRep = true;
					repCond = op.args[0];
					repBlock = new ArrayList<>();
					a = repBlock;
					break;
				case ENDB:
					if (depth>0) {
						depth--;
						a.add(op);
						break;
					}
					if (isWhile) {
						old.addAll(replaceWithGotos(pkg, whileBlock));
						old.add(new Operation(OpType.GOTO, op.range, begin));
						old.add(new Operation(OpType.LABEL, op.range, end));
						a = old;
						isWhile = false;
					}
					if (isRep) {
						old.addAll(replaceWithGotos(pkg, repBlock));
						old.addAll(replaceWithGotos(pkg, doBlock));
						old.add(new Operation(OpType.GOTOF, op.range, repCond, begin));
						a = old;
						isRep = false;
					}
					if (isIf) {
						if (elseBlock!=null) {
							old.addAll(replaceWithGotos(pkg, elseBlock));
						} else {
							old.addAll(replaceWithGotos(pkg, ifBlock));
							old.add(new Operation(OpType.LABEL, op.range, elseLabel));
						}
						old.add(new Operation(OpType.LABEL, op.range, endLabel));
						a = old;
						isIf = false;
					}
					break;
				case IF:
					if (isWhile || isRep || isIf) {
						a.add(op);
						break;
					}
					isIf = true;
					ifBlock = new ArrayList<>();
					elseBlock = null;
					old.addAll(replaceWithGotos(pkg, doBlock));
					elseLabel = pkg.nameProvider.getTempName();
					endLabel = pkg.nameProvider.getTempName();
					old.add(new Operation(OpType.GOTOF, op.range, op.args[0], elseLabel));
					a = ifBlock;
					break;
				case ELSE:
					if (isWhile || isRep || isIf) {
						if (depth==0) {
							old.addAll(replaceWithGotos(pkg, ifBlock));
							old.add(new Operation(OpType.GOTO, op.range, endLabel));
							old.add(new Operation(OpType.LABEL, op.range, elseLabel));
							elseBlock = new ArrayList<>();
							a = elseBlock;
						} else {
							a.add(op);
						}
						break;
					}
					break;
				default:
					a.add(op);
			}
		}
		return a;
	}
	
	public static ArrayList<Operation> replaceBlockScopes(SourcePackage pkg, ArrayList<Operation> code) {
		ArrayList<Operation> a = new ArrayList<>();
		return a;
	}
}