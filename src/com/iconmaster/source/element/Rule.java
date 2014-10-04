package com.iconmaster.source.element;

import com.iconmaster.source.element.ISpecialRule.RuleResult;
import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.tokenize.CompoundTokenRule;
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
	DIR(null,(a,i)->{ //local directive rule
		if (i==a.size() || a.get(i).type != TokenRule.DIRECTIVE || ((Token)a.get(i)).string().startsWith("@")) {
			return null;
		}
		Token dir = ((Token)a.get(i));
		Element e = a.get(i+1);
		e.directives.addAll(dir.directives);
		e.directives.add(dir.string());
		return new RuleResult(null,1);
	}),
	RECURSE(null,(a,i)->{ //parse the stuff in parens too!
		if (a.get(i).args[9]==null && a.get(i).type instanceof CompoundTokenRule) {
			a.get(i).args[0] = Parser.parse((ArrayList<Element>)a.get(i).args[0]);
			a.get(i).args[9] = true;
			return new RuleResult(null,0);
		}
		return null;
	}),
	FCALL("F","w0!p1?"),
	ICALL("I","w0!i1?"),
	CAST(null,(a,i)->{
		if (i>a.size()-2 || a.get(i+1).type != TokenRule.RESWORD || !((Token)a.get(i+1)).string().equals("as")) {
			return null;
		}
		Element e1 = a.get(i);
		Element e2 = a.get(i+2);
		e1.dataType = e2;
		return new RuleResult(e1,3);
	}),
		CHAIN("C", new com.iconmaster.source.element.ISpecialRule() {
		@Override
		public RuleResult match(ArrayList<Element> a, int i) {
			if (i+3>a.size() || !(a.get(i+1)).args[0].equals(".")) {
				return null;
			}
			Element e1 = a.get(i);
			Element e2 = a.get(i+2);
			
			ArrayList ea;
			if (e1.type==Rule.CHAIN && e1.args[0] instanceof ArrayList) {
				ea = (ArrayList) e1.args[0];
			} else {
				ea = new ArrayList();
				ea.add(e1);
			}
			ea.add(e2);
			Element res = new Element(Range.from(e1.range, e2.range),Rule.CHAIN);
			res.args[0] = ea;
			return new RuleResult(res,3);
		}
	}),
	MUL(null,"a@0'*'a@1"),
	DIV(null,"a@0'/'a@1"),
	ADD(null,"a@0'+'a@1"),
	NEG("_","'-'a@0"),
	SUB(null,(a,i)->{
		if (i+2>a.size() || a.get(i).type==TokenRule.SYMBOL || a.get(i).type==TokenRule.RESWORD || a.get(i+1).type!=Rule.NEG) {
			return null;
		}
		Element e = new Element(Range.from(a.get(i).range,a.get(i+1).range),Rule.SUB);
		e.args[0] = a.get(i);
		e.args[1] = a.get(i+1).args[0];
		return new RuleResult(e, 2);
	}),
	TUPLE("T", new com.iconmaster.source.element.ISpecialRule() {
		@Override
		public RuleResult match(ArrayList<Element> a, int i) {
			if (i+3>a.size() || !(a.get(i+1)).args[0].equals(",")) {
				return null;
			}
			Element e1 = a.get(i);
			Element e2 = a.get(i+2);
			
			ArrayList ea;
			if (e1.type==Rule.TUPLE && e1.args[0] instanceof ArrayList) {
				ea = (ArrayList) e1.args[0];
			} else {
				ea = new ArrayList();
				ea.add(e1);
			}
			ea.add(e2);
			Element res = new Element(Range.from(e1.range, e2.range),Rule.TUPLE);
			res.args[0] = ea;
			return new RuleResult(res,3);
		}
	}),
	LOCAL("L","'local'!t0?"),
	LOCAL_ASN(null,"L0!?'='t1"),
	FIELD("G","'field'!t0?"),
	FIELD_ASN(null,"G0!?'='t1"),
	ASSIGN("=","t0'='t1"),
	FUNC(null,"'function'!F01?c2"),
	PROPFUNC(null,"'function'!w0?c2"),
	SEP(null,(a,i)->{
		if (a.get(i).type != TokenRule.SEP) {
			return null;
		}
		return new RuleResult(null,1);
	}),
	UNRECURSE(null,(a,i)->{
		if (a.get(i).args[9]!=null && a.get(i).type instanceof CompoundTokenRule) {
			a.get(i).args[9] = null;
			return new RuleResult(null,0);
		}
		return null;
	});
	
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
				if ((clauses[j].literal && v.type instanceof TokenRule && ((Token)v).string().equals(clauses[j].toMatch)) || (v.type==Parser.getAlias(clauses[j].toMatch)) || (clauses[j].toMatch.equals("a")) || (clauses[j].toMatch.equals("t"))) {
					if (clauses[j].toMatch.equals("t") && v.type!=Rule.TUPLE) {
						if (v.type==CompoundTokenRule.PAREN) {
							v = ((Token)v).array().get(0);
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
}
