package com.iconmaster.source.parse;

import com.iconmaster.source.util.Range;
import com.iconmaster.source.util.Result;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author iconmaster
 */
public class TokenizerTest {
	
	public TokenizerTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of tokenize method, of class Tokenizer.
	 */
	@Test
	public void testTokenize() {
		System.out.println("===TOKENIZER===");
		
		System.out.println("test 1:");
		String input = "a b c";
		System.out.println("\tInput: '"+input+"'");
		Result<List<Token>> result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals("a", result.item.get(0).data);
		assertEquals(TokenType.WORD, result.item.get(0).type);
		assertEquals(new Range(0,1), result.item.get(0).range);
		assertEquals("b", result.item.get(1).data);
		assertEquals(TokenType.WORD, result.item.get(1).type);
		assertEquals(new Range(2,3), result.item.get(1).range);
		assertEquals("c", result.item.get(2).data);
		assertEquals(TokenType.WORD, result.item.get(2).type);
		assertEquals(new Range(4,5), result.item.get(2).range);
		
		System.out.println("test 2:");
		input = "->`<-";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals("->", result.item.get(0).data);
		assertEquals(TokenType.SYMBOL, result.item.get(0).type);
		assertEquals("`", result.item.get(1).data);
		assertEquals(TokenType.UNKNOWN, result.item.get(1).type);
		assertEquals("<-", result.item.get(2).data);
		assertEquals(TokenType.SYMBOL, result.item.get(2).type);
		
		System.out.println("test 3:");
		input = "1.2 a.b";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(4, result.item.size());
		assertEquals("1.2", result.item.get(0).data);
		assertEquals(TokenType.NUMBER, result.item.get(0).type);
		assertEquals(new Range(0,3), result.item.get(0).range);
		assertEquals("a", result.item.get(1).data);
		assertEquals(TokenType.WORD, result.item.get(1).type);
		assertEquals(new Range(4,4+1), result.item.get(1).range);
		assertEquals(".", result.item.get(2).data);
		assertEquals(TokenType.DOT, result.item.get(2).type);
		assertEquals(new Range(5,5+1), result.item.get(2).range);
		assertEquals("b", result.item.get(3).data);
		assertEquals(TokenType.WORD, result.item.get(3).type);
		assertEquals(new Range(6,6+1), result.item.get(3).range);
		
		System.out.println("test 4:");
		input = "hello\"hello world\"world";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals("hello", result.item.get(0).data);
		assertEquals(TokenType.WORD, result.item.get(0).type);
		assertEquals(new Range(0,0+5), result.item.get(0).range);
		assertEquals("hello world", result.item.get(1).data);
		assertEquals(TokenType.STRING, result.item.get(1).type);
		assertEquals(new Range(5,5+13), result.item.get(1).range);
		assertEquals("world", result.item.get(2).data);
		assertEquals(TokenType.WORD, result.item.get(2).type);
		assertEquals(new Range(18,18+5), result.item.get(2).range);
		
		System.out.println("test 5:");
		input = "a\"\"b";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals("a", result.item.get(0).data);
		assertEquals(TokenType.WORD, result.item.get(0).type);
		assertEquals(new Range(0,0+1), result.item.get(0).range);
		assertEquals("", result.item.get(1).data);
		assertEquals(TokenType.STRING, result.item.get(1).type);
		assertEquals(new Range(1,1+2), result.item.get(1).range);
		assertEquals("b", result.item.get(2).data);
		assertEquals(TokenType.WORD, result.item.get(2).type);
		assertEquals(new Range(3,3+1), result.item.get(2).range);
		
		System.out.println("test 6:");
		input = "((";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(2, result.item.size());
		assertEquals("(", result.item.get(0).data);
		assertEquals(TokenType.LPAREN, result.item.get(0).type);
		assertEquals(new Range(0,0+1), result.item.get(0).range);
		assertEquals("(", result.item.get(1).data);
		assertEquals(TokenType.LPAREN, result.item.get(1).type);
		assertEquals(new Range(1,1+1), result.item.get(1).range);
		
		System.out.println("test 7:");
		input = "[$$$]";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals("[", result.item.get(0).data);
		assertEquals(TokenType.LBRACKET, result.item.get(0).type);
		assertEquals(new Range(0,0+1), result.item.get(0).range);
		assertEquals("$$$", result.item.get(1).data);
		assertEquals(TokenType.SYMBOL, result.item.get(1).type);
		assertEquals(new Range(1,1+3), result.item.get(1).range);
		assertEquals("]", result.item.get(2).data);
		assertEquals(TokenType.RBRACKET, result.item.get(2).type);
		assertEquals(new Range(4,4+1), result.item.get(2).range);
		
		System.out.println("test 8:");
		input = ".03 1.2e5 1.7E5 0.3e-7 1.1E+43";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(5, result.item.size());
		assertEquals(".03", result.item.get(0).data);
		assertEquals(TokenType.NUMBER, result.item.get(0).type);
		assertEquals("1.2e5", result.item.get(1).data);
		assertEquals(TokenType.NUMBER, result.item.get(1).type);
		assertEquals("1.7E5", result.item.get(2).data);
		assertEquals(TokenType.NUMBER, result.item.get(2).type);
		assertEquals("0.3e-7", result.item.get(3).data);
		assertEquals(TokenType.NUMBER, result.item.get(3).type);
		assertEquals("1.1E+43", result.item.get(4).data);
		assertEquals(TokenType.NUMBER, result.item.get(4).type);
		
		System.out.println("test 9:");
		input = ".03-7";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals(".03", result.item.get(0).data);
		assertEquals(TokenType.NUMBER, result.item.get(0).type);
		assertEquals("-", result.item.get(1).data);
		assertEquals(TokenType.SYMBOL, result.item.get(1).type);
		assertEquals("7", result.item.get(2).data);
		assertEquals(TokenType.NUMBER, result.item.get(2).type);
		
		System.out.println("test 10:");
		input = "//comment\nwords";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(1, result.item.size());
		assertEquals("words", result.item.get(0).data);
		assertEquals(TokenType.WORD, result.item.get(0).type);
		
		System.out.println("test 11:");
		input = "@dir not_so";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(2, result.item.size());
		assertEquals("dir", result.item.get(0).data);
		assertEquals(TokenType.DIRECTIVE, result.item.get(0).type);
		assertEquals("not_so", result.item.get(1).data);
		assertEquals(TokenType.WORD, result.item.get(1).type);
	}
	
}
