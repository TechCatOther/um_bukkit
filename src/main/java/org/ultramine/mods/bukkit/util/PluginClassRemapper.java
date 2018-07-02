package org.ultramine.mods.bukkit.util;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;

public class PluginClassRemapper extends JarRemapper
{
	public PluginClassRemapper(JarMapping jarMapping)
	{
		super(jarMapping);
	}

	@Override
	public String mapSignature(String signature, boolean typeSignature)
	{
		try
		{
			return super.mapSignature(signature, typeSignature);
		} catch(Exception e)
		{
			return signature;
		}
	}
}
