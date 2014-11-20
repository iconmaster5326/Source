package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.Iterator;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.TypeDef;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class CompileUtils {

	public static interface CodeTransformer {

		public ArrayList<Operation> transform(SourcePackage pkg, Object workingOn, ArrayList<Operation> code);
	}

	public static abstract class FunctionCallTransformer implements CodeTransformer {

		public Function function;

		public FunctionCallTransformer(Function function) {
			this.function = function;
		}

		@Override
		public ArrayList<Operation> transform(SourcePackage pkg, Object workingOn, ArrayList<Operation> code) {
			ArrayList<Operation> a = new ArrayList<>();
			for (int i = 0; i < code.size(); i++) {
				Operation op = code.get(i);
				if (op.op == OpType.CALL && op.args[1].equals(function.getFullName())) {
					a.addAll(onCall(pkg, workingOn, code, op));
				} else {
					a.add(op);
				}
			}
			return a;
		}

		public abstract ArrayList<Operation> onCall(SourcePackage pkg, Object workingOn, ArrayList<Operation> code, Operation op);
	}

	public static final CodeTransformer forEachReplacer = (pkg, workingOn, code) -> {
		ArrayList<Operation> a = new ArrayList<>();
		for (int i = 0; i < code.size(); i++) {
			Operation op = code.get(i);

			if (op.op == OpType.FORE) {
				String temp = pkg.nameProvider.getTempName();
				String temp2 = pkg.nameProvider.getTempName();
				a.add(new Operation(OpType.CALL, TypeDef.INT, op.range, temp2, "list.size", op.args[0]));
				a.add(new Operation(OpType.FORR, TypeDef.INT, op.range, temp, "1", "1", temp2));
				i++;
				a.add(new Operation(OpType.BEGIN, op.range));
				a.add(new Operation(OpType.INDEX, op.type, op.range, op.args[1], op.args[0], temp));
			} else {
				a.add(op);
			}
		}
		return a;
	};

	public static final CodeTransformer iteratorReplacer = (pkg, work, code) -> {
		ArrayList<Operation> a = new ArrayList<>();
		ArrayList<Operation> old = a;
		Iterator iter = null;
		Operation forOp = null;
		int depth = 0;
		boolean found = false;

		for (int i = 0; i < code.size(); i++) {
			Operation op = code.get(i);

			if (op.op == OpType.FORC) {
				found = true;
				a = new ArrayList<>();
				iter = pkg.getIterator(op.args[0]);
				forOp = op;
				depth++;
			} else if (op.op == OpType.ENDB) {
				if (found) {
					depth--;
					if (depth == 0) {
						ArrayList<Operation> block = a;
						a = old;
						ArrayList<Operation> trans = new ArrayList<>();
						for (int k = 1; k < 1 + iter.getArguments().size(); k++) {
							trans.add(new Operation(OpType.MOV, iter.getArguments().get(k - 1).getName(), forOp.args[k]));
						}
						for (Operation op2 : iter.getCode()) {
							if (op2.op == OpType.RET) {
								int ii = 1 + iter.getArguments().size();
								for (String arg : op2.args) {
									trans.add(new Operation(OpType.MOV, forOp.args[ii], arg));
									ii++;
								}
								trans.addAll(CompileUtils.iteratorReplacer.transform(pkg, work, block));
							} else {
								trans.add(op2);
							}
						}
						old.addAll(trans);
						found = false;
					} else {
						a.add(op);
					}
				} else {
					a.add(op);
				}
			} else if (op.op.isBlockStarter()) {
				if (found) {
					depth++;
				}
				a.add(op);
			} else {
				a.add(op);
			}
		}
		return a;
	};

	public static final CodeTransformer gotoReplacer = (pkg, workingOn, code) -> {
		code = iteratorReplacer.transform(pkg, workingOn, code);
		code = forEachReplacer.transform(pkg, workingOn, code);
		return replaceWithGotos(pkg, code);
	};

	public static void transform(SourcePackage pkg, CodeTransformer ct) {
		for (Function f : pkg.getFunctionsAndIterators()) {
			if (f.getCode() != null) {
				ArrayList<Operation> code = ct.transform(pkg, f, f.getCode());
				f.setCompiled(code);
			}
		}

		for (Field f : pkg.getFields()) {
			if (f.getValue() != null) {
				ArrayList<Operation> code = ct.transform(pkg, f, f.getValue());
				Expression e = new Expression();
				e.addAll(code);
				f.setCompiled(e);
			}
		}
	}

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

		boolean isFor = false;
		ArrayList<Operation> forBlock = null;
		String forLabel = null;
		Operation forOp = null;

		int depth = 0;

		for (int i = 0; i < code.size(); i++) {
			Operation op = code.get(i);
			switch (op.op) {
				case DO:
					if (isWhile || isRep || isIf || isFor) {
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
					if (isWhile || isRep || isIf || isFor) {
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
					if (isWhile || isRep || isIf || isFor) {
						a.add(op);
						break;
					}
					isRep = true;
					repCond = op.args[0];
					repBlock = new ArrayList<>();
					a = repBlock;
					break;
				case ENDB:
					if (depth > 0) {
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
						if (elseBlock != null) {
							old.addAll(replaceWithGotos(pkg, elseBlock));
						} else {
							old.addAll(replaceWithGotos(pkg, ifBlock));
							old.add(new Operation(OpType.LABEL, op.range, elseLabel));
						}
						old.add(new Operation(OpType.LABEL, op.range, endLabel));
						a = old;
						isIf = false;
					}
					if (isFor) {
						old.addAll(replaceWithGotos(pkg, forBlock));
						old.add(new Operation(OpType.ADD, forOp.type, null, forOp.args[0], forOp.args[0], forOp.args.length<4?"1":forOp.args[4]));
						String temp = pkg.nameProvider.getTempName();
						old.add(new Operation(OpType.GT, TypeDef.BOOLEAN, null, temp, forOp.args[0], forOp.args[3]));
						old.add(new Operation(OpType.GOTOF, temp, forLabel));
						a = old;
						isFor = false;
					}
					break;
				case IF:
					if (isWhile || isRep || isIf || isFor) {
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
					if (isWhile || isRep || isIf || isFor) {
						if (depth == 0) {
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
				case FORR:
					if (isWhile || isRep || isIf || isFor) {
						a.add(op);
						break;
					}
					isFor = true;
					forBlock = new ArrayList<>();
					forOp = op;
					old.addAll(replaceWithGotos(pkg, doBlock));
					forLabel = pkg.nameProvider.getTempName();
					old.add(new Operation(OpType.MOV, op.type, op.range, op.args[0], op.args[2]));
					old.add(new Operation(OpType.LABEL, forLabel));
					a = forBlock;
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
