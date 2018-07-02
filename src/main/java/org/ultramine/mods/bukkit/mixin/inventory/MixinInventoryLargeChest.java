package org.ultramine.mods.bukkit.mixin.inventory;

import net.minecraft.inventory.IInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinInventoryLargeChest;

@Mixin(net.minecraft.inventory.InventoryLargeChest.class)
public class MixinInventoryLargeChest implements IMixinInventoryLargeChest
{
	@Shadow
	private IInventory upperChest;
	@Shadow
	private IInventory lowerChest;

	@Override
	public IInventory getUpperChest()
	{
		return upperChest;
	}

	@Override
	public IInventory getLowerChest()
	{
		return lowerChest;
	}
}
