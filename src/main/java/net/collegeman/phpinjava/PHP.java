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
import java.net.URL;
import java.io.*;

import org.springframework.mock.web.*;

/**
 * <p>Instances of this class wrap one or more PHP scripts, making it possible for
 * those scripts to be compiled into memory and</p>
 * <ul>
 * <li>for functions defined therein to be called from within Java code</li>
 * <li>for classes defined therein to be instantiated and used from within Java code</li>
 * <li>for the entire script to be executed, the output buffered for use within Java code</li>
 * </ul>
 * <p>Also, once instantiated, the <code>PHP</code> object can be used to</p>
 * <ul>
 * <li>load multiple independent PHP scripts, creating a bank of functionality cached in memory</li>
 * <li>compile and cache arbitrary snippets of PHP, composed from within Java</li>
 * </ul>
 * <h3>Quercus and Resin versus PHP-in-Java</h3>
 * <p>Quercus is a PHP interpreter written and distributed by <a href="http://coucho.com">Coucho</a>.
 * Quercus is distributed inside a Java EE compliant application server called Resin. Resin gives
 * you the flexibility of writing PHP applications that have access to the Java ecosystem, and is
 * many times more efficient than running PHP in Apache.</p>
 * <p>If you want to make PHP libraries like <a href="http://github.com/collegeman/geshi4j">GeSHi</a>
 * available to your Java and Groovy code, our PHP-in-Java library is for you.</p>
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
     * @param String An initial PHP script to load or a local directory 
	 */
	public PHP(String url) {
		this(url, PHP.class.getClassLoader());
	}
	
	private Path root;
	private QuercusPage main;
	
	/**
	 * Initial a <code>PHP</code> wrapper with a specific <code>ClassLoader</code> instance. Refer
	 * to the doc for {@link PHP(String)} for a description of the <code>url</code> parameter.
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
			
			if (ref.isDirectory()) {
				root = new FilePath(ref.getAbsolutePath());
			}
			else {
				File dir = ref.getParentFile();
				if (dir != null)
					root = new FilePath(ref.getParentFile().getAbsolutePath());
					
				try {
					main = getQuercus().parse(new FilePath(ref.getAbsolutePath()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				initEnv(main);
			}
		}
		
		// remote script
		else if (url.indexOf("http://") == 0 || url.indexOf("https://") == 0) {
			
		}
		
		// file reference
		else {
			
		}
		
	}
	
	public PHP() {
		
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
		}
	}
	
	public final Env getEnv() {
		if (env == null)
			throw new IllegalStateException("Environment not yet initialized");	
		return env;
	}
	
	public PHP set(String name, Object obj) {
		env.setGlobalValue(name, env.wrapJava(obj));
		return this;
	}
	
	public PHP require(String path) {
		return require(path, true);
	}
	
	public PHP require(String path, boolean once) {
		return this;
	}
	
	public PHP include(String path) {
		return include(path, true);
	}
	
	public PHP include(String path, boolean once) {
		return this;
	}
	
	public PHP inject(String path) {
		return this;
	}
	
	public void snippet(String snippet) {
		
	}
	
	public String run() {
		
		return null;
	}
	
	public String run(String snippet) {
		return null;
	}
	
	public Value execute(String snippet) {
		
		return null;
	}
	
	public Value fx(String fxName, Object ... args) {
		if (args != null && args.length > 0) {
			Value[] values = new Value[args.length];
			for (int i=0; i<args.length; i++)
				values[i] = getEnv().wrapJava(args[i]);
				
			return getEnv().call(fxName, values);
		}
		else {
			return getEnv().call(fxName);
		}
	}
	
	public Object newInstance(String className, Object ... args) {
		QuercusClass clazz = getEnv().findClass(className);
		if (clazz == null)
			throw new RuntimeException(new ClassNotFoundException("PHP:"+className));
		
		if (args != null && args.length > 0) {
			Value[] values = new Value[args.length];
			for (int i=0; i<args.length; i++)
				values[i] = getEnv().wrapJava(args[i]);
				
			return clazz.callNew(getEnv(), values);
		}
		else {
			return clazz.callNew(getEnv(), new Value[]{});
		}
	}
	
}