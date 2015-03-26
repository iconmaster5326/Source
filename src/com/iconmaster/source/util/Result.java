package com.iconmaster.source.util;

import com.iconmaster.source.exception.SourceError;
import java.util.Arrays;

/**
 *
 * @author iconmaster
 */
public class Result<T> {
	public boolean failed;
	public T item;
	public SourceError[] errors;

	public Result(T item) {
		failed = false;
		this.item = item;
	}

	public Result(SourceError... errors) {
		this.failed = true;
		this.errors = errors;
	}

	@Override
	public String toString() {
		return "Result{" + "failed=" + failed + ", item=" + item + ", errors=" + Arrays.toString(errors) + '}';
	}
}
