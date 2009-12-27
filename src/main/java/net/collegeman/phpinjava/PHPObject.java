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

import com.caucho.quercus.env.*;

/**
 * A thin wrapper around instances of <code>com.caucho.quercus.env.Value</code>, themselves representing
 * instances of PHP objects. This wrapper makes it easier to invoke methods and set and get properties.
 * @author Aaron Collegeman aaron@collegeman.net
 */
public class PHPObject {
	
	private Value wrapped;
	
	private Env env;
	
	public PHPObject(Env env, Value value) {
		this.env = env;
		this.wrapped = value;
	}
	
	/**
	 * Invoke the method <code>name</code> on wrapped <code>Value</code> object, passing
	 * into the invocation parameters <code>args</code>.
	 * @return An instance of <code>PHPObject</code>, wrapping any return value of <code>name</code>ed method.
	 */
	public final PHPObject invokeMethod(String name, Object ... args) {
		if (args != null && args.length > 0) {
			Value values[] = new Value[args.length];
			for(int i=0; i<args.length; i++)
				values[i] = PHP.toValue(env, args[i]);
				
			return new PHPObject(env, wrapped.callMethod(env, new StringBuilderValue(name), values));
		}
		else {
			return new PHPObject(env, wrapped.callMethod(env, new StringBuilderValue(name), new Value[]{}));
		}	
	}
	
	/**
	 * Set a public property of the wrapped <code>Value</code> to <code>value</code>.
	 * @return This instance of <code>PHPObject</code>, to support method chaining.
	 */
	public final PHPObject setProperty(String name, Object value) {
		wrapped.putField(env, new StringBuilderValue(name), PHP.toValue(env, value));
		return this;
	}
	
	/**
	 * Retrieve a public property <code>name</code> of the wrapped <code>Value</code>.
	 * @return A new instance of <code>PHPObject</code>, wrapping the retrieved property.
	 */
	public final PHPObject getProperty(String name) {
		return new PHPObject(env, wrapped.getField(env, new StringBuilderValue(name)));
	}
	
	public Value getWrappedValue() {
		return wrapped;
	}
	
	public String toString() {
		return wrapped.toJavaString();
	}
	
}