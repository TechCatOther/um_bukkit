package org.ultramine.mods.bukkit.interfaces.item.crafting;

import org.bukkit.craftbukkit.inventory.CraftShapedRecipe;

public interface IMixinShapedRecipes
{
	CraftShapedRecipe toBukkitRecipe();
}
