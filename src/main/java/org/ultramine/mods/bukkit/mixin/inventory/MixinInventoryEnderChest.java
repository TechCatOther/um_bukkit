package org.ultramine.mods.bukkit.mixin.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import org.spongepowered.asm.mixin.Mixin;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinInventoryEnderChest;

@Mixin(InventoryEnderChest.class)
public class MixinInventoryEnderChest implements IMixinInventoryEnderChest
{
	private EntityPlayer inventoryOwner;

	@Override
	public EntityPlayer getOwner()
	{
		return this.inventoryOwner;
	}

	@Override
	public void setOwner(EntityPlayer player)
	{
		this.inventoryOwner = player;
	}
}
