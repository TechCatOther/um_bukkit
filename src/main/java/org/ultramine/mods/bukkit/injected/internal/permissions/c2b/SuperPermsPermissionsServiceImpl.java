package org.ultramine.mods.bukkit.injected.internal.permissions.c2b;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.ultramine.core.permissions.Permissions;

import javax.annotation.Nonnull;

public class SuperPermsPermissionsServiceImpl implements Permissions
{
	private final Server server;

	public SuperPermsPermissionsServiceImpl(Server server)
	{
		this.server = server;
	}

	@Override
	public boolean has(String world, String playerName, String permission)
	{
		Player player = server.getPlayer(playerName);
		return player != null && player.hasPermission(permission);
	}

	@Nonnull
	@Override
	public String getMeta(String world, String player, String key)
	{
		return "";
	}
}
