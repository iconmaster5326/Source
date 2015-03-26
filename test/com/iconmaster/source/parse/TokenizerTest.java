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
		Result<List<Token>> result = Tokenizer.tokenize(input);
		System.out.println("\tProduced "+result);
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
		result = Tokenizer.tokenize(input);
		System.out.println("\tProduced "+result);
		assertEquals(true, result.failed);
		assertEquals(null, result.item);
		assertEquals(1, result.errors.length);
		assertEquals(SourceError.ErrorType.UNKNOWN_SYMBOL, result.errors[0].type);
		assertEquals(new Range(2,3), result.errors[0].range);
	}
	
}
