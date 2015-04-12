package com.iconmaster.source.link;

import com.iconmaster.source.SourceInput;
import com.iconmaster.source.prototype.Import;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Result;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author iconmaster
 */
public class LinkerTest {
	
	public LinkerTest() {
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
	 * Test of link method, of class Linker.
	 */
	@Test
	public void testLink_SourceInput_SourcePackage() {
		System.out.println("===LINKER===");
		
		SourceInput si;
		SourcePackage pkg;
		Result<LinkSpace> result;
		
		System.out.println("test 1:");
		si = new SourceInput();
		si.libraries.add(new SourcePackage());
		si.libraries.get(0).getPackage("lib");
		pkg = new SourcePackage();
		pkg.imports.add(new Import());
		pkg.imports.get(0).name = Arrays.asList("lib");
		pkg.imports.get(0).file = false;
		result = Linker.link(si, pkg);
		assertTrue(!result.failed);
		
		System.out.println("test 2:");
		si = new SourceInput();
		si.libraries.add(new SourcePackage());
		si.libraries.get(0).getPackage("lib");
		pkg = new SourcePackage();
		pkg.imports.add(new Import());
		pkg.imports.get(0).name = Arrays.asList("nonexist");
		pkg.imports.get(0).file = false;
		result = Linker.link(si, pkg);
		assertTrue(result.failed);
	}
}
