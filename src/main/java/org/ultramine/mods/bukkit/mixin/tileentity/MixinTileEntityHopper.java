package org.ultramine.mods.bukkit.mixin.tileentity;

import net.minecraft.block.BlockHopper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.Facing;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;
import org.ultramine.mods.bukkit.util.BukkitUtil;

import static net.minecraft.tileentity.TileEntityHopper.func_145889_a;

@Mixin(TileEntityHopper.class)
public abstract class MixinTileEntityHopper
{
	@Shadow protected abstract IInventory func_145895_l();

	@Shadow protected abstract boolean func_152102_a(IInventory p_152102_1_, int p_152102_2_);

	@Inject(method = "func_145898_a", cancellable = true, at = @At(value = "RETURN", ordinal = 0, shift = Shift.BY, by = 2))
	private static void func_145898_aInject(IInventory p_145898_0_, EntityItem p_145898_1_, CallbackInfoReturnable<Boolean> cir)
	{
		if (BukkitUtil.getInventoryOwner(p_145898_0_) != null && ((IMixinEntity) p_145898_1_).getBukkitEntity() != null)
		{
			InventoryPickupItemEvent event = new InventoryPickupItemEvent(BukkitUtil.getInventoryOwner(p_145898_0_).getInventory(), (org.bukkit.entity.Item) ((IMixinEntity) p_145898_1_).getBukkitEntity());
			((IMixinWorld) p_145898_1_.worldObj).getServer().getPluginManager().callEvent(event);
			if (event.isCancelled())
			{
				cir.setReturnValue(false);
				cir.cancel();
			}
		}
	}

	@Overwrite
	private boolean func_145883_k()
	{
		IInventory iinventory = this.func_145895_l();
		TileEntityHopper thisObject = ((TileEntityHopper) (Object) this);
		if (iinventory == null)
		{
			return false;
		}
		else
		{
			int i = Facing.oppositeSide[BlockHopper.getDirectionFromMetadata(thisObject.getBlockMetadata())];
			if (this.func_152102_a(iinventory, i))
			{
				return false;
			}
			else
			{
				for (int j = 0; j < thisObject.getSizeInventory(); ++j)
					if (thisObject.getStackInSlot(j) != null)
					{
						ItemStack itemstack = thisObject.getStackInSlot(j).copy();
						CraftItemStack oitemstack = CraftItemStack.asCraftMirror(thisObject.decrStackSize(j, 1));
						Inventory destinationInventory;

						// Have to special case large chests as they work oddly
						if (iinventory instanceof InventoryLargeChest)
						{
							destinationInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) iinventory);
						} else
						{
							InventoryHolder owner = BukkitUtil.getInventoryOwner(iinventory);
							destinationInventory = owner != null ? owner.getInventory() : null;
						}
						InventoryMoveItemEvent event = new InventoryMoveItemEvent(BukkitUtil.getInventoryOwner(thisObject).getInventory(), oitemstack.clone(), destinationInventory, true);
						Bukkit.getPluginManager().callEvent(event);
						if (event.isCancelled())
						{
							thisObject.setInventorySlotContents(j, itemstack);
							return false;
						}
						ItemStack itemstack1 = func_145889_a(iinventory, CraftItemStack.asNMSCopy(event.getItem()), i);
						if (itemstack1 == null || itemstack1.stackSize == 0)
						{
							iinventory.markDirty();
							return true;
						}
						thisObject.setInventorySlotContents(j, itemstack);
					}
				return false;
			}
		}
	}

	@Shadow
	private static boolean func_145890_b(IInventory p_145890_0_, ItemStack p_145890_1_, int p_145890_2_, int p_145890_3_)
	{
		return false;
	}

	@Overwrite
	private static boolean func_145892_a(IHopper hopper, IInventory inventory, int slotId, int p_145892_3_)
	{
		ItemStack itemstack = inventory.getStackInSlot(slotId);
		if (itemstack != null && func_145890_b(inventory, itemstack, slotId, p_145892_3_))
		{
			ItemStack itemstack1 = itemstack.copy();
			CraftItemStack oitemstack = CraftItemStack.asCraftMirror(inventory.decrStackSize(slotId, 1));
			Inventory sourceInventory = null;

			// Have to special case large chests as they work oddly
			if (inventory instanceof InventoryLargeChest)
			{
				sourceInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) inventory);
			}
			else
			{
				InventoryHolder owner = BukkitUtil.getInventoryOwner(inventory);
				sourceInventory = owner != null ? owner.getInventory() : null;
			}

			InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, oitemstack.clone(), BukkitUtil.getInventoryOwner(hopper).getInventory(), false);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled())
			{
				inventory.setInventorySlotContents(slotId, itemstack1);
				return false;
			}
			ItemStack itemstack2 = func_145889_a(hopper, CraftItemStack.asNMSCopy(event.getItem()), -1);
			if (itemstack2 == null || itemstack2.stackSize == 0)
			{
				inventory.markDirty();
				return true;
			}
			inventory.setInventorySlotContents(slotId, itemstack1);
		}
		return false;
	}
}
