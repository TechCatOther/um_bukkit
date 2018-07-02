package org.ultramine.mods.bukkit.api;

import org.bukkit.plugin.PluginDescriptionFile;
import org.ultramine.core.service.Service;

@Service
public interface BukkitRegistry
{
	void injectPlugin(String pkg, PluginDescriptionFile description);
}
