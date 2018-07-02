package org.ultramine.mods.bukkit.injected.internal.permissions.c2b;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.ultramine.core.permissions.Permissions;
import org.ultramine.core.service.ServiceBytecodeAdapter;
import org.ultramine.core.service.ServiceDelegate;
import org.ultramine.core.service.ServiceProviderLoader;
import org.ultramine.mods.bukkit.integration.permissions.b2c.SuperPermsReplacer;

import javax.annotation.Nullable;

public class BukkitPermissionsServiceLoader implements ServiceProviderLoader<Permissions>
{
	private static SuperPermsReplacer superPermsReplacer = (SuperPermsReplacer) ServiceBytecodeAdapter.provideService(SuperPermsReplacer.class);
	private final Server server;
	private boolean superPerms;

	public BukkitPermissionsServiceLoader(Server server)
	{
		this.server = server;
	}

	@Override
	public void load(ServiceDelegate<Permissions> service)
	{
		@Nullable RegisteredServiceProvider<Permission> regPerms = server.getServicesManager().getRegistration(Permission.class);
		@Nullable RegisteredServiceProvider<Chat> regChat = server.getServicesManager().getRegistration(Chat.class);
		if(regPerms == null)
			return;
		Permission perms = regPerms.getProvider();
		if(perms == null)
			return;
		superPerms = perms.getClass().getName().endsWith("SuperPerms");
		if(superPerms)
			superPermsReplacer.setEnabled(false);
		Chat chat = regChat == null ? null : regChat.getProvider();
		service.setProvider(new VaultPermissionsServiceImpl(server, perms, chat));
	}

	@Override
	public void unload()
	{
		if(superPerms)
			superPermsReplacer.setEnabled(true);
	}
}
