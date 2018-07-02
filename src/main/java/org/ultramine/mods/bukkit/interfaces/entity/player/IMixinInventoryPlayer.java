package org.ultramine.mods.bukkit.interfaces.entity.player;

import net.minecraft.item.ItemStack;

public interface IMixinInventoryPlayer
{
	int canHold(ItemStack itemstack);

	void dropAllItemsWithoutClear();
}
