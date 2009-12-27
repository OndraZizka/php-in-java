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

/**
 * A simple implementation of the <code>GroovyObject</code> interface, allowing
 * for the seamless introduction of PHP-powered APIs into your Groovy applications
 * (including Grails Web apps!).
 * <p>I really don't think it could get less complicated than this.</p>
 * @author Aaron Collegeman aaron@collegeman.net
 */
public class GroovyPHPObject extends GroovyObjectSupport {
	
	private PHPObject obj;
	
	/**
	 * The <code>GroovyPHPObject</code> is only allowed to wrap one kind of
	 * object: a <code>PHPObject</code> - the wrapper used by many methods in
	 * the <code>PHP</code> class.
	 * <p>The methods {@link GroovyPHP#get(String)}, {@link GroovyPHP#fx(String, Object[])},
	 * and {@link GroovyPHP#newInstance(String, Object...)} each return instances
	 * of <code>GroovyPHPObject</code>s.
	 */
	public GroovyPHPObject(PHPObject obj) {
		this.obj = obj;
	}
	
	/**
	 * Invokes the function or method <code>name</code> with arguments <code>args</code>
	 * on the PHP object reference wrapped within.
	 * @return An instance of <code>GroovyPHPObject</code> wrapping the return value of the function or method.
	 */
	public Object invokeMethod(String name, Object args) {
		return new GroovyPHPObject(obj.invokeMethod(name, args));
	}
	
	/**
	 * Retrieves the value of property <code>property</code> of the PHP object reference wrapped within.
	 * @return An instance of <code>GroovyPHPObject</code> wrapping the value of the property.
     */
	public Object getProperty(String property) {
		return new GroovyPHPObject(obj.getProperty(property));
	}
	
	/**
	 * Assigns <code>newValue</code> to the property <code>property</code> of the PHP object reference
	 * wrapped within.
	 */
	public void setProperty(String property, Object newValue) {
		obj.setProperty(property, newValue);
	}
	
	public String toString() {
		return obj.toString();
	}
	
}