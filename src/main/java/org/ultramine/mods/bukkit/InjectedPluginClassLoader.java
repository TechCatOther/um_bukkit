package org.ultramine.mods.bukkit;

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.IOException;

public class InjectedPluginClassLoader extends PluginClassLoader
{
	private final LaunchClassLoader lcs;
	private final String pkg;

	public InjectedPluginClassLoader(JavaPluginLoader loader, LaunchClassLoader parent, String pkg, PluginDescriptionFile description) throws InvalidPluginException
	{
		super(loader, parent, description);
		this.lcs = parent;
		this.pkg = pkg;
		initPlugin();
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		if(name.startsWith(pkg))
		{
			byte[] classBytes;
			try {
				classBytes = lcs.getClassBytes(name);
			} catch(IOException e) {
				throw new ClassNotFoundException(name, e);
			}
			if(classBytes == null)
				throw new ClassNotFoundException(name);
			Class<?> c = defineClass(name, classBytes, 0, classBytes.length);
			if (resolve)
				resolveClass(c);
			return c;
		}
		return super.loadClass(name, resolve);
	}
}
