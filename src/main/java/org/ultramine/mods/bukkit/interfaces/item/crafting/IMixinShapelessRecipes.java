package org.ultramine.mods.bukkit.interfaces.item.crafting;

import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;

public interface IMixinShapelessRecipes
{
	CraftShapelessRecipe toBukkitRecipe();
}
