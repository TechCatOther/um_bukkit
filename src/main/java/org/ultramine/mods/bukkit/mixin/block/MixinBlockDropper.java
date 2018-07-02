package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockDropper;
import net.minecraft.block.BlockSourceImpl;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;
import org.ultramine.mods.bukkit.util.BukkitUtil;

@Mixin(BlockDropper.class)
public class MixinBlockDropper
{
	private final IBehaviorDispenseItem behaviorDispenseItem = new BehaviorDefaultDispenseItem();

	@Overwrite
	protected void func_149941_e(World world, int x, int y, int z)
	{
		BlockSourceImpl blocksourceimpl = new BlockSourceImpl(world, x, y, z);
		TileEntityDispenser tileentitydispenser = (TileEntityDispenser) blocksourceimpl.getBlockTileEntity();
		if (tileentitydispenser != null)
		{
			int l = tileentitydispenser.func_146017_i();
			if (l < 0)
			{
				world.playAuxSFX(1001, x, y, z, 0);
			}
			else
			{
				ItemStack itemstack = tileentitydispenser.getStackInSlot(l);
				int i1 = world.getBlockMetadata(x, y, z) & 7;
				IInventory iinventory = TileEntityHopper.func_145893_b(world, x + Facing.offsetsXForSide[i1], y + Facing.offsetsYForSide[i1], z + Facing.offsetsZForSide[i1]);
				ItemStack itemstack1;
				if (iinventory != null)
				{
					CraftItemStack oitemstack = CraftItemStack.asCraftMirror(itemstack.copy().splitStack(1));
					org.bukkit.inventory.Inventory destinationInventory;
					if (iinventory instanceof InventoryLargeChest)
						destinationInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) iinventory);
					else
						destinationInventory = BukkitUtil.getInventoryOwner(iinventory).getInventory();
					InventoryMoveItemEvent event = new InventoryMoveItemEvent(BukkitUtil.getInventoryOwner(tileentitydispenser).getInventory(), oitemstack.clone(), destinationInventory, true);
					((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
					if (event.isCancelled())
						return;
					itemstack1 = TileEntityHopper.func_145889_a(iinventory, CraftItemStack.asNMSCopy(event.getItem()), Facing.oppositeSide[i1]);
					if (event.getItem().equals(oitemstack) && itemstack1 == null)
					{
						itemstack1 = itemstack.copy();
						if (--itemstack1.stackSize == 0)
							itemstack1 = null;
					}
					else
					{
						itemstack1 = itemstack.copy();
					}
				}
				else
				{
					itemstack1 = this.behaviorDispenseItem.dispense(blocksourceimpl, itemstack);
					if (itemstack1 != null && itemstack1.stackSize == 0)
						itemstack1 = null;
				}
				tileentitydispenser.setInventorySlotContents(l, itemstack1);
			}
		}
	}
}
