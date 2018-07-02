package org.ultramine.mods.bukkit.interfaces.management;

import net.minecraft.server.management.UserListEntry;

import java.util.Collection;

public interface IMixinUserList
{
	public Collection<UserListEntry> getValues();
}
