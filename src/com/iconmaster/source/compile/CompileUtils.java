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
		
		boolean isWhile = false;
		String whileBegin = null;
		String whileEnd = null;
		ArrayList<Operation> whileDo = null;
		ArrayList<Operation> whileBlock = null;
		
		for (int i=0;i<code.size();i++) {
			Operation op = code.get(i);
			switch (op.op) {
				case DO:
					isWhile = true;
					whileBegin = pkg.nameProvider.getTempName();
					whileDo = new ArrayList<>();
					a.add(new Operation(OpType.LABEL, op.range, whileBegin));
					a = whileDo;
					break;
				case WHILE:
					whileEnd = pkg.nameProvider.getTempName();
					whileBlock = new ArrayList<>();
					old.addAll(replaceWithGotos(pkg, whileDo));
					String temp = pkg.nameProvider.getTempName();
					old.add(new Operation(OpType.NOT, op.range, temp, op.args[0]));
					old.add(new Operation(OpType.GOTOIF, op.range, temp, whileEnd));
					a = whileBlock;
					break;
				case ENDB:
					if (isWhile) {
						old.addAll(replaceWithGotos(pkg, whileBlock));
						old.add(new Operation(OpType.GOTO, op.range, whileBegin));
						old.add(new Operation(OpType.LABEL, op.range, whileEnd));
						a = old;
						isWhile = false;
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
