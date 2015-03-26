package com.iconmaster.source.parse;

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
		assertEquals("+", result.item.data);
		assertEquals(TokenType.NUMBER, result.item.l.type);
		assertEquals("1", result.item.l.data);
		assertEquals(TokenType.NUMBER, result.item.r.type);
		assertEquals("2", result.item.r.data);
		
		
	}
	
}
