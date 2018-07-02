package org.ultramine.mods.bukkit.interfaces.world.storage;

import net.minecraft.world.storage.WorldInfo;

import java.util.UUID;

public interface IMixinSaveHandler
{
	UUID getUUID();
}
