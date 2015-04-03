package com.iconmaster.source.prototype;

import com.iconmaster.source.exception.SourceError;
import com.iconmaster.source.parse.Token;
import com.iconmaster.source.parse.TokenType;
import com.iconmaster.source.util.Result;
import com.iconmaster.source.util.TokenUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public class Prototyper {
	public static class PrototyperContext {
		public SourcePackage root;
		public SourcePackage pkg;
		public ArrayList<String> dirs = new ArrayList<>();
		public ArrayList<SourceError> errs = new ArrayList<>();

		public PrototyperContext(SourcePackage root) {
			this.root = root;
			this.pkg = root;
		}
	}
	
	public static Result<SourcePackage> prototype(Token code) {
		SourcePackage pkg = new SourcePackage(code.range);
		PrototyperContext ctx = new PrototyperContext(pkg);
		prototype(code, ctx);
		if (ctx.errs.isEmpty()) {
			return new Result<>(pkg);
		} else {
			return new Result<>(ctx.errs.toArray(new SourceError[0]));
		}
	}
	
	public static void prototype(Token code, PrototyperContext ctx) {
		switch (code.type) {
			case STATEMENT:
				prototype(code.l,ctx);
				prototype(code.r,ctx);
				break;
			case PACKAGE:
				List<Token> names = TokenUtils.getTokens(code.l, TokenType.LINK);
				for (Token t : names) {
					if (t.type==TokenType.WORD) {
						ctx.pkg = ctx.pkg.getPackage(t.data, code.range);
					} else {
						ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, t.range, "Token of type "+t.type+" not allowed in package name"));
					}
				}
				ctx.dirs.clear();
				break;
			case PACKAGE_BLOCK:
				names = TokenUtils.getTokens(code.l, TokenType.LINK);
				SourcePackage oldPkg = ctx.pkg;
				for (Token t : names) {
					if (t.type==TokenType.WORD) {
						ctx.pkg = ctx.pkg.getPackage(t.data, code.range);
					} else {
						ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, t.range, "Token of type "+t.type+" not allowed in package name"));
					}
				}
				prototype(code.r,ctx);
				ctx.pkg = oldPkg;
				ctx.dirs.clear();
				break;
			case IMPORT: {
				List<Token> tokens = TokenUtils.getTokens(code.l, TokenType.TUPLE);
				for (Token t : tokens) {
					List<Token> tokens2 = TokenUtils.getTokens(t, TokenType.LINK);
					List<String> a = new ArrayList<>();
					for (Token t2 : tokens2) {
						if (t2.type==TokenType.WORD) {
							a.add(t2.data);
						} else {
							ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, t2.range, "Token of type "+t2.type+" not allowed in package name"));
						}
					}
					ctx.pkg.rawImports.add(a);
				}
				break;
			} case FUNCTION:
				Function fn = new Function(code.r);
				fn.range = code.range;
				fn.dirs.addAll(ctx.dirs);
				prototypeFunction(code.l, fn, ctx);
				ctx.dirs.clear();
				break;
			case FIELD:
				List<Token> tokens;
				if (code.l.type==TokenType.ASSIGN) {
					tokens = TokenUtils.getTokens(code.l.l, TokenType.TUPLE);
					ctx.pkg.rawFieldValues.add(code.l);
				} else {
					tokens = TokenUtils.getTokens(code.l, TokenType.TUPLE);
				}
				
				for (Token t : tokens) {
					Field f = new Field();
					f.range = t.range;
					f.dirs.addAll(ctx.dirs);
					prototypeField(t, f, ctx);
				}
				ctx.dirs.clear();
				break;
			case GLOBAL_DIR:
				ctx.pkg.dirs.add(code.data);
				break;
			case LOCAL_DIR:
				ctx.dirs.add(code.data);
				prototype(code.l,ctx);
				break;
			default:
				ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, code.range, "Token of type "+code.type+" not allowed in global scope"));
				break;
		}
	}
	
	public static void prototypeFunction(Token code, Function fn, PrototyperContext ctx) {
		switch (code.type) {
			case LINK:
				SourcePackage oldPkg = ctx.pkg;
				List<Token> tokens = TokenUtils.getTokens(code, TokenType.LINK);
				Token last = tokens.get(tokens.size()-1);
				if (last.type!=TokenType.FCALL) {
					ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, last.range, "Token of type "+last.type+" is not a function definition"));
				} else {
					tokens.remove(last);
					for (Token t : tokens) {
						if (t.type==TokenType.WORD) {
							ctx.pkg = ctx.pkg.getPackage(t.data, code.range);
						} else {
							ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, t.range, "Token of type "+t.type+" not allowed in function name"));
						}
					}
					
					prototypeFunction(last, fn, ctx);
				}
				ctx.pkg = oldPkg;
				break;
			case FCALL:
				fn.name = code.data;
				
				List<Token> args = TokenUtils.getTokens(code.l, TokenType.TUPLE);
				for (Token t : args) {
					Field arg = new Field();
					arg.range = t.range;
					prototypeFuncArg(t, arg, ctx);
					fn.rawArgs.add(arg);
				}
				
				ctx.pkg.addFunction(fn);
				break;
			case AS:
				fn.rawReturnType = code.r;
				prototypeFunction(code.l, fn, ctx);
				break;
			default:
				ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, code.range, "Token of type "+code.type+" not allowed in function definition"));
		}
	}
	
	public static void prototypeFuncArg(Token code, Field f, PrototyperContext ctx) {
		switch (code.type) {
			case WORD:
				f.name = code.data;
				break;
			case AS:
				f.rawDataType = code.r;
				prototypeFuncArg(code.l, f, ctx);
				break;
			case LOCAL_DIR:
				f.dirs.add(code.data);
				prototypeFuncArg(code.l, f, ctx);
				break;
			default:
				ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, code.range, "Token of type "+code.type+" not allowed in function definition arguments"));
		}
	}
	
	public static void prototypeField(Token code, Field f, PrototyperContext ctx) {
		switch (code.type) {
			case LINK:
				SourcePackage oldPkg = ctx.pkg;
				List<Token> tokens = TokenUtils.getTokens(code, TokenType.LINK);
				Token last = tokens.get(tokens.size()-1);
				if (last.type!=TokenType.WORD) {
					ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, last.range, "Token of type "+last.type+" not allowed in field name"));
				} else {
					tokens.remove(last);
					for (Token t : tokens) {
						if (t.type==TokenType.WORD) {
							ctx.pkg = ctx.pkg.getPackage(t.data, code.range);
						} else {
							ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, t.range, "Token of type "+t.type+" not allowed in field name"));
						}
					}
					
					prototypeField(last, f, ctx);
				}
				ctx.pkg = oldPkg;
				break;
			case LOCAL_DIR:
				f.dirs.add(code.data);
				prototypeField(code.l, f, ctx);
				break;
			case AS:
				f.rawDataType = code.r;
				prototypeField(code.l, f, ctx);
				break;
			case WORD:
				f.name = code.data;
				ctx.pkg.addField(f);
				break;
			default:
				ctx.errs.add(new SourceError(SourceError.ErrorType.SYNTAX, code.range, "Token of type "+code.type+" not allowed in field definition"));
		}
	}
}
