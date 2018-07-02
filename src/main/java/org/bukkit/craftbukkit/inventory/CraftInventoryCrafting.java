package org.bukkit.craftbukkit.inventory;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinInventoryCrafting;
import org.ultramine.mods.bukkit.interfaces.item.crafting.IMixinShapedRecipes;
import org.ultramine.mods.bukkit.interfaces.item.crafting.IMixinShapelessRecipes;
import org.ultramine.mods.bukkit.util.BukkitUtil;
import org.ultramine.mods.bukkit.util.CustomModRecipe;

import java.util.Arrays;


public class CraftInventoryCrafting extends CraftInventory implements CraftingInventory
{
	private final net.minecraft.inventory.IInventory resultInventory;

	public CraftInventoryCrafting(net.minecraft.inventory.InventoryCrafting inventory, net.minecraft.inventory.IInventory resultInventory)
	{
		super(inventory);
		this.resultInventory = resultInventory;
	}

	public net.minecraft.inventory.IInventory getResultInventory()
	{
		return resultInventory;
	}

	public net.minecraft.inventory.IInventory getMatrixInventory()
	{
		return inventory;
	}

	@Override
	public int getSize()
	{
		return getResultInventory().getSizeInventory() + getMatrixInventory().getSizeInventory();
	}

	@Override
	public void setContents(ItemStack[] items)
	{
		int resultLen = getResultInventory().getSizeInventory();
		int len = getMatrixInventory().getSizeInventory() + resultLen;
		if(len > items.length)
		{
			throw new IllegalArgumentException("Invalid inventory size; expected " + len + " or less");
		}
		setContents(items[0], Arrays.copyOfRange(items, 1, items.length));
	}

	@Override
	public ItemStack[] getContents()
	{
		ItemStack[] items = new ItemStack[getSize()];
		net.minecraft.item.ItemStack[] mcResultItems = BukkitUtil.getVanillaContents(getResultInventory());

		int i = 0;
		for(i = 0; i < mcResultItems.length; i++)
		{
			items[i] = CraftItemStack.asCraftMirror(mcResultItems[i]);
		}

		net.minecraft.item.ItemStack[] mcItems = BukkitUtil.getVanillaContents(getMatrixInventory());

		for(int j = 0; j < mcItems.length; j++)
		{
			items[i + j] = CraftItemStack.asCraftMirror(mcItems[j]);
		}

		return items;
	}

	public void setContents(ItemStack result, ItemStack[] contents)
	{
		setResult(result);
		setMatrix(contents);
	}

	@Override
	public CraftItemStack getItem(int index)
	{
		if(index < getResultInventory().getSizeInventory())
		{
			net.minecraft.item.ItemStack item = getResultInventory().getStackInSlot(index);
			return item == null ? null : CraftItemStack.asCraftMirror(item);
		}
		else
		{
			net.minecraft.item.ItemStack item = getMatrixInventory().getStackInSlot(index - getResultInventory().getSizeInventory());
			return item == null ? null : CraftItemStack.asCraftMirror(item);
		}
	}

	@Override
	public void setItem(int index, ItemStack item)
	{
		if(index < getResultInventory().getSizeInventory())
		{
			getResultInventory().setInventorySlotContents(index, (item == null ? null : CraftItemStack.asNMSCopy(item)));
		}
		else
		{
			getMatrixInventory().setInventorySlotContents((index - getResultInventory().getSizeInventory()), (item == null ? null : CraftItemStack.asNMSCopy(item)));
		}
	}

	public ItemStack[] getMatrix()
	{
		ItemStack[] items = new ItemStack[getSize()];
		net.minecraft.item.ItemStack[] matrix = BukkitUtil.getVanillaContents(getMatrixInventory());

		for(int i = 0; i < matrix.length; i++)
		{
			items[i] = CraftItemStack.asCraftMirror(matrix[i]);
		}

		return items;
	}

	public ItemStack getResult()
	{
		net.minecraft.item.ItemStack item = getResultInventory().getStackInSlot(0);
		if(item != null) return CraftItemStack.asCraftMirror(item);
		return null;
	}

	public void setMatrix(ItemStack[] contents)
	{
		net.minecraft.inventory.IInventory inv = getMatrixInventory();
		int size = inv.getSizeInventory();
		if(size > contents.length)
		{
			throw new IllegalArgumentException("Invalid inventory size; expected " + size + " or less");
		}

		for(int i = 0; i < size; i++)
		{
			if(i < contents.length)
			{
				ItemStack item = contents[i];
				if(item == null || item.getTypeId() <= 0)
				{
					inv.setInventorySlotContents(i, null);
				}
				else
				{
					inv.setInventorySlotContents(i, CraftItemStack.asNMSCopy(item));
				}
			}
			else
			{
				inv.setInventorySlotContents(i, null);
			}
		}
	}

	public void setResult(ItemStack item)
	{
		if(item == null || item.getTypeId() <= 0)
		{
			getResultInventory().setInventorySlotContents(0, null);
		}
		else
		{
			getResultInventory().setInventorySlotContents(0, CraftItemStack.asNMSCopy(item));
		}
	}

	public Recipe getRecipe()
	{
		IRecipe recipe = ((IMixinInventoryCrafting) this.getInventory()).getCurrentRecipe();
		if (recipe != null)
		{
			if (recipe instanceof ShapelessRecipes)
				return ((IMixinShapelessRecipes) recipe).toBukkitRecipe();
			else if(recipe instanceof ShapedRecipe)
				return ((IMixinShapedRecipes) recipe).toBukkitRecipe();
		}
		return new CustomModRecipe(recipe);
	}
}
