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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author n3k0nation
 *
 */
public class StartupInstance<StartLevel extends Enum<StartLevel>> {
	private final HashMap<StartLevel, List<StartModule<StartLevel>>> startTable = new HashMap<>();
	
	protected StartupInstance() {
		
	}
	
	protected void put(StartLevel level, StartModule<StartLevel> module) {
		List<StartModule<StartLevel>> invokes = startTable.get(level);
		if(invokes == null) {
			startTable.put(level, invokes = new ArrayList<>());
		}
		
		invokes.add(module);
	}
	
	protected List<StartModule<StartLevel>> get(StartLevel level) {
		return startTable.get(level);
	}
	
	protected Collection<Entry<StartLevel, List<StartModule<StartLevel>>>> getAll() {
		return startTable.entrySet();
	}
	
	public void runLevel(StartLevel level) {
		List<StartModule<StartLevel>> list = startTable.get(level);
		if(list == null) {
			return;
		}
		
		final List<StartModule<StartLevel>> lastModules = new ArrayList<>();
		for(StartModule<StartLevel> module : list) {
			if(module.isLast() && module.isEmptyDependency()) {
				lastModules.add(module);
				continue;
			}
			module.init();
		}
		
		for(StartModule<StartLevel> module : list) {
			module.init();
		}
	}
}
