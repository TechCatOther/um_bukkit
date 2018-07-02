package org.bukkit.plugin.java;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.transformer.MavenShade;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.Warning;
import org.bukkit.Warning.WarningState;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.ultramine.mods.bukkit.EventImplProgress;
import org.ultramine.mods.bukkit.InjectedPluginClassLoader;
import org.ultramine.mods.bukkit.util.ClassGenUtils;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Represents a Java plugin loader, allowing plugins in the form of .jar
 */
public final class JavaPluginLoader implements PluginLoader
{
	final Server server;
	private final Pattern[] fileFilters = new Pattern[]{Pattern.compile("\\.jar$"),};
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	private final Map<String, PluginClassLoader> loaders = new LinkedHashMap<String, PluginClassLoader>();

	private final Map<Method, Class<?>> handlerCache = new HashMap<Method, Class<?>>();

	/**
	 * This class was not meant to be constructed explicitly
	 */
	@Deprecated
	public JavaPluginLoader(Server instance)
	{
		Validate.notNull(instance, "Server cannot be null");
		server = instance;
	}

	public Plugin loadPlugin(File file) throws InvalidPluginException
	{
		Validate.notNull(file, "File cannot be null");

		if(!file.exists())
		{
			throw new InvalidPluginException(new FileNotFoundException(file.getPath() + " does not exist"));
		}

		PluginDescriptionFile description;
		try
		{
			description = getPluginDescription(file);
		} catch(InvalidDescriptionException ex)
		{
			throw new InvalidPluginException(ex);
		}

		File dataFolder = new File(file.getParentFile(), description.getName());
		File oldDataFolder = getDataFolder(file);

		// Found old data folder
		if(dataFolder.equals(oldDataFolder))
		{
			// They are equal -- nothing needs to be done!
		}
		else if(dataFolder.isDirectory() && oldDataFolder.isDirectory())
		{
			server.getLogger().log(Level.INFO, String.format(
					"While loading %s (%s) found old-data folder: %s next to the new one: %s",
					description.getName(),
					file,
					oldDataFolder,
					dataFolder
			));
		}
		else if(oldDataFolder.isDirectory() && !dataFolder.exists())
		{
			if(!oldDataFolder.renameTo(dataFolder))
			{
				throw new InvalidPluginException("Unable to rename old data folder: '" + oldDataFolder + "' to: '" + dataFolder + "'");
			}
			server.getLogger().log(Level.INFO, String.format(
					"While loading %s (%s) renamed data folder: '%s' to '%s'",
					description.getName(),
					file,
					oldDataFolder,
					dataFolder
			));
		}

		if(dataFolder.exists() && !dataFolder.isDirectory())
		{
			throw new InvalidPluginException(String.format(
					"Projected datafolder: '%s' for %s (%s) exists and is not a directory",
					dataFolder,
					description.getName(),
					file
			));
		}

		List<String> depend = description.getDepend();
		if(depend == null)
		{
			depend = ImmutableList.<String>of();
		}

		for(String pluginName : depend)
		{
			if(loaders == null)
			{
				throw new UnknownDependencyException(pluginName);
			}
			PluginClassLoader current = loaders.get(pluginName);

			if(current == null)
			{
				throw new UnknownDependencyException(pluginName);
			}
		}

		PluginClassLoader loader;
		try
		{
			loader = new PluginClassLoader(this, getClass().getClassLoader(), description, dataFolder, file);
		} catch(InvalidPluginException ex)
		{
			throw ex;
		} catch(Throwable ex)
		{
			throw new InvalidPluginException(ex);
		}

		loaders.put(description.getName(), loader);

		return loader.plugin;
	}

	public Plugin loadInjectedPlugin(String pkg, PluginDescriptionFile description)
	{
		PluginClassLoader loader;
		try
		{
			loader = new InjectedPluginClassLoader(this, (LaunchClassLoader) getClass().getClassLoader(), pkg, description);
		} catch(InvalidPluginException e)
		{
			throw new RuntimeException(e);
		}

		loaders.put(description.getName(), loader);

		return loader.plugin;
	}

	private File getDataFolder(File file)
	{
		File dataFolder = null;

		String filename = file.getName();
		int index = file.getName().lastIndexOf(".");

		if(index != -1)
		{
			String name = filename.substring(0, index);

			dataFolder = new File(file.getParentFile(), name);
		}
		else
		{
			// This is if there is no extension, which should not happen
			// Using _ to prevent name collision

			dataFolder = new File(file.getParentFile(), filename + "_");
		}

		return dataFolder;
	}

