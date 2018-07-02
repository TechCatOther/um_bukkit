package org.ultramine.mods.bukkit.interfaces.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.crafting.IRecipe;

public interface IMixinInventoryCrafting
{
	void setOwner(EntityPlayer inventoryOwner);

	void setCurrentRecipe(IRecipe recipe);

	IRecipe getCurrentRecipe();

	EntityPlayer getOwner();
}
