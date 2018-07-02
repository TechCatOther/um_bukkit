package org.ultramine.mods.bukkit.mixin.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.item.crafting.IMixinShapedRecipes;

@Mixin(ShapedRecipes.class)
public class MixinShapedRecipes implements IMixinShapedRecipes
{
	@Shadow private ItemStack recipeOutput;

	@Override
	public CraftShapedRecipe toBukkitRecipe()
	{
		ShapedRecipes thisShapedRecipe = (ShapedRecipes)(Object) this;
		CraftItemStack result = CraftItemStack.asCraftMirror(recipeOutput);
		CraftShapedRecipe craftShapedRecipe = new CraftShapedRecipe(result, thisShapedRecipe);
		switch (thisShapedRecipe.recipeHeight)
		{
			case 1:
				switch (thisShapedRecipe.recipeWidth)
				{
					case 1:
						craftShapedRecipe.shape("a");
						break;
					case 2:
						craftShapedRecipe.shape("ab");
						break;
					case 3:
						craftShapedRecipe.shape("abc");
						break;
				}
				break;
			case 2:
				switch (thisShapedRecipe.recipeWidth)
				{
					case 1:
						craftShapedRecipe.shape("a", "b");
						break;
					case 2:
						craftShapedRecipe.shape("ab", "cd");
						break;
					case 3:
						craftShapedRecipe.shape("abc", "def");
						break;
				}
				break;
			case 3:
				switch (thisShapedRecipe.recipeWidth)
				{
					case 1:
						craftShapedRecipe.shape("a", "b", "c");
						break;
					case 2:
						craftShapedRecipe.shape("ab", "cd", "ef");
						break;
					case 3:
						craftShapedRecipe.shape("abc", "def", "ghi");
						break;
				}
				break;
		}
		char c = 'a';
		for (ItemStack stack : thisShapedRecipe.recipeItems)
		{
			if (stack != null)
				craftShapedRecipe.setIngredient(c, org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(stack.getItem()), stack.getItemDamage());
			c++;
		}
		return craftShapedRecipe;
	}
}
