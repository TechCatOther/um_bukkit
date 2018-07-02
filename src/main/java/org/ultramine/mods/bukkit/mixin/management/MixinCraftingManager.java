package org.ultramine.mods.bukkit.mixin.management;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinInventoryCrafting;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingManager.class)
public class MixinCraftingManager
{
	@Inject(method = "findMatchingRecipe", cancellable = true, at = @At(value = "RETURN", ordinal = 0), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	public void findMatchingRecipeInject(InventoryCrafting inventoryCrafting, World world, CallbackInfoReturnable<ItemStack> cir, int i, ItemStack itemstack, ItemStack itemstack1, int p, Item item, int j1, int k, int l, int i1)
	{
		ItemStack result = new ItemStack(itemstack.getItem(), 1, i1);
		List<ItemStack> ingredients = new ArrayList<ItemStack>();
		ingredients.add(itemstack.copy());
		ingredients.add(itemstack1.copy());
		ShapelessRecipes currentRecipe = new ShapelessRecipes(result.copy(), ingredients);
		((IMixinInventoryCrafting) inventoryCrafting).setCurrentRecipe(currentRecipe);
		// TODO: callPreCraftEvent
		cir.setReturnValue(result);
		cir.cancel();
	}

	@Inject(method = "findMatchingRecipe", at = @At(value = "INVOKE", target = "Lorg/ultramine/server/RecipeCache;findRecipe(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/item/crafting/IRecipe;", shift = Shift.BY, by = 3), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	public void findMatchingRecipeInject(InventoryCrafting inventoryCrafting, World world, CallbackInfoReturnable<ItemStack> cir, int i, ItemStack itemstack, ItemStack itemstack1, int p, IRecipe recipe)
	{
		if (recipe != null)
		{
			if (recipe.matches(inventoryCrafting, world))
				((IMixinInventoryCrafting) inventoryCrafting).setCurrentRecipe(recipe);
			// TODO: callPreCraftEvent
		}
	}
}
