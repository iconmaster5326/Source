package com.iconmaster.source.element;

import com.iconmaster.source.element.ISpecialRule.RuleResult;
import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.tokenize.Token;
import com.iconmaster.source.tokenize.TokenRule;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author iconmaster
 */
public enum Rule implements IElementType {
	PAREN("p",(a,i)->{
		if (a.get(i).type==TokenRule.SYMBOL && "(".equals(a.get(i).args[0])) {
			int j = i;
			int depth = 1;
			ArrayList<Element> a2 = new ArrayList<>();
			Range r1 = a.get(i).range;
			while (true) {
				j++;
				if (j>a.size()) {
					return null;
				}
				if (a.get(j).type==TokenRule.SYMBOL && "(".equals(a.get(j).args[0])) {
					depth++;
				}
				if (a.get(j).type==TokenRule.SYMBOL && ")".equals(a.get(j).args[0])) {
					depth--;
				}
				if (depth==0) {
					Element e = new Element(Range.from(r1, a.get(j).range),Rule.PAREN);
					ArrayList<Element> a3 = Parser.parse((ArrayList<Element>) a2.clone());
					e.args[0] = a3;
					return new RuleResult(e,a2.size()+2);
				}
				a2.add(a.get(j));
			}
		}
		return null;
	}),
	INDEX("i",(a,i)->{
		if (a.get(i).type==TokenRule.SYMBOL && "[".equals(a.get(i).args[0])) {
			int j = i;
			int depth = 1;
			ArrayList<Element> a2 = new ArrayList<>();
			Range r1 = a.get(i).range;
			while (true) {
				j++;
				if (j>a.size()) {
					return null;
				}
				if (a.get(j).type==TokenRule.SYMBOL && "[".equals(a.get(j).args[0])) {
					depth++;
				}
				if (a.get(j).type==TokenRule.SYMBOL && "]".equals(a.get(j).args[0])) {
					depth--;
				}
				if (depth==0) {
					Element e = new Element(Range.from(r1, a.get(j).range),Rule.INDEX);
					ArrayList<Element> a3 = Parser.parse((ArrayList<Element>) a2.clone());
					e.args[0] = a3;
					return new RuleResult(e,a2.size()+2);
				}
				a2.add(a.get(j));
			}
		}
		return null;
	}),
	CODE("c",(a,i)->{
		if (a.get(i).type==TokenRule.SYMBOL && "{".equals(a.get(i).args[0])) {
			int j = i;
			int depth = 1;
			ArrayList<Element> a2 = new ArrayList<>();
			Range r1 = a.get(i).range;
			while (true) {
				j++;
				if (j>a.size()) {
					return null;
				}
				if (a.get(j).type==TokenRule.SYMBOL && "{".equals(a.get(j).args[0])) {
					depth++;
				}
				if (a.get(j).type==TokenRule.SYMBOL && "}".equals(a.get(j).args[0])) {
					depth--;
				}
				if (depth==0) {
					Element e = new Element(Range.from(r1, a.get(j).range),Rule.CODE);
					ArrayList<Element> a3 = Parser.parse((ArrayList<Element>) a2.clone());
					e.args[0] = a3;
					return new RuleResult(e,a2.size()+2);
				}
				a2.add(a.get(j));
			}
		}
		return null;
	}),
	DIR(null,(a,i)->{ //local directive rule
		ArrayList<String> dirs = new ArrayList<>();
		int j = i;
		while (j < a.size()) {
			if (a.get(j).type == TokenRule.DIRECTIVE && !((Token)a.get(j)).string().startsWith("@")) {
				dirs.add((String) a.get(j).args[0]);
			} else {
				a.get(j).getDirectives().addAll(dirs);
				return new RuleResult(null, dirs.size());
			}
			j++;
		}
		return null;
	}),
	GLOBAL_DIR(null,"r0"),
	FCALL("F","w0!p1?"),
	ICALL("I","w0!i1?"),
	FCALL2(null,"I02!p1?"),
	FCALL_HAX(null,(a,i)->{
		if (a.get(i).type!=FCALL2) {
			return null;
		}
		Element e = new Element(a.get(i).range, FCALL);
		e.args[0] = a.get(i).args[0];
		e.args[1] = a.get(i).args[1];
		e.args[2] = a.get(i).args[2];
		e.directives.addAll(a.get(i).directives);
		e.dataType = a.get(i).dataType;
		return new RuleResult(e, 1);
	}),
	TO(null,"a@0'to'a@1"),
	CAST(null,(a,i)->{
		if (i>a.size()-2 || a.get(i+1).type != TokenRule.RESWORD || !((Token)a.get(i+1)).string().equals("as")) {
			return null;
		}
		Element e1 = a.get(i);
		Element e2 = a.get(i+2);
		e1.dataType = e2;
		return new RuleResult(e1,3);
	}),
	EXTEND("E","a@0'extends'a@1"),
	CHAIN("C", new com.iconmaster.source.element.ISpecialRule() {
		@Override
		public RuleResult match(ArrayList<Element> a, int i) {
			if (i+2>a.size() || a.get(i+1).type!=TokenRule.SYMBOL || !".".equals(a.get(i+1).args[0])) {
				return null;
			}
			int j = i;
			ArrayList<Element> a2 = new ArrayList<>();
			Range r1 = a.get(i).range;
			while (true) {
				a2.add(a.get(j));
				if (j+2>a.size() || a.get(j+1).type!=TokenRule.SYMBOL || !".".equals(a.get(j+1).args[0])) {
					Element e = new Element(Range.from(r1, a.get(j).range),Rule.CHAIN);
					e.args[0] = a2;
					return new RuleResult(e,a2.size()*2-1);
				}
				j+=2;
			}
		}
	}),
	TRUE(null,"'true'?!"),
	FALSE(null,"'false'?!"),
	NOT(null,"'not'a@0"),
	BIT_NOT(null,"'!'a@0"),
	POW(null,"a@0'^'a@1"),
	MUL(null,"a@0'*'a@1"),
	DIV(null,"a@0'/'a@1"),
	MOD(null,"a@0'%'a@1"),
	ADD(null,"a@0'+'a@1"),
	NEG("_","'-'a@0"),
	SUB(null,"A@0_1"),
	CONCAT(null,"a@0'~'a@1"),
	SLL(null,"a@0'<<'a@1"),
	SRL(null,"a@0'>>'a@1"),
	SRA(null,"a@0'>>>'a@1"),
	BIT_AND(null,"a@0'&'a@1"),
	BIT_OR(null,"a@0'|'a@1"),
	RAW_EQ(null,"a@0'==='a@1"),
	EQ(null,"a@0'=='a@1"),
	NEQ(null,"a@0'!='a@1"),
	LT(null,"a@0'<'a@1"),
	GT(null,"a@0'>'a@1"),
	LTE(null,"a@0'<='a@1"),
	GTE(null,"a@0'>='a@1"),
	AND(null,"a@0'and'a@1"),
	OR(null,"a@0'or'a@1"),
	TUPLE("T", new com.iconmaster.source.element.ISpecialRule() {
		@Override
		public RuleResult match(ArrayList<Element> a, int i) {
			if (i+2>a.size() || a.get(i+1).type!=TokenRule.SYMBOL || !",".equals(a.get(i+1).args[0])) {
				return null;
			}
			int j = i;
			ArrayList<Element> a2 = new ArrayList<>();
			Range r1 = a.get(i).range;
			while (true) {
				a2.add(a.get(j));
				if (j+2>a.size() || a.get(j+1).type!=TokenRule.SYMBOL || !",".equals(a.get(j+1).args[0])) {
					Element e = new Element(Range.from(r1, a.get(j).range),Rule.TUPLE);
					e.args[0] = a2;
					return new RuleResult(e,a2.size()*2-1);
				}
				j+=2;
			}
		}
	}),
	LOCAL("L","'local'!t0?"),
	LOCAL_ASN(null,"L0!?'='t1"),
	FIELD("G","'field'!t0?"),
	FIELD_ASN(null,"G0!?'='t1"),
	ICALL_REF(null,"A@0i1"),
	ADD_ASN(null,"a@0'+='a@1"),
	SUB_ASN(null,"a@0'-='a@1"),
	MUL_ASN(null,"a@0'*='a@1"),
	DIV_ASN(null,"a@0'/='a@1"),
	ASSIGN("=","t0'='t1"),
	IF(null,"'if'!a@0c2"),
	ELSEIF(null,"'elseif'!a@0c2"),
	ELSE(null,"'else'!c2"),
	IFBLOCK(null,(a,i)->{
		if (a.get(i).type!=Rule.IF) {
			return null;
		}
		Element e = new Element(null, IFBLOCK);
		e.args[0] = a.get(i);
		ArrayList<Element> es = new ArrayList<>(); 
		e.args[1] = es;
		Range endr = e.range;
		int n = 1;
		for (int j=i+1;j<a.size();j++) {
			if (a.get(j).type==Rule.ELSEIF) {
				es.add(a.get(j));
				n++;
				endr = a.get(j).range;
			} else if (a.get(j).type==Rule.ELSE) {
				e.args[2] = a.get(j);
				n++;
				endr = a.get(j).range;
			} else {
				break;
			}
		}
		e.range = Range.from(a.get(i).range, endr);
		return new RuleResult(e, n);
	}),
	FOR(null,"'for'!t0'in'a@1c2"),
	WHILE(null,"'while'!a@0c2"),
	REPEAT(null,"'repeat'!c2'until'a@0"),
	RETURN_NULL("R","'return'!"),
	RETURN(null,"R!A@0?"),
	BREAK(null,"'break'!"),
	CONTINUE(null,"'continue'!"),
	PACKAGE(null,"'package'!a@0"),
	IMPORT(null,"'import'!a@0?"),
	FUNC(null,"'function'!F013?c2"),
	ITERATOR(null,"'iterator'!F01?c2"),
	STRUCT(null,"'struct'!w@0c2"),
	STRUCT_EXT(null,"'struct'!E01c2"),
	ENUM(null,"'enum'!w0c1"),
	SEP(null,(a,i)->{
		if (a.get(i).type != TokenRule.SEP) {
			return null;
		}
		return new RuleResult(null,1);
	});

