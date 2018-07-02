package org.ultramine.mods.bukkit.mixin.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinInventoryCrafting;

@Mixin(ContainerPlayer.class)
public class MixinContainerPlayer
{
	@Shadow public InventoryCrafting craftMatrix;
	@Shadow public IInventory craftResult;

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	public void ContainerPlayerInject(InventoryPlayer p_i1819_1_, boolean p_i1819_2_, EntityPlayer p_i1819_3_, CallbackInfo ci)
	{
		((IMixinInventoryCrafting) this.craftMatrix).setOwner(p_i1819_1_.player);
	}
}
