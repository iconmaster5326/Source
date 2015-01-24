package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.link.platform.hppl.InlinedExpression.InlineOp;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class InlinedExpression extends ArrayList<InlineOp> {
	public static enum Status {
		KEEP_NO_LVAL, INLINE, KEEP
	}
	
	public static class InlineOp {
		public Operation op;
		public Status status = null;
		public int refs = 0;

		public InlineOp(Operation op) {
			this.op = op;
		}
		
		public void addRef() {
			refs++;
		}
		
		public void setStatus() {
			if (refs==0) {
				status = Status.KEEP_NO_LVAL;
			} else if (refs==1) {
				status = Status.INLINE;
			} else {
				status = Status.KEEP;
			}
		}
	}
}
