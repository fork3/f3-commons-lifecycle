# f3-commons-lifecycle
## Startup example
### Create entry point
```java
public static void main(String...args) {
	StartupInstance<StartLevel> startup = StartupManager.createStartup();
	List<Class<?>> classes = ClasspathScanner.getClasses(
		(p, c) -> ReflectionPath.defaultPathList.stream().anyMatch(path -> p.startsWith(path)), 
		ClasspathScanner.getClassPathURL()
	);
	StartupManager.configureStartupReflection(StartModule::new, startup, classes, StartLevel.class);

	for(StartLevel level : StartLevel.values()) {
		startup.runLevel(level);
	}
	StartupManager.purgeStartup(startup);
}
```

### Startup levels
```java
enum StartLevel {
	SomeLevel,
	OtherLevel;
}
```

### Startup class
```java
@Startup("SomeLevel")
public class Foo {
	public Foo() {
		System.out.println("Foo created!");
	}
}
```

For singleton:

```java
@Startup(value="OtherLevel", singleton=true)
public class Bar {
	private static Bar instance;
	public static Bar getInstance() {
		return instance == null ? instance = new Bar() : instance;
	}
  
	private Bar() {
		System.out.println("Bar created!");
	}
}
```

### Dependency from other class
Now bar depends from Foo.

```java
@Startup("SomeLevel")
public class Foo {
	public Foo() {
		System.out.println("Foo created!");
	}
}

@Startup(value="SomeLevel", dependency={Foo.class})
public class Bar {
	public Bar() {
		System.out.println("Bar created!");
	}
}
```

Result:

```
Foo created!
Bar created!
```

## DI and other stuff in startup
```java
public class DIStartModule extends StartModule<StartLevel> {
	public ExtendedStartupModule(StartLevel startLevel, Class<?> clazz) {
		super(startLevel, clazz);
	}
	
	@Override
	public void init() {
		if(getInstance() != null) { // prevent secondary init
			return;
		}
		
		super.init(); // create instance
		
		final Object instance = getInstance();
		if(instance != null) {
			final Injector injector = InjectorUtils.createInjectorByDefaultPath(); // get injector instance
			injector.inject(instance); // inject values to object
		}
		
		// other stuff
	}
}
```

Dont forget change startup init: 

`StartupManager.configureStartupReflection(DIStartModule::new, startup, classes, StartLevel.class);`