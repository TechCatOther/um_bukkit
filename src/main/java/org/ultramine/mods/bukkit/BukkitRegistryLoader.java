package org.ultramine.mods.bukkit;

import net.minecraftforge.common.MinecraftForge;
import org.ultramine.core.service.ServiceDelegate;
import org.ultramine.core.service.ServiceProviderLoader;
import org.ultramine.mods.bukkit.api.BukkitRegistry;

public class BukkitRegistryLoader implements ServiceProviderLoader<BukkitRegistry>
{
	private BukkitRegistry instance;

	@Override
	public void load(ServiceDelegate<BukkitRegistry> service)
	{
		BukkitRegistry impl = new BukkitRegistryImpl();
		this.instance = impl;
		MinecraftForge.EVENT_BUS.register(impl);
		service.setProvider(impl);
	}

	@Override
	public void unload()
	{
		MinecraftForge.EVENT_BUS.unregister(instance);
	}
}
