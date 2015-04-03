package com.iconmaster.source.prototype;

import com.iconmaster.source.parse.Token;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public class Function {
	public String name;
	public Token rawCode;
	public Token rawReturnType = null;
	public List<String> dirs = new ArrayList<>();
	public List<Field> rawArgs = new ArrayList<>();

	public Function(String name, Token rawCode) {
		this.name = name;
		this.rawCode = rawCode;
	}

	public Function(Token rawCode) {
		this.rawCode = rawCode;
	}

	public Function() {
		
	}

	@Override
	public String toString() {
		return "Function{" + "name=" + name + ", rawCode=" + rawCode + ", rawReturnType=" + rawReturnType + ", dirs=" + dirs + ", rawArgs=" + rawArgs + '}';
	}
}
