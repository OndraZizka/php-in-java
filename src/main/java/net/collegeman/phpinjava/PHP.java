package net.collegeman.phpinjava;

/**
 * PHP-in-Java PHP wrapper for Java and Groovy.
 * Copyright (C) 2009-2010 Collegeman.net, LLC.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import com.caucho.quercus.*;
import com.caucho.vfs.*;
import com.caucho.util.*;
import com.caucho.quercus.env.*;
import com.caucho.quercus.page.*;

import java.util.logging.*;
import java.net.*;
import java.io.*;
import java.util.*;

import org.springframework.mock.web.*;

/**
 * <p>Instances of this class wrap one or more PHP scripts, making it possible for
 * those scripts to be compiled into memory and</p>
 * <ul>
 * <li>for functions defined therein to be called from within Java code</li>
 * <li>for classes defined therein to be instantiated and used from within Java code</li>
 * <li>for the entire script to be executed, the output buffered for use within Java code</li>
 * </ul>
 * <p>Also, once instantiated, the <code>PHP</code> object can be used to compile and cache   
 * arbitrary snippets of PHP into a single PHP script.</p>
 * <h3>Quercus and Resin versus PHP-in-Java</h3>
 * <p>Our PHP-in-Java library is built on top of Quercus.</p>
 * <p>Quercus is a PHP interpreter written in Java, distributed by <a href="http://caucho.com" target="_top">Caucho</a>.
 * Quercus is embedded inside a Java EE compliant application server called Resin. Resin gives
 * you the flexibility of writing PHP applications that have access to the Java ecosystem, and is
 * many times more efficient than running PHP in Apache.</p>
 * <p>If you're wanting to run large-scale PHP applications on Java, you need to take a look at Resin.
 * But if you're just looking to make PHP libraries like <a href="http://github.com/collegeman/geshi4j" target="_top">GeSHi</a>
 * available to your Java and Groovy code, our PHP-in-Java library is for you.</p>
 * <h3>Using PHP-in-Java</h3>
 * <p>For usage instructions, please refer to <a href="http://github.com/collegeman/php-in-java" target="_top">README</a> on our Github project.</p>
 * @author Aaron Collegeman aaron@collegeman.net
 */
public class PHP {

	private static final Logger log = Logger.getLogger(PHP.class.getName());
	private static Quercus quercus;
	
	private synchronized Quercus getQuercus() {
		if (quercus == null) {
			quercus = new Quercus();
		}		
		return quercus;
	}
	
	/**
	 * Initialize a <code>PHP</code> wrapper with either an intial PHP script or a local directory. 
	 * <p><code>url</code> can take one of several forms:</p>
	 * <ul>
	 * <li>A <b>classpath</b> reference, taking the form <code>classpath:/path/to/file/or/directory</code></li>
	 * <li>A <b>remote script</b> reference, taking the form <code>http://path/to/script</code></li>
     * <li>All other forms are assumed to be <b>file</b> references, referring to files available locally</li>
     * </ul>
     * @param url An initial PHP script to load or a local directory 
	 */
	public PHP(String url) {
		this(url, PHP.class.getClassLoader());
	}
	
	private QuercusPage main;
	
	/** 
	 * Create an empty instance of <code>PHP</code>. This instance can be used to execute arbitrary
	 * snippets of PHP. But if you're looking to load a PHP library and execute snippets against that,
	 * best to use one of the other constructors, {@link #PHP(String)} or {@link #PHP(String, ClassLoader)}
	 */
	public PHP() {}
	
	
	/**
	 * Initialize a <code>PHP</code> wrapper with a specific <code>ClassLoader</code> instance. Refer
	 * to the doc for {@link #PHP(String)} for a description of the <code>url</code> parameter.
	 */
	public PHP(String url, ClassLoader classLoader) {
		if (url == null || url.length() < 1)
			throw new IllegalArgumentException("[url] parameter must be defined");
			
		if (classLoader == null)
			throw new IllegalArgumentException("[classLoader] parameter must be defined");
		
			
		// classpath reference
		if (url.indexOf("classpath:/") == 0) {
			URL resource = classLoader.getResource(url.substring(11));
			File ref = new File(resource.getPath());
			initByFile(ref);
		}
		
		// remote script
		else if (url.indexOf("http://") == 0 || url.indexOf("https://") == 0) {
			try {
				StringBuilder script = new StringBuilder();
				URLConnection conn = new URL(url).openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = in.readLine()) != null) {
					script.append(line);
					script.append("\n");
				}
				
				snippet(script.toString());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		// file reference
		else {
			initByFile(new File(url));
		}
		
	}
	
	/**
	 * Initialize a <code>PHP</code> wrapper with a specific <code>File</code> loaded by the host.
	 * @param file A file full of PHP script
	 */
	public PHP(File file) {
		initByFile(file);
	}
	
