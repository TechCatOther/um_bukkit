package org.ultramine.mods.bukkit.api;

import cpw.mods.fml.common.eventhandler.Event;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class CraftPlayerCreationForgeEvent extends Event
{
	private final CraftPlayer player;

	public CraftPlayerCreationForgeEvent(CraftPlayer player)
	{
		this.player = player;
	}

	public CraftPlayer getPlayer()
	{
		return player;
	}
}
