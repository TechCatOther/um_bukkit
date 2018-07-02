package org.ultramine.mods.bukkit.handler;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.ultramine.mods.bukkit.interfaces.world.IMixinChunk;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

public class WorldEventHandler
{
	private final CraftServer server;

	public WorldEventHandler(CraftServer server)
	{
		this.server = server;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onWorldLoad(WorldEvent.Load e)
	{
		CraftWorld world = ((IMixinWorld) e.world).getWorld();
		if(e.world.provider.dimensionId == 0)
			server.scoreboardManager = new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager(MinecraftServer.getServer(), e.world.getScoreboard());
		server.getPluginManager().callEvent(new WorldInitEvent(world)); //TODO call only once per world
		server.getPluginManager().callEvent(new WorldLoadEvent(world));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onWorldUnload(WorldEvent.Unload e)
	{
		server.getPluginManager().callEvent(new WorldUnloadEvent(((IMixinWorld) e.world).getWorld()));
		server.removeWorld(((IMixinWorld) e.world).getWorld());
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Save e)
	{
		server.getPluginManager().callEvent(new WorldSaveEvent(((IMixinWorld) e.world).getWorld()));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChunkLoad(ChunkEvent.Load e)
	{
		server.getPluginManager().callEvent(new ChunkLoadEvent(((IMixinChunk) e.getChunk()).getBukkitChunk(), false)); //TODO
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onChunkUnload(ChunkEvent.Unload e)
	{
		server.getPluginManager().callEvent(new ChunkUnloadEvent(((IMixinChunk) e.getChunk()).getBukkitChunk()));
	}
}