	private void initByFile(File ref) {
		if (!ref.exists()) {
			throw new RuntimeException(new FileNotFoundException("No PHP file or directory at ["+ref.getAbsolutePath()+"]"));
		}
		
		if (ref.isDirectory()) {
			snippet("");
			getEnv().setPwd(new FilePath(ref.getAbsolutePath()));
		}
		else {
			try {
				main = getQuercus().parse(new FilePath(ref.getAbsolutePath()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			initEnv(main);
			
			File dir = ref.getParentFile();
			if (dir != null)
				getEnv().setPwd(new FilePath(ref.getParentFile().getAbsolutePath()));
			
			main.executeTop(getEnv());
		}
	}
	
	private Env env;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private StreamImpl out;
	private WriteStream ws;
	
	private void initEnv(QuercusPage page) {
		if (env == null) {
			request = new MockHttpServletRequest();
			response = new MockHttpServletResponse();
			
			WriterStreamImpl writer = new WriterStreamImpl();
			try {
				writer.setWriter(response.getWriter());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			
			out = writer;
			ws = new WriteStream(out);
			ws.setNewlineString("\n");
			
			env = getQuercus().createEnv(page, ws, request, response);
			
			env.setPwd(new FilePath(System.getProperty("user.dir")));
			
			env.start();
		}
	}
	
	/**
	 * Retrieves the Quercus execution environment, which your Java code
	 * can use to interact directly with the Quercus parsing engine.
	 * @link http://www.caucho.com/resin-javadoc/com/caucho/quercus/env/Env.html
	 * @throws IllegalStateException When environment has not yet been initialized
	 */
	public final Env getEnv() {
		if (env == null)
			throw new IllegalStateException("Environment not yet initialized");	
		return env;
	}
	
	/**
	 * Set the value of a global variable in the PHP execution environment.
	 * @param name The name of the global parameter to create/update
	 * @param obj The value to store there
	 */
	public PHP set(String name, Object obj) {
		snippet("");
		getEnv().setGlobalValue(name, toValue(getEnv(), obj));
		return this;
	}
	
	/**
	 * Retrieve the value of a global varialbe in the PHP execution environment.
	 * @param name The name of the global parameter tto read
	 */
	public PHPObject get(String name) {
		snippet("");
		return new PHPObject(getEnv(), getEnv().getGlobalValue(name));
	}
	
	/**
	 * Ensures that <code>obj</code> is of type or wrapped in an instance
	 * of Quercus' <code>Value</code>, with respect to the given execution
	 * environment <code>env</code>.
	 * @return <code>obj</code> or <code>obj</code> wrapped in a <code>Value</code> instance.
	 */
	public static Value toValue(Env env, Object obj) {
		if (obj == null)
			return env.wrapJava(obj);
		else if (obj instanceof PHPObject)
			return ((PHPObject) obj).getWrappedValue();
		else if (obj instanceof Value)
			return (Value) obj;
		else 
			return env.wrapJava(obj);
	}
	
	/** 
	 * Parse and execute a <code>snippet</code> of PHP script, adding to the
	 * execution context any artifacts and/or output generated by the code.
	 */
	public PHP snippet(String snippet) {
		try {
			QuercusPage page = getQuercus().parse(StringStream.open(snippet));
			initEnv(page);
			page.executeTop(getEnv());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return this;
	}
	
	/**
	 * Retrieve the output generated by all PHP scripts executed in this context.
	 */
	public String toString() {
		if (env != null) {
			try {
				ws.flush();
				return response.getContentAsString();
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			return null;
		}
	}
	
	/**
	 * Clear any text buffered by script execution, presumably in preparation for
	 * executing another PHP snippet.
	 * @return This instance of <code>PHP</code>, to support method chaining.
	 */
	public PHP clear() {
		response.setCommitted(false);
		response.reset();
		return this;
	}
	
	/**
	 * Call the PHP function named <code>fxName</code> with arguments <code>args</code>
	 * @return An instance of PHPObject, wrapped around the return value of the function.
	 */
	public PHPObject fx(String fxName, Object ... args) {
		if (args != null && args.length > 0) {
			Value[] values = new Value[args.length];
			for (int i=0; i<args.length; i++)
				values[i] = toValue(getEnv(), args[i]);
				
			return new PHPObject(getEnv(), getEnv().call(fxName, values));
		}
		else {
			return new PHPObject(getEnv(), getEnv().call(fxName));
		}
	}
	
	/**
	 * Create a new instance of the PHP class <code>className</code>, initialized with
	 * arguments <code>args</code>.
	 * @return An instance of PHPObject, wrapping the new instance of <code>className</code>.
	 */
	public PHPObject newInstance(String className, Object ... args) {
		QuercusClass clazz = getEnv().findClass(className);
		if (clazz == null)
			throw new RuntimeException(new ClassNotFoundException("PHP:"+className));
		
		if (args != null && args.length > 0) {
			Value[] values = new Value[args.length];
			for (int i=0; i<args.length; i++)
				values[i] = toValue(getEnv(), args[i]);
				
			return new PHPObject(getEnv(), clazz.callNew(getEnv(), values));
		}
		else {
			return new PHPObject(getEnv(), clazz.callNew(getEnv(), new Value[]{}));
		}
	}
	
}