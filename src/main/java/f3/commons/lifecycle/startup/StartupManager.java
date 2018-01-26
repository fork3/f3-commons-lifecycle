/*
 * Copyright (c) 2010-2016 fork2
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
package f3.commons.lifecycle.startup;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import f3.commons.reflection.MethodUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author n3k0nation
 *
 */
@Slf4j
public class StartupManager {
	private StartupManager() {
	}
	
	public static <SL extends Enum<SL>> StartupInstance<SL> createStartup() {
		return new StartupInstance<>();
	}
	
	public static <SL extends Enum<SL>> void purgeStartup(StartupInstance<SL> startupInstance) {
		for(Entry<SL, List<StartModule<SL>>> entry : startupInstance.getAll()) {
			for(StartModule<SL> module : entry.getValue()) {
				if(module.isPurge()) {
					module.setInstance(null);
				}
			}
		}
	}
	
	public static <SL extends Enum<SL>> int configureStartupReflection(
			IStartModuleFactory<SL> startModuleFactory,
			StartupInstance<SL> startupInstance, 
			List<Class<?>> classes, 
			Class<SL> sl) throws RuntimeException {
		int count = 0;
		try {
			for(Class<?> clazz : classes) {
				final int mod = clazz.getModifiers();
				if(Modifier.isAbstract(mod) || Modifier.isInterface(mod)) {
					continue;
				}
				
				Startup startup = clazz.getAnnotation(Startup.class);
				if(startup == null) {
					final List<Method> methods = MethodUtils.getAnnotatedMethods(clazz, Startup.class);
					for(Method method : methods) {
						startup = method.getAnnotation(Startup.class);
						final SL key = getKey(sl, startup);
						if(key == null) {
							continue;
						}
						
						final StartModule<SL> module = startModuleFactory.create(key, clazz, method);
						startupInstance.put(key, module);
						count++;
					}
					
					continue;
				}
				
				final SL key = getKey(sl, startup);
				if(key == null) {
					continue;
				}
				
				final StartModule<SL> module = startModuleFactory.create(key, clazz);
				startupInstance.put(key, module);
				count++;
			}
			
			for(Entry<SL, List<StartModule<SL>>> entry : startupInstance.getAll()) {
				final List<StartModule<SL>> invalidModules = new ArrayList<>();
				final List<StartModule<SL>> modules = entry.getValue();
				for(StartModule<SL> module : modules) {
					final Class<?> clazz = module.getClazz();
					final Class<?>[] dependency = module.getAnnotation().dependency();
					for(Class<?> dep : dependency) {
						final Optional<StartModule<SL>> dependencyModule = modules.stream()
								.filter(m -> m.getClazz().getCanonicalName().equals(dep.getCanonicalName()))
								.findAny();
						
						if(dependencyModule.isPresent()) {
							module.addDependency(dependencyModule.get());
						} else {
							invalidModules.add(module);
							log.warn("Not found dependency ({}) for {} on {} start level.", dep.getCanonicalName(), clazz.getCanonicalName(), module.getStartLevel().name());
						}
					}
				}
				
				modules.removeAll(invalidModules);
			}
			
			//TODO n3k0nation: check to circular dependence
			
			
		} catch(Exception e) {
			throw new RuntimeException("Failed load startups!", e);
		}
		return count;
	}
	
	private static <StartLevel extends Enum<StartLevel>> StartLevel getKey(Class<StartLevel> sl, Startup startup) {
		try {
			return Enum.valueOf(sl, startup.value());
		} catch(Throwable e) {
			return null;
		}
	}
}
