package org.ultramine.mods.bukkit.injected.internal.permissions.c2b;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.ultramine.core.permissions.Permissions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VaultPermissionsServiceImpl implements Permissions
{
	private final Server server;
	private final Permission perms;
	private final @Nullable Chat chat;

	public VaultPermissionsServiceImpl(Server server, Permission perms, @Nullable Chat chat)
	{
		this.server = server;
		this.perms = perms;
		this.chat = chat;
	}

	@Override
	public boolean has(String world, String player, String permission)
	{
		OfflinePlayer offline = server.getOfflinePlayer(player);
		return offline != null && perms.playerHas(world, offline, permission);
	}

	@Nonnull
	@Override
	public String getMeta(String world, String player, String key)
	{
		if(chat == null)
			return "";
		OfflinePlayer offline = server.getOfflinePlayer(player);
		if(offline == null)
			return "";
		return chat.getPlayerInfoString(world, offline, key, "");
	}
}