	private boolean isCompound() {
		switch (this) {
			default:
				return false;
		}
	}
	
	public class Clause {
		public String toMatch;
		public int[] toEnter = new int[0];
		public boolean isTyped = false;
		public boolean isDired = false;
		public boolean literal;
		private boolean[] isRef = new boolean[0];

		public Clause(String toMatch, boolean literal) {
			this.toMatch = toMatch;
			this.literal = literal;
		}

		@Override
		public String toString() {
			return "CLAUSE("+toMatch+" "+Arrays.toString(toEnter)+" "+isTyped+" "+isDired+" "+literal+")";
		}
	}
	
	public class ClauseMatcher implements ISpecialRule {
		
		public final Clause[] clauses;
		public final IElementType type;
		
		public ClauseMatcher(Clause[] clauses, IElementType type) {
			this.clauses = clauses;
			this.type = type;
		}

		@Override
		public RuleResult match(ArrayList<Element> a, int i) {
			if (i+clauses.length>a.size()) {
				return null;
			}
			Element e = new Element(null,type);
			Range low = null;
			Range high = null;
			for (int j=0;j<clauses.length;j++) {
				Element v = a.get(i+j);
				if ((clauses[j].literal && v.type instanceof TokenRule && ((Token)v).string().equals(clauses[j].toMatch)) || (v.type==Parser.getAlias(clauses[j].toMatch)) || (clauses[j].toMatch.equals("a")) || (clauses[j].toMatch.equals("t")) || (clauses[j].toMatch.equals("A")) && isValidOperationToken(v)) {
					if (clauses[j].toMatch.equals("t") && v.type!=Rule.TUPLE) {
						if (v.type==Rule.PAREN) {
							Element oldv = v;
							v = new Element(v.range,Rule.TUPLE);
							v.args[0] = oldv.args[0];
						} else {
							ArrayList a2 = new ArrayList();
							a2.add(v);
							v = new Token(v.range,Rule.TUPLE,a2);
						}
					}
					for (int k=0;k<clauses[j].toEnter.length;k++) {
						if (clauses[j].isRef[k]) {
							e.args[clauses[j].toEnter[k]] = v;
						} else {
							e.args[clauses[j].toEnter[k]] = v.args[k];
						}
					}
					if (clauses[j].isTyped) {
						e.dataType = v.dataType;
					}
					if (clauses[j].isDired) {
						e.directives.addAll(v.directives);
					}
					if (j==0) {
						low = v.range;
					}
					high = v.range;
				} else {
					return null;
				}
			}
			e.range=Range.from(low, high);
			return new RuleResult(e,clauses.length);
		}
	}
	
