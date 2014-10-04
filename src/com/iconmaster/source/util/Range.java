package com.iconmaster.source.util;

/**
 *
 * @author iconmaster
 */
public class Range {
	public final int low;
	public final int high;

	public Range(int low, int high) {
		this.low = Math.min(low, high);
		this.high = Math.max(low, high);
	}

	@Override
	public String toString() {
		return low+"~"+high;
	}
	
	public static Range from(Range r1, Range r2) {
		return new Range(r1==null?0:r1.low,r2==null?0:r2.high);
	}
	
	
}
