package com.iconmaster.source.prototype;

import com.iconmaster.source.parse.Parser;
import com.iconmaster.source.parse.Token;
import com.iconmaster.source.parse.Tokenizer;
import com.iconmaster.source.util.Result;
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
		Result<SourcePackage> pkg;
		
		System.out.println("test 1");
		s = "package test";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertTrue(pkg.item.subPackages.containsKey("test"));
		
		System.out.println("test 2");
		s = "package test function lol() {}";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertTrue(pkg.item.subPackages.containsKey("test"));
		assertTrue(pkg.item.subPackages.get("test").functions.containsKey("lol"));
		
		System.out.println("test 3");
		s = "field x,y,z";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(3,pkg.item.fields.size());
		assertTrue(pkg.item.fields.containsKey("x"));
		assertTrue(pkg.item.fields.containsKey("y"));
		assertTrue(pkg.item.fields.containsKey("z"));
		assertEquals(0,pkg.item.rawFieldValues.size());
		
		System.out.println("test 4");
		s = "field x,y,z = 1,2,3";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(3,pkg.item.fields.size());
		assertTrue(pkg.item.fields.containsKey("x"));
		assertTrue(pkg.item.fields.containsKey("y"));
		assertTrue(pkg.item.fields.containsKey("z"));
		assertEquals(1,pkg.item.rawFieldValues.size());
		
		System.out.println("test 5");
		s = "import lib";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(1,pkg.item.imports.size());
		assertEquals(1,pkg.item.imports.get(0).name.size());
		assertEquals("lib",pkg.item.imports.get(0).name.get(0));
		assertTrue(!pkg.item.imports.get(0).file);
		
		System.out.println("test 6");
		s = "import lib1,lib2";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(2,pkg.item.imports.size());
		assertEquals(1,pkg.item.imports.get(0).name.size());
		assertEquals("lib1",pkg.item.imports.get(0).name.get(0));
		assertEquals(1,pkg.item.imports.get(1).name.size());
		assertEquals("lib2",pkg.item.imports.get(1).name.get(0));
		assertTrue(!pkg.item.imports.get(0).file);
		assertTrue(!pkg.item.imports.get(1).file);
		
		System.out.println("test 7");
		s = "import lib1.lib2";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(1,pkg.item.imports.size());
		assertEquals(2,pkg.item.imports.get(0).name.size());
		assertEquals("lib1",pkg.item.imports.get(0).name.get(0));
		assertEquals("lib2",pkg.item.imports.get(0).name.get(1));
		assertTrue(!pkg.item.imports.get(0).file);
		
		System.out.println("test 8");
		s = "package pkg {field x}";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertTrue(pkg.item.subPackages.containsKey("pkg"));
		assertTrue(pkg.item.subPackages.get("pkg").fields.containsKey("x"));
		
		System.out.println("test 9");
		s = "import lib as alias";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(1,pkg.item.imports.size());
		assertEquals(1,pkg.item.imports.get(0).name.size());
		assertEquals("lib",pkg.item.imports.get(0).name.get(0));
		assertEquals(1,pkg.item.imports.get(0).alias.size());
		assertEquals("alias",pkg.item.imports.get(0).alias.get(0));
		assertTrue(!pkg.item.imports.get(0).file);
		
		System.out.println("test 10");
		s = "import \"file.src\"";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(1,pkg.item.imports.size());
		assertEquals(1,pkg.item.imports.get(0).name.size());
		assertEquals("file.src",pkg.item.imports.get(0).name.get(0));
		assertTrue(pkg.item.imports.get(0).file);
		
		System.out.println("test 11");
		s = "import \"file.src\" as alias";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(1,pkg.item.imports.size());
		assertEquals(1,pkg.item.imports.get(0).name.size());
		assertEquals("file.src",pkg.item.imports.get(0).name.get(0));
		assertEquals(1,pkg.item.imports.get(0).alias.size());
		assertEquals("alias",pkg.item.imports.get(0).alias.get(0));
		assertTrue(pkg.item.imports.get(0).file);
		
		System.out.println("test 12");
		s = "import \"file.src\" as a1.a2";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(1,pkg.item.imports.size());
		assertEquals(1,pkg.item.imports.get(0).name.size());
		assertEquals("file.src",pkg.item.imports.get(0).name.get(0));
		assertEquals(2,pkg.item.imports.get(0).alias.size());
		assertEquals("a1",pkg.item.imports.get(0).alias.get(0));
		assertEquals("a2",pkg.item.imports.get(0).alias.get(1));
		assertTrue(pkg.item.imports.get(0).file);
		
		System.out.println("test 13");
		s = "import lib as a1.a2";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertEquals(1,pkg.item.imports.size());
		assertEquals(1,pkg.item.imports.get(0).name.size());
		assertEquals("lib",pkg.item.imports.get(0).name.get(0));
		assertEquals(2,pkg.item.imports.get(0).alias.size());
		assertEquals("a1",pkg.item.imports.get(0).alias.get(0));
		assertEquals("a2",pkg.item.imports.get(0).alias.get(1));
		assertTrue(!pkg.item.imports.get(0).file);
		
		System.out.println("test 14");
		s = "package {field x}";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		pkg = Prototyper.prototype(code);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+pkg);
		assertTrue(!pkg.failed);
		assertTrue(pkg.item.fields.containsKey("x"));
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
		
		System.out.println("test 10");
		s = "func[T]()";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertTrue(fn.rawArgs.isEmpty());
		assertEquals(1, fn.rawParams.size());
		assertEquals("T", fn.rawParams.get(0).data);
		
		System.out.println("test 11");
		s = "func[A,B]()";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		fn = new Function();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeFunction(code, fn, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+fn);
		assertEquals("func", fn.name);
		assertTrue(fn.rawArgs.isEmpty());
		assertEquals(2, fn.rawParams.size());
		assertEquals("A", fn.rawParams.get(0).data);
		assertEquals("B", fn.rawParams.get(1).data);
	}
	
	@Test
	public void testPrototypeField() {
		System.out.println("===PROTOTYPER: FIELDS===");
		
		Token code;
		Field f;
		String s;
		Prototyper.PrototyperContext ctx;
		
		System.out.println("test 1");
		s = "x";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		f = new Field();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeField(code, f, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+f);
		assertEquals("x", f.name);
		
		System.out.println("test 2");
		s = "pkg.x";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		f = new Field();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeField(code, f, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+f);
		assertTrue(ctx.pkg.subPackages.containsKey("pkg"));
		assertTrue(ctx.pkg.subPackages.get("pkg").fields.containsKey("x"));
		assertEquals("x", f.name);
		
		System.out.println("test 3");
		s = "x as int";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		f = new Field();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeField(code, f, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+f);
		assertEquals("x", f.name);
		assertEquals("int", f.rawDataType.data);
		
		System.out.println("test 4");
		s = "@dir x";
		code = Parser.parse(Tokenizer.tokenize(s).item).item;
		f = new Field();
		ctx = new Prototyper.PrototyperContext(new SourcePackage());
		ctx.pkg.name = "testPkg";
		Prototyper.prototypeField(code, f, ctx);
		System.out.println("\tInput: '"+s+"'");
		System.out.println("\tProduced: "+f);
		assertEquals("x", f.name);
		assertEquals(1, f.dirs.size());
		assertEquals("dir", f.dirs.get(0));
	}
}
