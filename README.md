#PHP-in-Java: execute PHP code from within Java and Groovy

##Introduction

###Why would I want to execute PHP from within my Java code?

As a consultant, I have spent a lot of time moving back and forth between two Open Source
communities: Java and PHP. Despite some major criticisms against its verbosity and depth,
Java has left an indelible mark on software development and developers. If there's a 
programming problem to be solved, odds are you can find a solution or a pattern for solving
it in the Java ecosystem.

But long before Java was usable in Web applications, PHP was making it possible for a
host of young and old developers to write CGI scripts without shouldering the burden of learning PERL
or C. Like Java, PHP's ecosystem is thriving, especially given its adoption at the heart of
many major Web companies the likes of Facebook and [Squidoo](http://www.squidoo.com).

Essentially what we have then are two beautiful worlds of software, and a lot of overlap. But
where they don't overlap I seem to find myself working in one language and longing for the
features of the other. For some time now it has been possible to access [Java code from
within PHP](http://www.php.net/manual/en/java.examples-basic.php). But accessing PHP from
within Java has never been as straight-forward.

###So, how does it work?

As it turns out, years ago an enterprising company named [Caucho](http://caucho.com) 
decided to write the "most reliable application server in the Open Source market." Their
core product is a Java EE compliant Web server they call *Resin*, and at its heart is *Quercus*:
a fast PHP interpreter written in cross-platform Java.

So if your goal is to run PHP in a Java EE Web container, then Resin is your tool. 

But I wondered if I could use Quercus from within any old Java application, and at along last gain
access to my favorite PHP libraries from within Java.  As it turns out, this is quite possible. 
I've recently pushed out my first Java-wrapped PHP library: [GeSHi4J](http://github.com/collegeman/geshi4j),
making my favorite HTML-based code highlighting library available to me in any Java application!

##Writing Your Own Wrappers

###Using the PHP wrapper API

The following examples, while not practical on an individual basis, should be used in
combination to create great PHP wrappers.

####Executing a PHP script

PHP scripts may be loaded from the classpath (my personal favorite, as the scripts end up packed 
with the Java code in a JAR file), loaded from the local file system (by a `File` object or a
simple file path), or remotely via HTTP.

	PHP php = new PHP("classpath:/com/faux/php/HelloWorld.php");
	
Once loaded into memory, several execution options become available to you.

####Calling global functions

Like so

	PHPObject returnValue = php.fx("function_name", arg1, arg2, ...);
	
	// or...
	PHPObject returnValue = php.fx("function_name", new Object[]{arg1, arg2});
	
	// or...
	List args = new ArrayList<Object>();
	params.add(arg1);
	params.add(arg2);
	PHPObject returnValue = php.fx("function_name", args.toArray());
	
The return type of the `fx(String, Object ... args)` method is an instance of our <a href="http://aaroncollegeman.com/static/projects/php-in-java/javadoc/net/collegeman/phpinjava/PHPObject.html">`PHPObject`</a> class, which wraps a special kind of Quercus object called <a href="http://www.caucho.com/resin-javadoc/com/caucho/quercus/env/Value.html">`Value`</a>. Through our `PHPObject` API you can 

* retrieve a `String` version of the `Value`'s content with `toString()`
* read/write object properties with `getProperty(String property)` and `setProperty(String property, Object newValue)`
* invoke object methods with `invokeMethod(String name, Object ... args)`
* gain direct access to the wrapped `Value` object through `getWrappedValue()`

Extra line of text here to create whitespace between unordered list and H4. :-P
 
####Setting and reading global variables

	// write
	php.set("variable_name", value);
	
	// read
	PHPObject value = php.get("variable_name");
	
####Instantiating PHP classes

	// create instance
	PHPObject myInstance = php.newInstance("MyPHPClass", arg1, arg2, ...);
	
	// read and write properties
	PHPObject propertyValue = myInstance.getProperty("propertyName");
	myInstance.setProperty("propertyName", newValue);
	
	// invoke methods
	PHPObject returnValue = myInstance.invokeMethod("methodName", arg1, arg2, ...);
	
####Capturing the output of your script

	// load a script
	PHP myScript = new PHP("classpath:/path/to/myscript.php");
	
	// do stuff with your script
	myScript.set("global_variable", "foo");
	myScript.fx("process_foo");
	
	// capture output
	String output = myScript.toString();	
	
####Executing arbitrary snippets of PHP

	// when no other scripts have yet been parsed
	PHP php = new PHP();
	php.snippet("<?php echo 'Hello, world!'");
	String output = php.toString(); // == "Hello, world!"
	
	// and when a script block is already open
	PHP php = new PHP("classpath:/path/to/myscript.php");
	php.snippet("my_function('foo', 'bar');");
	String output = php.toString();
	
###Strategy for composing wrappers

The best pattern for implementing a wrapper with PHP-in-Java is the *Adapter* pattern, bar none. 

From [Design Patterns in Java](http://www.amazon.com/gp/product/0321333020?ie=UTF8&tag=httpcollegene-20&linkCode=as2&camp=1789&creative=390957&creativeASIN=0321333020)

> The intent of `ADAPTER` is to provide the interface that a client expects while
> using the services of a class with a different interface.

Of course, our use of *interface* here is a bit more abstract (no pun intended): the
interface for your wrapper should reflect the interface of your PHP-scripted class, not the interface
of our Java-based PHP class.

With a good adapter in place, your users should never have to do this

	PHP php = new PHP("classpath:/path/to/MyPHPClass.php");
	PHPObject instance = php.newInstance("MyPHPClass", arg1, arg2);
	PHPObject value = instance.invokeMethod("myMethod", arg3, arg4);
	String result = value.toString();
	
but should instead be instantiating and interacting with an API that reflects your PHP class

	MyPHPClass instance = new MyPHPClass(arg1, arg2);
	String result = instance.myMethod(arg3, arg4);

As the author of the wrapper, your job is to take care of bridging the gap between 
the native PHP API and your Java-based implementation of the same.

The best example for writing your own wrapper is my wrapper for GeSHi: [GeSHi4J](http://github.com/collegeman/geshi4j).

If you've written your own PHP wrapper with PHP-in-Java, [follow me on Twitter](http://twitter.com/collegeman) or [GitHub](http://github.com/collegeman),
and message me with the URL to your own project.

###GroovyPHP and GroovyPHPObject

For you Groovy developers, we've made your job even easier. If you build your adapter on
top of our own Groovy adaptations of `PHP` and `PHPObject`, then your work is practically
complete.  

Taking advantage of the `GroovyObject` interface, the `invokeMethod(String, Object)`,
`getProperty(String)` and `setProperty(String, Object)` methods of `GroovyPHPObject` 
comply with the method signatures needed to make their invocation dynamic. 

So you can say goodbye to having to write methods in your adapter to mirror those in
your PHP classes' API.  Instead, all you'll need to focus on are implementing the
necessary constructors, and implementing any methods whose return type you wish to
unwrap from `GroovyPHPObject` or Quercus `Value` instances.

Here's an example of a `GroovyPHP`-based PHP class adapter

	class MyPHPClass {

		def instance
	
		public MyPHPClass(arg1, arg2) {
			instance = new GroovyPHP("classpath:/path/to/MyPHPClass.php").newInstance("MyPHPClass", arg1, arg2)
		}
	
		Integer specificMethod = { arg ->
			instance.invokeMethod("specificMethod", arg).toJavaInteger()
		}

	}

And an example of usage

	def myPHP = new MyPHPClass(arg1, arg2)
	
	// since we need a specific return type here, we've implemented a specific signature
	Integer value = myPHP.specificMethod(arg)
	
	// but for all other use cases, we rely on GroovyObject support
	def otherValue = myPHP.someOtherMethod(args)
	
This usage example assumes that in the definition of our PHP class `MyPHPClass` there exists
some method `someOtherMethod` that accepts at least one parameter.

The return type of `myPHP.someOtherMethod(args)` will be another instance of `GroovyPHPObject`,
thus allowing for the same dynamic approach to interfaces provided by the Groovy-powered
instance of `MyPHPClass`.

###What's next?

For the time being, our `PHP` and `GroovyPHP` classes adapt only the most useful 
aspects of the Quercus interpreter API. Missing from our implementation are

* Access to constants
* Access to static methods and properties of classes
* Access to the PHP ini model
* Access to methods for importing PHP scripts (from within Java - you can still use `require`, `include`, `require_once`, and `include_once` in your PHP scripts)

If you require access to these methods or any other aspect of Quercus, you can always get to it
with the method `PHP#getEnv()` and `GroovyPHP#getEnv()`, each returning an instance of <a href="http://www.caucho.com/resin-javadoc/com/caucho/quercus/env/Env.html">Env</a>,
thus exposing the interpreter API to your own code in totality.

If you need to make reference to it without downloading this project, you can browse our [javadoc online](http://aaroncollegeman.com/static/projects/java-in-php/javadoc).

**Good luck, and happy coding.**

 




	
	

	



