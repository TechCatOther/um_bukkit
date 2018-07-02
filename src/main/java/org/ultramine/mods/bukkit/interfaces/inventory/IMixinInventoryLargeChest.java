package org.ultramine.mods.bukkit.interfaces.inventory;

import net.minecraft.inventory.IInventory;

public interface IMixinInventoryLargeChest
{
	IInventory getUpperChest();

	IInventory getLowerChest();
}