	public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException
	{
		Validate.notNull(file, "File cannot be null");

		JarFile jar = null;
		InputStream stream = null;

		try
		{
			jar = new JarFile(file);
			JarEntry entry = jar.getJarEntry("plugin.yml");

			if(entry == null)
			{
				throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain plugin.yml"));
			}

			stream = jar.getInputStream(entry);

			return new PluginDescriptionFile(stream);

		} catch(IOException ex)
		{
			throw new InvalidDescriptionException(ex);
		} catch(YAMLException ex)
		{
			throw new InvalidDescriptionException(ex);
		} finally
		{
			if(jar != null)
			{
				try
				{
					jar.close();
				} catch(IOException e)
				{
				}
			}
			if(stream != null)
			{
				try
				{
					stream.close();
				} catch(IOException e)
				{
				}
			}
		}
	}

	public Pattern[] getPluginFileFilters()
	{
		return fileFilters.clone();
	}

	Class<?> getClassByName(final String name)
	{
		Class<?> cachedClass = classes.get(name);

		if(cachedClass != null)
		{
			return cachedClass;
		}
		else
		{
			for(String current : loaders.keySet())
			{
				PluginClassLoader loader = loaders.get(current);

				try
				{
					cachedClass = loader.findClass(name, false);
				} catch(ClassNotFoundException cnfe)
				{
				}
				if(cachedClass != null)
				{
					return cachedClass;
				}
			}
		}
		return null;
	}

	void setClass(final String name, final Class<?> clazz)
	{
		if(!classes.containsKey(name))
		{
			classes.put(name, clazz);

			if(ConfigurationSerializable.class.isAssignableFrom(clazz))
			{
				Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass(ConfigurationSerializable.class);
				ConfigurationSerialization.registerClass(serializable);
			}
		}
	}

	private void removeClass(String name)
	{
		Class<?> clazz = classes.remove(name);

		try
		{
			if((clazz != null) && (ConfigurationSerializable.class.isAssignableFrom(clazz)))
			{
				Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass(ConfigurationSerializable.class);
				ConfigurationSerialization.unregisterClass(serializable);
			}
		} catch(NullPointerException ex)
		{
			// Boggle!
			// (Native methods throwing NPEs is not fun when you can't stop it before-hand)
		}

		if(clazz != null)
		{
			for(Iterator<Method> it = handlerCache.keySet().iterator(); it.hasNext(); )
			{
				if(it.next().getDeclaringClass() == clazz)
					it.remove();
			}
		}
	}

	public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, final Plugin plugin)
	{
		Validate.notNull(plugin, "Plugin can not be null");
		Validate.notNull(listener, "Listener can not be null");

		boolean useTimings = server.getPluginManager().useTimings();
		Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<Class<? extends Event>, Set<RegisteredListener>>();
		Set<Method> methods;
		try
		{
			Method[] publicMethods = listener.getClass().getMethods();
			methods = new HashSet<Method>(publicMethods.length, Float.MAX_VALUE);
			for(Method method : publicMethods)
			{
				methods.add(method);
			}
			for(Method method : listener.getClass().getDeclaredMethods())
			{
				methods.add(method);
			}
		} catch(NoClassDefFoundError e)
		{
			plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
			return ret;
		}

		for(final Method method : methods)
		{
			final EventHandler eh = method.getAnnotation(EventHandler.class);
			if(eh == null) continue;
			final Class<?> checkClass;
			if(method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0]))
			{
				plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
				continue;
			}
			final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
			EventImplProgress.checkEventImplemented(eventClass, plugin);
			method.setAccessible(true);
			Set<RegisteredListener> eventSet = ret.get(eventClass);
			if(eventSet == null)
			{
				eventSet = new HashSet<RegisteredListener>();
				ret.put(eventClass, eventSet);
			}

			for(Class<?> clazz = eventClass; Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass())
			{
				// This loop checks for extending deprecated events
				if(clazz.getAnnotation(Deprecated.class) != null)
				{
					Warning warning = clazz.getAnnotation(Warning.class);
					WarningState warningState = server.getWarningState();
					if(!warningState.printFor(warning))
					{
						break;
					}
					plugin.getLogger().log(
							Level.WARNING,
							String.format(
									"\"%s\" has registered a listener for %s on method \"%s\", but the event is Deprecated." +
											" \"%s\"; please notify the authors %s.",
									plugin.getDescription().getFullName(),
									clazz.getName(),
									method.toGenericString(),
									(warning != null && warning.reason().length() != 0) ? warning.reason() : "Server performance will be affected",
									Arrays.toString(plugin.getDescription().getAuthors().toArray())),
							warningState == WarningState.ON ? new AuthorNagException(null) : null);
					break;
				}
			}

			EventExecutor executor = makeEventExecutor(eventClass, listener, method);
//			if (false) { // Spigot - RL handles useTimings check now
//				eventSet.add(new TimedRegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
//			} else {
			eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
