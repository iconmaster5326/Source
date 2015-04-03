package com.iconmaster.source.prototype;

import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.parse.Token;
import com.iconmaster.source.parse.Tokenizer;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author iconmaster
 */
public class PrototyperTest {
	
	public PrototyperTest() {
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
	 * Test of prototype method, of class Prototyper.
	 */
	@Test
	public void testPrototype_Token() {
		System.out.println("===PROTOTYPER===");
		
		Token code;
		String s;
		SourcePackage pkg;
		
		System.out.println("test 1");
		s = "package test";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(pkg.subPackages.containsKey("test"));
		
		System.out.println("test 2");
		s = "package test function lol() {}";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(pkg.subPackages.containsKey("test"));
		assertTrue(pkg.subPackages.get("test").functions.containsKey("lol"));
	}

	/**
	 * Test of prototypeFunction method, of class Prototyper.
	 */
	@Test
	public void testPrototypeFunction() {
		System.out.println("===PROTOTYPER: FUNCTIONS===");
		Token code;
		Function fn;
		String s;
		Prototyper.PrototyperContext ctx;
		
		System.out.println("test 1");
		s = "func()";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		
		System.out.println("test 2");
		s = "type.func()";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertTrue(ctx.pkg.subPackages.containsKey("type"));
		assertTrue(ctx.pkg.subPackages.get("type").functions.containsKey("func"));
		
		System.out.println("test 3");
		s = "type.func() as int";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertEquals("int", fn.rawReturnType.data);
		assertTrue(ctx.pkg.subPackages.containsKey("type"));
		assertTrue(ctx.pkg.subPackages.get("type").functions.containsKey("func"));
		
		System.out.println("test 4");
		s = "func(a)";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertEquals(1, fn.rawArgs.size());
		assertEquals("a", fn.rawArgs.get(0).name);
		
		System.out.println("test 5");
		s = "func(a,b,c)";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertEquals(3, fn.rawArgs.size());
		assertEquals("a", fn.rawArgs.get(0).name);
		assertEquals("b", fn.rawArgs.get(1).name);
		assertEquals("c", fn.rawArgs.get(2).name);
		
		System.out.println("test 6");
		s = "func(a as int)";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertEquals(1, fn.rawArgs.size());
		assertEquals("a", fn.rawArgs.get(0).name);
		assertEquals("int", fn.rawArgs.get(0).rawDataType.data);
		
		System.out.println("test 7");
		s = "func(a as int,b as real)";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertEquals(2, fn.rawArgs.size());
		assertEquals("a", fn.rawArgs.get(0).name);
		assertEquals("int", fn.rawArgs.get(0).rawDataType.data);
		assertEquals("b", fn.rawArgs.get(1).name);
		assertEquals("real", fn.rawArgs.get(1).rawDataType.data);
		
		System.out.println("test 8");
		s = "func(@dir a)";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertEquals(1, fn.rawArgs.size());
		assertEquals("a", fn.rawArgs.get(0).name);
		assertEquals(1, fn.rawArgs.get(0).dirs.size());
		assertEquals("dir", fn.rawArgs.get(0).dirs.get(0));
		
		System.out.println("test 9");
		s = "func(@dir1 @dir2 a)";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertEquals(1, fn.rawArgs.size());
		assertEquals("a", fn.rawArgs.get(0).name);
		assertEquals(2, fn.rawArgs.get(0).dirs.size());
		assertEquals("dir1", fn.rawArgs.get(0).dirs.get(0));
		assertEquals("dir2", fn.rawArgs.get(0).dirs.get(1));
	}
	
}