	public final Clause[] clauses;
	public String alias;
	public ISpecialRule rule;
	
	Rule(String alias,String match) {
		List<String> cstr = new ArrayList<>();
		if (match!=null) {
			Matcher m = Pattern.compile("'[^']+'|.").matcher(match);
			while (m.find()) {
				cstr.add(m.group());
			}

			//System.out.println(cstr);

			ArrayList<Clause> a = new ArrayList<>();

			Clause clause = null;
			boolean ref = false;
			for (String line : cstr) {
				if (line.matches("'[^']+'|[^\\d\\!\\?\\@]")) {
					if (clause != null) {
						a.add(clause);
					}
					if (line.startsWith("'")) {
						clause = new Clause(line.substring(1, line.length()-1),true);
					} else {
						clause = new Clause(line,false);
					}
				} else {
					if (clause != null) {
						if (line.matches("\\d")) {
							clause.toEnter = Arrays.copyOf(clause.toEnter, clause.toEnter.length+1);
							clause.isRef = Arrays.copyOf(clause.isRef, clause.isRef.length+1);
							clause.toEnter[clause.toEnter.length-1] = Integer.parseInt(line);
							clause.isRef[clause.isRef.length-1] = ref;
							ref = false;
						} else if (line.equals("!")) {
							clause.isDired = true;
						} else if (line.equals("?")) {
							clause.isTyped = true;
						} else if (line.matches("@")) {
							ref = true;
						}
					}
				}
			}

			if (clause != null) {
				a.add(clause);
			}

			this.clauses = a.toArray(new Clause[0]);
			this.rule = new ClauseMatcher(clauses,this);
		} else {
			this.clauses = new Clause[0];
		}

		this.alias = alias;
	}
	
	Rule(String match) {
		this(null,match);
	}
	
	Rule(String alias, ISpecialRule rule) {
		this(alias,(String)null);
		this.rule = rule;
	}
	
	@Override
	public String getAlias() {
		return alias;
	}
	
	public static boolean isValidOperationToken(Element e) {
		return !(e.type==TokenRule.SYMBOL || e.type==TokenRule.RESWORD || e.type==TokenRule.SEP);
	}
}
