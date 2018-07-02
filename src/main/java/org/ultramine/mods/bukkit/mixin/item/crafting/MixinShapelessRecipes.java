package org.ultramine.mods.bukkit.mixin.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.item.crafting.IMixinShapelessRecipes;

import java.util.List;

@Mixin(ShapelessRecipes.class)
public class MixinShapelessRecipes implements IMixinShapelessRecipes
{
	@Final
	@Shadow private ItemStack recipeOutput;

	@Override
	@SuppressWarnings("unchecked")
	public CraftShapelessRecipe toBukkitRecipe()
	{
		ShapelessRecipes thisShapelessRecipe = (ShapelessRecipes)(Object) this;
		CraftItemStack result = CraftItemStack.asCraftMirror(recipeOutput);
		CraftShapelessRecipe craftShapelessRecipe = new CraftShapelessRecipe(result, thisShapelessRecipe);
		for (ItemStack stack : (List<ItemStack>) thisShapelessRecipe.recipeItems)
			if (stack != null)
				craftShapelessRecipe.addIngredient(org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(stack.getItem()), stack.getItemDamage());
		return  craftShapelessRecipe;
	}
}
