package com.iconmaster.source.parse;

import com.iconmaster.source.exception.SourceError.ErrorType;
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
public class ParserTest {
	
	public ParserTest() {
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
	 * Test of parse method, of class Parser.
	 */
	@Test
	public void testParse() {
		System.out.println("===PARSER===");
		
		System.out.println("test 1:");
		List<Token> tokens = Tokenizer.tokenize("1+2").item;
		System.out.println("\tInput: "+tokens);
		Result<Token> result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.ADD, result.item.type);
		assertEquals(TokenType.NUMBER, result.item.l.type);
		assertEquals("1", result.item.l.data);
		assertEquals(TokenType.NUMBER, result.item.r.type);
		assertEquals("2", result.item.r.data);
		
		System.out.println("test 2:");
		tokens = Tokenizer.tokenize("1/2+3").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.ADD, result.item.type);
		assertEquals(TokenType.DIV, result.item.l.type);
		assertEquals(TokenType.NUMBER, result.item.l.l.type);
		assertEquals("1", result.item.l.l.data);
		assertEquals(TokenType.NUMBER, result.item.l.r.type);
		assertEquals("2", result.item.l.r.data);
		assertEquals(TokenType.NUMBER, result.item.r.type);
		assertEquals("3", result.item.r.data);
		
		System.out.println("test 3:");
		tokens = Tokenizer.tokenize("1*2+3*4").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.ADD, result.item.type);
		assertEquals(TokenType.MUL, result.item.l.type);
		assertEquals(TokenType.NUMBER, result.item.l.l.type);
		assertEquals("1", result.item.l.l.data);
		assertEquals(TokenType.NUMBER, result.item.l.r.type);
		assertEquals("2", result.item.l.r.data);
		assertEquals(TokenType.MUL, result.item.r.type);
		assertEquals(TokenType.NUMBER, result.item.r.l.type);
		assertEquals("3", result.item.r.l.data);
		assertEquals(TokenType.NUMBER, result.item.r.r.type);
		assertEquals("4", result.item.r.r.data);
		
		System.out.println("test 4:");
		tokens = Tokenizer.tokenize("-3").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.NEG, result.item.type);
		assertEquals(TokenType.NUMBER, result.item.l.type);
		assertEquals("3", result.item.l.data);
		
		System.out.println("test 5:");
		tokens = Tokenizer.tokenize("- 1 - 2").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.SUB, result.item.type);
		assertEquals(TokenType.NEG, result.item.l.type);
		assertEquals(TokenType.NUMBER, result.item.l.l.type);
		assertEquals("1", result.item.l.l.data);
		assertEquals(TokenType.NUMBER, result.item.r.type);
		assertEquals("2", result.item.r.data);
		
		System.out.println("test 6:");
		tokens = Tokenizer.tokenize("(3)").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.PAREN, result.item.type);
		assertEquals(TokenType.NUMBER, result.item.l.type);
		assertEquals("3", result.item.l.data);
		
		System.out.println("test 7:");
		tokens = Tokenizer.tokenize("()").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.PAREN, result.item.type);
		assertEquals(null, result.item.l);
		
		System.out.println("test 8:");
		tokens = Tokenizer.tokenize("(1 and 2)").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.PAREN, result.item.type);
		assertEquals(TokenType.AND, result.item.l.type);
		assertEquals(TokenType.NUMBER, result.item.l.l.type);
		assertEquals("1", result.item.l.l.data);
		assertEquals(TokenType.NUMBER, result.item.l.r.type);
		assertEquals("2", result.item.l.r.data);
		
		System.out.println("test 9:");
		tokens = Tokenizer.tokenize("(1 2)").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(true, result.failed);
		assertEquals(1, result.errors.length);
		assertEquals(ErrorType.ILLEGAL_PARENS, result.errors[0].type);
		
		System.out.println("test 10:");
		tokens = Tokenizer.tokenize("(unexpected EOF incoming").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(true, result.failed);
		assertEquals(1, result.errors.length);
		assertEquals(ErrorType.UNEXPECTED_EOF, result.errors[0].type);
		
		System.out.println("test 11:");
		tokens = Tokenizer.tokenize("[3]").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.INDEX, result.item.type);
		assertEquals(TokenType.NUMBER, result.item.l.type);
		assertEquals("3", result.item.l.data);
		
		System.out.println("test 12:");
		tokens = Tokenizer.tokenize("[]").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.INDEX, result.item.type);
		assertEquals(null, result.item.l);
		
		System.out.println("test 13:");
		tokens = Tokenizer.tokenize("[1 2]").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(true, result.failed);
		assertEquals(1, result.errors.length);
		assertEquals(ErrorType.ILLEGAL_PARENS, result.errors[0].type);
		
		System.out.println("test 14:");
		tokens = Tokenizer.tokenize("{3}").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.CODE, result.item.type);
		assertEquals(TokenType.NUMBER, result.item.l.type);
		assertEquals("3", result.item.l.data);
		
		System.out.println("test 15:");
		tokens = Tokenizer.tokenize("{}").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.CODE, result.item.type);
		assertEquals(null, result.item.l);
		
		System.out.println("test 16:");
		tokens = Tokenizer.tokenize("{1 2}").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.CODE, result.item.type);
		assertEquals(TokenType.STATEMENT, result.item.l.type);
		assertEquals(TokenType.NUMBER, result.item.l.l.type);
		assertEquals("1", result.item.l.l.data);
		assertEquals(TokenType.NUMBER, result.item.l.r.type);
		assertEquals("2", result.item.l.r.data);
		
		System.out.println("test 17:");
		tokens = Tokenizer.tokenize("local x").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.LOCAL, result.item.type);
		assertEquals(TokenType.WORD, result.item.l.type);
		assertEquals("x", result.item.l.data);
		
		System.out.println("test 18:");
		tokens = Tokenizer.tokenize("local a,b = 1,2").item;
		System.out.println("\tInput: "+tokens);
		result = Parser.parse(tokens);
		System.out.println("\tProduced: "+result);
		assertEquals(false, result.failed);
		assertEquals(TokenType.LOCAL, result.item.type);
		assertEquals(TokenType.ASSIGN, result.item.l.type);
		assertEquals(TokenType.TUPLE, result.item.l.l.type);
		assertEquals(TokenType.WORD, result.item.l.l.l.type);
		assertEquals("a", result.item.l.l.l.data);
		assertEquals(TokenType.WORD, result.item.l.l.r.type);
		assertEquals("b", result.item.l.l.r.data);
		assertEquals(TokenType.TUPLE, result.item.l.r.type);
		assertEquals(TokenType.NUMBER, result.item.l.r.l.type);
		assertEquals("1", result.item.l.r.l.data);
		assertEquals(TokenType.NUMBER, result.item.l.r.r.type);
		assertEquals("2", result.item.l.r.r.data);
	}
	
}
