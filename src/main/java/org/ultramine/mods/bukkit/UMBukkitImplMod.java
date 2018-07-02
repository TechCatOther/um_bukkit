package org.ultramine.mods.bukkit;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.MinecraftForge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.ultramine.core.service.InjectService;
import org.ultramine.core.service.ServiceManager;
import org.ultramine.mods.bukkit.api.BukkitRegistry;
import org.ultramine.mods.bukkit.handler.ChunkPopulateHandler;
import org.ultramine.mods.bukkit.handler.CoreEventHandler;
import org.ultramine.mods.bukkit.handler.EntityEventHandler;
import org.ultramine.mods.bukkit.handler.InternalEventHandler;
import org.ultramine.mods.bukkit.handler.PlayerEventHandler;
import org.ultramine.mods.bukkit.handler.WorldEventHandler;
import org.ultramine.mods.bukkit.integration.permissions.b2c.SuperPermsReplacer;
import org.ultramine.mods.bukkit.integration.permissions.b2c.SuperPermsReplacerImpl;

@Mod(modid = "UMBukkitImpl", name = "UMBukkitImpl", version = "1.0.0", acceptableRemoteVersions = "*")
public class UMBukkitImplMod
{
	@InjectService
	private static ServiceManager services;
	@InjectService
	private static BukkitRegistry bukkitRegistry;
	private CraftServer bserver;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		services.register(BukkitRegistry.class, new BukkitRegistryLoader(), 0);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e)
	{
		bukkitRegistry.injectPlugin("org.ultramine.mods.bukkit.injected.internal.",
				new PluginDescriptionFile("ultramine_core_plugin", "1.0.0", "org.ultramine.mods.bukkit.injected.internal.InjectedUltramineCorePlugin")
						.setSoftDepend("Vault")
		);
	}

	@Mod.EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent e)
	{
		setupLoggers();
		bserver = new CraftServer(e.getServer(), e.getServer().getConfigurationManager());
		services.register(CraftPlayerCache.class, new CraftPlayerCacheImpl(bserver), 0);
		services.register(SuperPermsReplacer.class, new SuperPermsReplacerImpl(), 0);
		register(new CoreEventHandler(bserver));
		MinecraftForge.EVENT_BUS.register(new WorldEventHandler(bserver));
		MinecraftForge.EVENT_BUS.register(new EntityEventHandler(bserver));
		MinecraftForge.EVENT_BUS.register(new InternalEventHandler());
		register(new PlayerEventHandler(bserver));
		GameRegistry.registerWorldGenerator(new ChunkPopulateHandler(bserver), 0);
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent e)
	{
		bserver.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
	}

	private static void setupLoggers()
	{
		java.util.logging.Logger global = java.util.logging.Logger.getLogger("");
		global.setUseParentHandlers(false);

		for(java.util.logging.Handler handler : global.getHandlers())
		{
			global.removeHandler(handler);
		}

		global.addHandler(new org.bukkit.craftbukkit.util.ForwardLogHandler());
	}

	private static void register(Object handler)
	{
		FMLCommonHandler.instance().bus().register(handler);
		MinecraftForge.EVENT_BUS.register(handler);
	}
}
