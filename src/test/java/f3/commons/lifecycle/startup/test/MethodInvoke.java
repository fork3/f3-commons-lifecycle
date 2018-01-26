/*
 * Copyright (c) 2010-2018 fork3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package f3.commons.lifecycle.startup.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import f3.commons.lifecycle.startup.IStartModuleFactory;
import f3.commons.lifecycle.startup.Startup;
import f3.commons.lifecycle.startup.StartupInstance;
import f3.commons.lifecycle.startup.StartupManager;

/**
 * @author n3k0nation
 *
 */
public class MethodInvoke {
	
	public static enum StartLevel {
		First,
		Second
	}
	
	public static interface Interface {
		String doLogic();
	}
	
	public static class InterfaceImpl implements Interface {
		static Interface instance;
		
		final String value;
		public InterfaceImpl(String value) {
			this.value = value;
			instance = this;
		}
		
		@Override
		public String doLogic() {
			return value;
		}
	}
	
	public static class SomeFactory {
		
		@Startup("First")
		public static Interface create() {
			return new InterfaceImpl("some value");
		}
		
	}
	
	public static class SomeLoader {
		
		@Startup("Second")
		public static void loadProcess() {
			InterfaceImpl i = new InterfaceImpl("other value");
		}
		
	}
	
	
	@Test
	public void testMethodInvoke() {
		StartupInstance<StartLevel> si = StartupManager.createStartup();
		StartupManager.configureStartupReflection(IStartModuleFactory.getDefaultFactory(), si, Arrays.asList(SomeFactory.class, SomeLoader.class), StartLevel.class);
		si.runLevel(StartLevel.First);
		Assert.assertEquals("some value", InterfaceImpl.instance.doLogic());
		
		si.runLevel(StartLevel.Second);
		Assert.assertEquals("other value", InterfaceImpl.instance.doLogic());
	}
	
	
}
