package org.ultramine.mods.bukkit.integration.permissions.b2c;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.ultramine.core.permissions.Permissions;
import org.ultramine.core.service.InjectService;

public class UmPermissible extends PermissibleBase
{
	@InjectService private static Permissions perms;

	private final Player player;

	public UmPermissible(Player player)
	{
		super(player);
		this.player = player;
	}

	@Override
	public boolean isPermissionSet(String name)
	{
		return perms.has(player.getWorld().getName(), player.getName(), name) || super.isPermissionSet(name);
	}

	@Override
	public boolean hasPermission(String inName)
	{
		return perms.has(player.getWorld().getName(), player.getName(), inName) || super.hasPermission(inName);
	}
}
