package org.ultramine.mods.bukkit.mixin.tileentity;

import net.minecraft.block.BlockFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(TileEntityFurnace.class)
public abstract class MixinTileEntityFurnace extends TileEntity
{
	@Shadow private ItemStack[] furnaceItemStacks;
	@Shadow public int currentItemBurnTime;
	@Shadow public int furnaceBurnTime;
	@Shadow public int furnaceCookTime;

	@Shadow protected abstract boolean canSmelt();

	@Shadow public abstract boolean isBurning();

	/**
	 * @author CraftBukkit Team
	 */
	@Overwrite
	public void updateEntity()
	{
		boolean flag = this.furnaceBurnTime > 0;
		boolean flag1 = false;

		// CraftBukkit - moved from below
		if (this.isBurning() && this.canSmelt())
		{
			++this.furnaceCookTime;

			if (this.furnaceCookTime == 200)
			{
				this.furnaceCookTime = 0;
				this.smeltItem();
				flag1 = true;
			}
		}
		else
		{
			this.furnaceCookTime = 0;
		}

		if (this.furnaceBurnTime > 0)
		{
			--this.furnaceBurnTime;
		}

		if (!this.worldObj.isRemote)
		{
			if (this.furnaceBurnTime != 0 || this.furnaceItemStacks[1] != null && this.furnaceItemStacks[0] != null)
			{
				//CraftBukkit start
				if (this.furnaceBurnTime <= 0 && this.canSmelt()) // CraftBukkit - == to <=
				{
					ItemStack itemstack = this.furnaceItemStacks[1];
					CraftItemStack fuel = CraftItemStack.asCraftMirror(itemstack);
					FurnaceBurnEvent furnaceBurnEvent = new FurnaceBurnEvent(((IMixinWorld) this.worldObj).getWorld().getBlockAt(this.xCoord, this.yCoord, this.zCoord),  fuel, TileEntityFurnace.getItemBurnTime(itemstack));
					((IMixinWorld) this.worldObj).getServer().getPluginManager().callEvent(furnaceBurnEvent);

					if (furnaceBurnEvent.isCancelled())
					{
						return;
					}

					this.currentItemBurnTime = furnaceBurnEvent.getBurnTime();
					this.furnaceBurnTime += this.currentItemBurnTime;

					if (this.furnaceBurnTime > 0 && furnaceBurnEvent.isBurning())
					//CraftBukkit end
					{
						flag1 = true;

						if (this.furnaceItemStacks[1] != null)
						{
							--this.furnaceItemStacks[1].stackSize;

							if (this.furnaceItemStacks[1].stackSize == 0)
							{
								this.furnaceItemStacks[1] = furnaceItemStacks[1].getItem().getContainerItem(furnaceItemStacks[1]);
							}
						}
					}
				}
			}

			if (flag != this.furnaceBurnTime > 0)
			{
				flag1 = true;
				BlockFurnace.updateFurnaceBlockState(this.furnaceBurnTime > 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			}
		}

		if (flag1)
		{
			this.markDirty();
		}
	}

	/**
	 * @author CraftBukkit Team
	 */
	@Overwrite
	public void smeltItem()
	{
		if (this.canSmelt())
		{
			ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[0]);

			// CraftBukkit start - fire FurnaceSmeltEvent
			CraftItemStack source = CraftItemStack.asCraftMirror(this.furnaceItemStacks[0]);
			org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemstack);

			FurnaceSmeltEvent furnaceSmeltEvent = new FurnaceSmeltEvent(((IMixinWorld) this.worldObj).getWorld().getBlockAt(this.xCoord, this.yCoord, this.zCoord), source, result);
			((IMixinWorld) this.worldObj).getServer().getPluginManager().callEvent(furnaceSmeltEvent);

			if (furnaceSmeltEvent.isCancelled())
			{
				return;
			}

			result = furnaceSmeltEvent.getResult();
			itemstack = CraftItemStack.asNMSCopy(result);

			if (itemstack != null)
			{
				if (this.furnaceItemStacks[2] == null)
				{
					this.furnaceItemStacks[2] = itemstack.copy();
				}
				else if (CraftItemStack.asCraftMirror(this.furnaceItemStacks[2]).isSimilar(result))
				{
					this.furnaceItemStacks[2].stackSize += itemstack.stackSize; // Forge BugFix: Results may have multiple items
				}
			}
			// CraftBukkit end

			--this.furnaceItemStacks[0].stackSize;

			if (this.furnaceItemStacks[0].stackSize <= 0)
			{
				this.furnaceItemStacks[0] = null;
			}
		}
	}
}
