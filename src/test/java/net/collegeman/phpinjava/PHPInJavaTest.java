package net.collegeman.phpinjava;

import junit.framework.*;

public class PHPInJavaTest extends TestCase {
	
	public void testBasics() {
		
		assertEquals("Hello, world!", new PHP("classpath:/com/faux/php/HelloWorld.php").run());
		
	}
	
}