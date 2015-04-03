package com.iconmaster.source.prototype;

import com.iconmaster.source.parse.Token;
import com.iconmaster.source.parse.TokenType;
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

		public PrototyperContext(SourcePackage root) {
			this.root = root;
			this.pkg = root;
		}
	}
	
	public static SourcePackage prototype(Token code) {
		SourcePackage pkg = new SourcePackage(code.range);
		prototype(code, new PrototyperContext(pkg));
		return pkg;
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
						//error
					}
				}
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
							//error
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
				//error
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
					//error
				} else {
					tokens.remove(last);
					for (Token t : tokens) {
						if (t.type==TokenType.WORD) {
							ctx.pkg = ctx.pkg.getPackage(t.data, code.range);
						} else {
							//error
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
				//error
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
				//error
		}
	}
	
	public static void prototypeField(Token code, Field f, PrototyperContext ctx) {
		switch (code.type) {
			case LINK:
				SourcePackage oldPkg = ctx.pkg;
				List<Token> tokens = TokenUtils.getTokens(code, TokenType.LINK);
				Token last = tokens.get(tokens.size()-1);
				if (last.type!=TokenType.WORD) {
					//error
				} else {
					tokens.remove(last);
					for (Token t : tokens) {
						if (t.type==TokenType.WORD) {
							ctx.pkg = ctx.pkg.getPackage(t.data, code.range);
						} else {
							//error
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
				//error
		}
	}
}
