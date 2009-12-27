package net.collegeman.phpinjava;

import junit.framework.*;
import com.caucho.quercus.env.*;
import java.util.*;
import java.io.*;

public class PHPInJavaTest extends TestCase {
	
	public void testExecuteFromClasspath() {
		assertEquals("Hello, world!", new PHP("classpath:/com/faux/php/HelloWorld.php").toString());
	}
	
	public void testExecuteSnippet() {
		assertEquals("Hello, world!", new PHP().snippet("<?php echo 'Hello, world!';").toString());
	}
	
	public void testExecuteSnippetDefiningFunction() {
		PHP php = new PHP().snippet("<?php function just_return_it($message) { return $message; }");		
		assertEquals("Hello, world!", php.fx("just_return_it", "Hello, world!").toString());
	}
		
	public void testExecuteGlobalFunction() {
		PHP php = new PHP("classpath:/com/faux/php/HelloWorldFx.php");
		assertEquals("Hello, world!", php.fx("repeat", "Hello, world!").toString());
	}
			
	public void testExecuteInstanceMethod() {	
		assertEquals("Hello, world!", new PHP("classpath:/com/faux/php/HelloWorldClass.php")
			.newInstance("Repeater", "Hello, world!")
			.invokeMethod("repeat").toString());
	}
	
	public void testExecuteInstanceMutator() {		
		PHPObject repeater = new PHP("classpath:/com/faux/php/HelloWorldClass.php").newInstance("Repeater");
		repeater.invokeMethod("setMessage", "Foo bar!");
		assertEquals("Foo bar!", repeater.invokeMethod("repeat").toString());
	}
	
	public void testExecutePropertyAccess() {
		PHPObject repeater = new PHP("classpath:/com/faux/php/HelloWorldClass.php").newInstance("Repeater");
		repeater.setProperty("message", "Up, up, and away!");
		assertEquals("Up, up, and away!", repeater.getProperty("message").toString());
	}
	
	public void testGlobalReadAndWrite() {
		PHP php = new PHP();
		php.set("helloWorld", "Hello, world!");
		assertEquals("Hello, world!", php.get("helloWorld").toString());
	}
	
	public void testExecuteFile() {
		// hackish
		String path = getClass().getClassLoader().getResource("com/faux/php/HelloWorld.php").getPath();
		assertEquals("Hello, world!", new PHP(path).toString());
		
		assertEquals("Hello, world!", new PHP(new File(path)).toString());
	}

}