package org.ultramine.mods.bukkit.mixin.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryMerchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinInventoryMerchant;

@Mixin(InventoryMerchant.class)
public class MixinInventoryMerchant implements IMixinInventoryMerchant
{
	@Final
	@Shadow private EntityPlayer thePlayer;

	@Override
	public EntityPlayer getPlayer()
	{
		return thePlayer;
	}
}
