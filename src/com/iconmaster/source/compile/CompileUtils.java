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
	
	public static abstract class IteratorTransformer implements CodeTransformer {

		public Iterator iterator;

		public IteratorTransformer(Iterator function) {
			this.iterator = function;
		}

		@Override
		public ArrayList<Operation> transform(SourcePackage pkg, Object work, ArrayList<Operation> code) {
			ArrayList<Operation> a = new ArrayList<>();
			ArrayList<Operation> iterCode = null;
			ArrayList<Operation> forCode = null;
			ArrayList<Operation> doCode = null;
			String[] forVars = null;
			Operation doOp = null;
			int depth = 0;
			for (Operation op : code) {
				boolean add = true;
				if (op.op==OpType.DO && depth==0) {
					add = false;
					doOp = op;
					doCode = new ArrayList<>();
				} else if (op.op==OpType.ITER && depth==0) {
					add = false;
					doOp = null;
					a.addAll(doCode);
					doCode = null;

					String iterName = op.args[0];
					Iterator iter = pkg.getIterator(iterName);
					if (op.args[0].equals(iterator.getFullName())) {
						iterCode = iter.getCode();
						forCode = new ArrayList<>();
					}
				} else if (op.op==OpType.FOR) {
					if (depth==0 && forCode != null) {
						add = false;

						forVars = op.args;
					}
					depth++;
				} else if (op.op.isBlockStarter() && depth==0 && doOp!=null) {
					a.add(doOp);
					a.addAll(doCode);
					doOp = null;
					doCode = null;
				} else if (op.op==OpType.ENDB) {
					depth--;
					if (depth==0) {
						add = false;

						a.addAll(transform(pkg,work,onCall(pkg, work, forVars, forCode)));
					}
				}

				if (add) {
					if (doOp!=null) {
						doCode.add(op);
					} else if (depth==0) {
						a.add(op);
					} else {
						forCode.add(op);
					}
				}
			}
			return a;
		}

		public abstract ArrayList<Operation> onCall(SourcePackage pkg, Object workingOn, String[] vars, ArrayList<Operation> forBlock);
	}
	
	public static final CodeTransformer iteratorReplacer = new CodeTransformer() {
		@Override
		public ArrayList<Operation> transform(SourcePackage pkg, Object work, ArrayList<Operation> code) {
			ArrayList<Operation> a = new ArrayList<>();
			ArrayList<Operation> iterCode = null;
			ArrayList<Operation> forCode = null;
			ArrayList<Operation> doCode = null;
			String[] forVars = null;
			Operation doOp = null;
			int depth = 0;
			for (Operation op : code) {
				boolean add = true;
				if (op.op==OpType.DO && depth==0) {
					add = false;
					doOp = op;
					doCode = new ArrayList<>();
				} else if (op.op==OpType.ITER && depth==0) {
					add = false;
					doOp = null;
					a.addAll(doCode);
					doCode = null;
					
					String iterName = op.args[0];
					Iterator iter = pkg.getIterator(iterName);
					if (iter.shouldInline && iter.getCode()!=null) {
						iterCode = iter.getCode();
						forCode = new ArrayList<>();
					}
				} else if (op.op==OpType.FOR) {
					if (depth==0 && forCode != null) {
						add = false;

						forVars = op.args;
					}
					depth++;
				} else if (op.op.isBlockStarter() && depth==0 && doOp!=null) {
					a.add(doOp);
					a.addAll(doCode);
					doOp = null;
					doCode = null;
				} else if (op.op==OpType.ENDB) {
					depth--;
					if (depth==0) {
						add = false;

						for (Operation op2 : iterCode) {
							if (op2.op==OpType.RET) {
								String[] rets = op2.args;

								int i = 0;
								for (String var : forVars) {
									a.add(new Operation(OpType.MOV, (TypeDef) null, op2.range, var, rets[i]));
									i++;
								}
								a.addAll(iteratorReplacer.transform(pkg, work, forCode));
							} else {
								a.add(op2);
							}
						}
					}
				}

				if (add) {
					if (doOp!=null) {
						doCode.add(op);
					} else if (depth==0) {
						a.add(op);
					} else {
						forCode.add(op);
					}
				}
			}
			return a;
		}
	};

	public static final CodeTransformer gotoReplacer = (pkg, workingOn, code) -> {
		code = iteratorReplacer.transform(pkg, workingOn, code);
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
		if (code==null) {
			return null;
		}
		
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
						String step;
						if (forOp.args.length<=4) {
							step = pkg.nameProvider.getTempName();
							old.add(new Operation(OpType.MOVN, forOp.type, null, step, "1"));
						} else {
							step = forOp.args[4];
						}
						old.add(new Operation(OpType.CALL, forOp.type, null, forOp.args[0], forOp.type.name+"._add", forOp.args[0], step)); //TODO: Make this not use a direct name
						String temp = pkg.nameProvider.getTempName();
						old.add(new Operation(OpType.CALL, TypeDef.BOOLEAN, null, temp, forOp.type.name+"._gt", forOp.args[0], forOp.args[3])); //TODO: Make this not use a direct name
						old.add(new Operation(OpType.GOTOF, temp, forLabel));
						a = old;
						forBlock = null;
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
//				case FORR:
//					if (isWhile || isRep || isIf || isFor) {
//						a.add(op);
//						break;
//					}
//					isFor = true;
//					forBlock = new ArrayList<>();
//					forOp = op;
//					if (doBlock!=null) {
//						old.addAll(replaceWithGotos(pkg, doBlock));
//					}
//					doBlock = null;
//					forLabel = pkg.nameProvider.getTempName();
//					old.add(new Operation(OpType.MOV, op.type, op.range, op.args[0], op.args[2]));
//					old.add(new Operation(OpType.LABEL, forLabel));
//					a = forBlock;
//					break;
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
