package org.ultramine.mods.bukkit;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;
import org.ultramine.mods.bukkit.api.BukkitRegistry;
import org.ultramine.mods.bukkit.api.BukkitStateForgeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BukkitRegistryImpl implements BukkitRegistry
{
	private final List<InjectedPluginRegistration> injectedPlugins = new ArrayList<InjectedPluginRegistration>();
	private @Nullable CraftServer server;

	@Override
	public void injectPlugin(String pkg, PluginDescriptionFile description)
	{
		if(server != null) // TODO add post init registration
			throw new IllegalStateException("Bukkit server is already init. You should inject bukkit plugins on LOAD forge state");
		InjectedPluginRegistration reg = new InjectedPluginRegistration(pkg, description);
		injectedPlugins.add(reg);
	}

	@SubscribeEvent
	public void onPrePluginsLoad(BukkitStateForgeEvent.PluginsLoad.Pre e)
	{
		CraftServer server = this.server = e.getServer();
		for(InjectedPluginRegistration reg : injectedPlugins)
			injectPlugin(server, reg);
	}

	private void injectPlugin(CraftServer server, InjectedPluginRegistration reg)
	{
		((SimplePluginManager) server.getPluginManager()).loadInjectedPlugin(reg.pkg, reg.description);
	}

	private static class InjectedPluginRegistration
	{
		private final String pkg;
		private final PluginDescriptionFile description;

		private InjectedPluginRegistration(String pkg, PluginDescriptionFile description)
		{
			this.pkg = pkg;
			this.description = description;
		}
	}
}
