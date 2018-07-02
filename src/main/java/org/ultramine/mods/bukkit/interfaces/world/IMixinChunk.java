package org.ultramine.mods.bukkit.interfaces.world;

import org.bukkit.craftbukkit.CraftChunk;

public interface IMixinChunk
{
	CraftChunk getBukkitChunk();

	void setBukkitChunk(CraftChunk bukkitChunk);
}