//			}
		}
		return ret;
	}

	public void enablePlugin(final Plugin plugin)
	{
		Validate.isTrue(plugin instanceof JavaPlugin, "Plugin is not associated with this PluginLoader");

		if(!plugin.isEnabled())
		{
			plugin.getLogger().info("Enabling " + plugin.getDescription().getFullName());

			JavaPlugin jPlugin = (JavaPlugin) plugin;

			String pluginName = jPlugin.getDescription().getName();

			if(!loaders.containsKey(pluginName))
			{
				loaders.put(pluginName, (PluginClassLoader) jPlugin.getClassLoader());
			}

			try
			{
				jPlugin.setEnabled(true);
			} catch(Throwable ex)
			{
				server.getLogger().log(Level.SEVERE, "Error occurred while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
			}

			// Perhaps abort here, rather than continue going, but as it stands,
			// an abort is not possible the way it's currently written
			server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
		}
	}

	public void disablePlugin(Plugin plugin)
	{
		Validate.isTrue(plugin instanceof JavaPlugin, "Plugin is not associated with this PluginLoader");

		if(plugin.isEnabled())
		{
			String message = String.format("Disabling %s", plugin.getDescription().getFullName());
			plugin.getLogger().info(message);

			server.getPluginManager().callEvent(new PluginDisableEvent(plugin));

			JavaPlugin jPlugin = (JavaPlugin) plugin;
			ClassLoader cloader = jPlugin.getClassLoader();

			try
			{
				jPlugin.setEnabled(false);
			} catch(Throwable ex)
			{
				server.getLogger().log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
			}

			loaders.remove(jPlugin.getDescription().getName());

			if(cloader instanceof PluginClassLoader)
			{
				PluginClassLoader loader = (PluginClassLoader) cloader;
				Set<String> names = loader.getClasses();

				for(String name : names)
				{
					removeClass(name);
				}
			}
		}
	}

	// Cauldron start
	private InheritanceMap globalInheritanceMap = null;

	/**
	 * Get the inheritance map for remapping all plugins
	 */
	public InheritanceMap getGlobalInheritanceMap()
	{
		if(globalInheritanceMap == null)
		{
			Map<String, String> relocationsCurrent = new HashMap<String, String>();
			relocationsCurrent.put("net.minecraft.server", "net.minecraft.server." + PluginClassLoader.getNativeVersion());
			JarMapping currentMappings = new JarMapping();

			try
			{
				currentMappings.loadMappings(
						new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("mappings/" + PluginClassLoader.getNativeVersion() + "/cb2numpkg.srg"))),
						new MavenShade(relocationsCurrent),
						null, false);
			} catch(IOException ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}

			BiMap<String, String> inverseClassMap = HashBiMap.create(currentMappings.classes).inverse();
			globalInheritanceMap = new InheritanceMap();

			BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("mappings/" + PluginClassLoader.getNativeVersion() + "/nms.inheritmap")));

			try
			{
				globalInheritanceMap.load(reader, inverseClassMap);
			} catch(IOException ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			System.out.println("Loaded inheritance map of " + globalInheritanceMap.size() + " classes");
		}

		return globalInheritanceMap;
	}
	// Cauldron end

	private EventExecutor makeEventExecutor(final Class<? extends Event> eventClass, Listener listener, Method method)
	{
		if((method.getModifiers() & Modifier.PUBLIC) != 0)
			return makeASMEventExecutor(eventClass, listener, method);
		else
			return makeReflectEventExecutor(eventClass, method);
	}

	private EventExecutor makeReflectEventExecutor(final Class<? extends Event> eventClass, final Method method)
	{
		return new EventExecutor()
		{
			public void execute(Listener listener, Event event) throws EventException
			{
				try
				{
					if(!eventClass.isAssignableFrom(event.getClass()))
						return;

					method.invoke(listener, event);
				} catch(InvocationTargetException ex)
				{
					throw new EventException(ex.getCause());
				} catch(Throwable t)
				{
					throw new EventException(t);
				}
			}
		};
	}

	private EventExecutor makeASMEventExecutor(final Class<? extends Event> eventClass, Listener listener, Method method)
	{
		Class<?> cls = handlerCache.get(method);
		if(cls == null)
		{
			cls = ClassGenUtils.makeOneArgMethodDelegate(listener.getClass(), getUniqueHandlerName(method), method, IEventHandler.class);
			handlerCache.put(method, cls);
		}

		final IEventHandler handler;
		try
		{
			handler = (IEventHandler) cls.getConstructor(Object.class).newInstance(listener);
		} catch(Exception e)
		{
			throw new RuntimeException(e);
		}

		return new EventExecutor()
		{
			@Override
			public void execute(Listener _listener, Event event) throws EventException
			{
				if(!eventClass.isAssignableFrom(event.getClass()))
					return;
				try
				{
					handler.invoke(event);
				} catch(Throwable t)
				{
					throw new EventException(t);
				}
			}
		};
	}

	private String getUniqueHandlerName(Method method)
	{
		return String.format("%s_%s_%s_%s", getClass().getName(),
				method.getDeclaringClass().getSimpleName(),
				method.getName(),
				method.getParameterTypes()[0].getSimpleName());
	}

	public interface IEventHandler
	{
		void invoke(Event event);
	}
}