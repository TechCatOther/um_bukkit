package org.ultramine.mods.bukkit.mixin.entity.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.player.IMixinInventoryPlayer;

@Mixin(net.minecraft.entity.player.InventoryPlayer.class)
public abstract class MixinInventoryPlayer implements IMixinInventoryPlayer
{
	@Shadow
	public ItemStack[] mainInventory;
	@Shadow
	public ItemStack[] armorInventory;
	@Shadow
	public EntityPlayer player;

	@Shadow
	public abstract int getInventoryStackLimit();

	@Override
	public int canHold(ItemStack itemstack)
	{
		int remains = itemstack.stackSize;

		for(int i = 0; i < this.mainInventory.length; ++i)
		{
			if(this.mainInventory[i] == null)
			{
				return itemstack.stackSize;
			}

			// Taken from firstPartial(ItemStack)
			if(this.mainInventory[i] != null && this.mainInventory[i].getItem() == itemstack.getItem() && this.mainInventory[i].isStackable() && this.mainInventory[i].stackSize < this.mainInventory[i].getMaxStackSize() && this.mainInventory[i].stackSize < this.getInventoryStackLimit() && (!this.mainInventory[i].getHasSubtypes() || this.mainInventory[i].getItemDamage() == itemstack.getItemDamage()) && ItemStack.areItemStackTagsEqual(this.mainInventory[i], itemstack))
			{
				remains -= (this.mainInventory[i].getMaxStackSize() < this.getInventoryStackLimit() ? this.mainInventory[i].getMaxStackSize() : this.getInventoryStackLimit()) - this.mainInventory[i].stackSize;
			}

			if(remains <= 0)
			{
				return itemstack.stackSize;
			}
		}

		return itemstack.stackSize - remains;
	}

	@Override
	public void dropAllItemsWithoutClear()
	{
		int i;

		for(i = 0; i < this.mainInventory.length; ++i)
		{
			if(this.mainInventory[i] != null)
			{
				this.player.func_146097_a(this.mainInventory[i], true, false);
				//this.mainInventory[i] = null; // Cauldron - we clear this in EntityPlayerMP.onDeath after PlayerDeathEvent
			}
		}

		for(i = 0; i < this.armorInventory.length; ++i)
		{
			if(this.armorInventory[i] != null)
			{
				this.player.func_146097_a(this.armorInventory[i], true, false);
				//this.armorInventory[i] = null; // Cauldron - we clear this in EntityPlayerMP.onDeath after PlayerDeathEvent
			}
		}
	}
}
