package org.ultramine.mods.bukkit.interfaces.inventory;

import net.minecraft.entity.player.EntityPlayer;

public interface IMixinInventoryEnderChest
{
	EntityPlayer getOwner();

	void setOwner(EntityPlayer player);
}
