package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.link.platform.hppl.InlinedExpression.InlineOp;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class InlinedExpression extends ArrayList<InlineOp> {
	public static enum SpecialOp {
		CALL_IFN;
	}
	
	public static enum Status {
		KEEP_NO_LVAL, INLINE, KEEP, NOT_APPLICABLE
	}
	
	public static class InlineOp {
		public Operation op;
		public Status status = null;
		public int refs = 0;
		public SpecialOp spec;
		public int matchingBlock = -1;

		public InlineOp(Operation op) {
			this.op = op;
		}
		
		public InlineOp(Operation op, SpecialOp spec) {
			this.op = op;
			this.spec = spec;
		}
		
		public void addRef() {
			refs++;
		}
		
		public void setStatus() {
			if (!op.op.hasLVar()) {
				status = Status.NOT_APPLICABLE;
			} else if (refs==0) {
				status = Status.KEEP_NO_LVAL;
			} else if (refs==1 && op.op!=OpType.MOVA && op.op!=OpType.MOVL) {
				status = Status.INLINE;
			} else {
				status = Status.KEEP;
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("<");
			sb.append(refs);
			sb.append("-");
			sb.append(status);
			if (spec!=null) {
				sb.append(" ");
				sb.append(spec);
			}
			if (matchingBlock!=-1) {
				sb.append("(loop ");
				sb.append(matchingBlock);
				sb.append(")");
			}
			sb.append("> ");
			sb.append(op);
			return sb.toString();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (InlineOp op : this) {
			sb.append(op);
			sb.append("\n");
		}
		return sb.toString();
	}
}
