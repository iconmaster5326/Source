package com.iconmaster.source.parse;

import com.iconmaster.source.exception.SourceError;
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
		System.out.println("test 1:");
		String input = "a b c";
		System.out.println("\tInput: '"+input+"'");
		Result<List<Token>> result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals("a", result.item.get(0).data);
		assertEquals(TokenType.WORD, result.item.get(0).type);
		assertEquals("b", result.item.get(1).data);
		assertEquals(TokenType.WORD, result.item.get(1).type);
		assertEquals("c", result.item.get(2).data);
		assertEquals(TokenType.WORD, result.item.get(2).type);
		
		System.out.println("test 2:");
		input = "->`<-";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(true, result.failed);
		assertEquals(null, result.item);
		assertEquals(1, result.errors.length);
		assertEquals(SourceError.ErrorType.UNKNOWN_SYMBOL, result.errors[0].type);
		assertEquals(new Range(2,3), result.errors[0].range);
		
		System.out.println("test 3:");
		input = "1.2 a.b";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(4, result.item.size());
		assertEquals("1.2", result.item.get(0).data);
		assertEquals(TokenType.NUMBER, result.item.get(0).type);
		assertEquals("a", result.item.get(1).data);
		assertEquals(TokenType.WORD, result.item.get(1).type);
		assertEquals(".", result.item.get(2).data);
		assertEquals(TokenType.DOT, result.item.get(2).type);
		assertEquals("b", result.item.get(3).data);
		assertEquals(TokenType.WORD, result.item.get(3).type);
		
		System.out.println("test 4:");
		input = "hello\"hello world\"world";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals("hello", result.item.get(0).data);
		assertEquals(TokenType.WORD, result.item.get(0).type);
		assertEquals("hello world", result.item.get(1).data);
		assertEquals(TokenType.STRING, result.item.get(1).type);
		assertEquals("world", result.item.get(2).data);
		assertEquals(TokenType.WORD, result.item.get(2).type);
		
		System.out.println("test 5:");
		input = "a\"\"b";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals("a", result.item.get(0).data);
		assertEquals(TokenType.WORD, result.item.get(0).type);
		assertEquals("", result.item.get(1).data);
		assertEquals(TokenType.STRING, result.item.get(1).type);
		assertEquals("b", result.item.get(2).data);
		assertEquals(TokenType.WORD, result.item.get(2).type);
		
		System.out.println("test 6:");
		input = "((";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(2, result.item.size());
		assertEquals("(", result.item.get(0).data);
		assertEquals(TokenType.LPAREN, result.item.get(0).type);
		assertEquals("(", result.item.get(1).data);
		assertEquals(TokenType.LPAREN, result.item.get(1).type);
		
		System.out.println("test 7:");
		input = "[$$$]";
		System.out.println("\tInput: '"+input+"'");
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(3, result.item.size());
		assertEquals("[", result.item.get(0).data);
		assertEquals(TokenType.LBRACKET, result.item.get(0).type);
		assertEquals("$$$", result.item.get(1).data);
		assertEquals(TokenType.SYMBOL, result.item.get(1).type);
		assertEquals("]", result.item.get(2).data);
		assertEquals(TokenType.RBRACKET, result.item.get(2).type);
		
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
	}
	
}
