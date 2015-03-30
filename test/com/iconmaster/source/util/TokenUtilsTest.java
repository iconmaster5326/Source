package com.iconmaster.source.util;

import com.iconmaster.source.parse.Token;
import com.iconmaster.source.parse.TokenType;
import java.util.ArrayList;
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
public class TokenUtilsTest {
	
	public TokenUtilsTest() {
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
	 * Test of getTokens method, of class TokenUtils.
	 */
	@Test
	public void testGetTokens() {
		System.out.println("===TOKEN UTILS===");
		Token input;
		TokenType type;
		List<Token> expResult;
		List<Token> result;
		
		System.out.println("test 1");
		input = new Token(TokenType.WORD, "x", null, null, null);
		type = TokenType.LINK;
		System.out.println("\tInput: '"+input+"' and '"+type+"'");
		result = TokenUtils.getTokens(input, type);
		System.out.println("\tProduced: "+result);
		expResult = new ArrayList<>();
		expResult.add(input);
		assertEquals(expResult, result);
	}
	
}
