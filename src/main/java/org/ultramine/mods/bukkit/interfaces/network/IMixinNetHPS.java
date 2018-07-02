package org.ultramine.mods.bukkit.interfaces.network;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public interface IMixinNetHPS
{
	CraftPlayer getPlayerB();

	boolean isDisconnected();

	CraftServer getCraftServer();

	void teleport(Location dest);
}
