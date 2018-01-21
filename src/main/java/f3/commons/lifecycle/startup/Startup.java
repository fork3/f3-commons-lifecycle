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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Марка. Помеченные классы будут созданы во время загрузки.
 * 
 * Загрузчик сканирует класс на наличие статического метода getInstance и если он присутствует, то
 *  инстанс объекта будет получен через данный метод.
 *  
 * Если объект не имеет марки getInstance метода, то требуется убедится, что такой объект не будет пересоздаваться,
 *  при изменении каких-либо параметров, иначе произойдет утечка памяти, так как один из созданных объектов
 *  хранится жеской ссылкой внутри StartupInstance (purge флаг отменяет хранение жесткой ссылки и очищает созданные
 *  инстансы объекта в конце загрузки, либо при вызове purge).
 *  
 * Важно не наебаться, так как в марке используется String-значение имени элемента из enum'а и если оно 
 *  не будет найдено, то загрузка не произойдет!
 * 
 * @author n3k0nation
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Startup {
	/** Startup level */
	String value();
	
	/** Dependency */
	Class<?>[] dependency() default {};
	
	boolean last() default false;
	boolean purge() default false;
	
	boolean singleton() default false;
}
