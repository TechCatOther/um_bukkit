package org.ultramine.mods.bukkit.mixin.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinInventoryCrafting;

@Mixin(ContainerWorkbench.class)
public class MixinContainerWorkbench
{
	@Shadow public InventoryCrafting craftMatrix;
	@Shadow public IInventory craftResult;

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	public void ContainerWorkbenchInject(InventoryPlayer p_i1808_1_, World p_i1808_2_, int p_i1808_3_, int p_i1808_4_, int p_i1808_5_, CallbackInfo ci)
	{
		((IMixinInventoryCrafting) this.craftMatrix).setOwner(p_i1808_1_.player);
	}
}
