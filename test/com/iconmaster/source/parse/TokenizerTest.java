package com.iconmaster.source.parse;

import java.util.ArrayList;
import java.util.Arrays;
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
		System.out.println("tokenize");
		String input = "a b c";
		List<Token> expResult = new ArrayList<>(Arrays.asList(new Token(TokenType.WORD, "a"),new Token(TokenType.WORD, "b"),new Token(TokenType.WORD, "c")));
		List<Token> result = Tokenizer.tokenize(input);
		assertEquals(expResult, result);
	}
	
}
