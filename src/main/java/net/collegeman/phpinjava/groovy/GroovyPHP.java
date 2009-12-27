package net.collegeman.phpinjava.groovy;

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

import net.collegeman.phpinjava.*;
import groovy.lang.*;
import java.io.*;
import java.lang.reflect.*;

public class GroovyPHP extends GroovyObjectSupport {
	
	private PHP php;
	
	public GroovyPHP(String url) {
		php = new PHP(url, GroovyPHP.class.getClassLoader());
	}
	
	public GroovyPHP(String url, ClassLoader classLoader) {
		php = new PHP(url, classLoader);
	}	
	
	public GroovyPHP(File file) {
		php = new PHP(file);
	}
	
	public GroovyPHP() {
		php = new PHP();
	}
	
	public Object invokeMethod(String name, Object ... args) {
		try {
			if (args != null && args.length > 0) {
				Class[] classes = new Class[args.length];
				for (int i=0; i<args.length; i++) 
					classes[i] = args[i].getClass();
			
				Method m = php.getClass().getMethod(name, classes);
				return m.invoke(php, args);
			}
			else {
				Method m = php.getClass().getMethod(name, null);
				return m.invoke(php, args);
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public GroovyPHPObject fx(String fxName, Object ... args) {
		return new GroovyPHPObject(php.fx(fxName, args));
	}
	
	public GroovyPHPObject newInstance(String className, Object ... args) {
		return new GroovyPHPObject(php.newInstance(className, args));
	}
	
	public GroovyPHPObject get(String name) {
		return new GroovyPHPObject(php.get(name));
	}
	
	public GroovyPHP set(String name, Object newValue) {
		php.set(name, newValue);
		return this;
	}
	
	public GroovyPHP snippet(String snippet) {
		php.snippet(snippet);
		return this;
	}
	
	public String toString() {
		return php.toString();
	}
	
}