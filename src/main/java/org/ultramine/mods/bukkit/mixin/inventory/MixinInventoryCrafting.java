package org.ultramine.mods.bukkit.mixin.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.IRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinInventoryCrafting;

@Mixin(InventoryCrafting.class)
public class MixinInventoryCrafting implements IMixinInventoryCrafting
{
	private EntityPlayer inventoryOwner;
	private IRecipe currentRecipe;

	@Override
	public void setOwner(EntityPlayer inventoryOwner)
	{
		this.inventoryOwner = inventoryOwner;
	}

	@Override
	public void setCurrentRecipe(IRecipe recipe)
	{
		this.currentRecipe = recipe;
	}

	@Override
	public IRecipe getCurrentRecipe()
	{
		return currentRecipe;
	}

	@Override
	public EntityPlayer getOwner()
	{
		return inventoryOwner;
	}
}
