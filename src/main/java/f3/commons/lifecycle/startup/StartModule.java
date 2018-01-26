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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author n3k0nation
 *
 */
@Slf4j
public class StartModule<StartLevel extends Enum<StartLevel>> {
	private final List<StartModule<StartLevel>> dependency = new ArrayList<>(0);
	
	@Getter private final StartLevel startLevel;
	@Getter private final Class<?> clazz;
	@Getter private final Method method;
	@Getter private final Startup annotation;
	@Getter @Setter(AccessLevel.PROTECTED) private Object instance;
	
	public StartModule(StartLevel startLevel, Class<?> clazz) {
		this(startLevel, clazz, null);
	}
	
	public StartModule(StartLevel startLevel, Class<?> clazz, Method method) {
		this.startLevel = startLevel;
		this.clazz = clazz;
		this.method = method;
		annotation = method != null ? method.getAnnotation(Startup.class) : clazz.getAnnotation(Startup.class);
	}
	
	public boolean isLast() {
		return annotation.last();
	}
	
	public boolean isPurge() {
		return annotation.purge();
	}
	
	public boolean isSingleton() {
		return annotation.singleton();
	}
	
	public void addDependency(StartModule<StartLevel> module) {
		dependency.add(module);
	}
	
	public boolean isEmptyDependency() {
		return dependency.isEmpty();
	}
	
	public void init() {
		if(instance != null) { //cannot allow to secondary init
			return;
		}
		
		for(StartModule<StartLevel> depend : dependency) {
			depend.init();
		}
		
		if(Modifier.isStatic(method.getModifiers())) {
			try {
				instance = method.invoke(null);
			} catch (ReflectiveOperationException e) {
			}
			
			if(instance == null) {
				log.warn("Failed to invoke {} in {}", method.getName(), clazz.getCanonicalName());
			}
			return;
		}
		
		if(isSingleton()) {
			Method method;
			try {
				method = clazz.getDeclaredMethod("getInstance");
			} catch (NoSuchMethodException e) {
				try {
					method = clazz.getDeclaredMethod("get");
				} catch(NoSuchMethodException e1) {
					log.warn("Failed create new instance from '{}' class.", clazz.getCanonicalName());
					return;
				}
			}

			try {
				instance = method.invoke(null);
			} catch (Throwable e) {
				log.warn("Failed invoke singleton method from '{}' class.", clazz.getCanonicalName(), e);
				return;
			}
			
			if(instance == null || !clazz.isInstance(instance)) {
				log.warn("Class '{}' marked as singleton, but doesnt support singleton pattern!", clazz.getCanonicalName());
				return;
			}
		} else {
			try {
				instance = clazz.newInstance();
			} catch(Throwable e) {
				log.warn("Failed create new instance from '{}' class. Default constructor not found.", clazz.getCanonicalName());
				return;
			}
		}
	}
}
