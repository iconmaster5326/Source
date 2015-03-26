package com.iconmaster.source.util;

/**
 *
 * @author iconmaster
 */
public class Range {
	public int low;
	public int high;

	public Range(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	public static Range from(Range r1, Range r2) {
		return new Range(r1.low,r2.high);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + this.low;
		hash = 97 * hash + this.high;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Range other = (Range) obj;
		if (this.low != other.low) {
			return false;
		}
		return this.high == other.high;
	}

	@Override
	public String toString() {
		return low+"~"+high;
	}
}
