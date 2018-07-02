package org.ultramine.mods.bukkit.mixin.init;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(targets = "net/minecraft/init/Bootstrap$10")
public class MixinBootstrapWaterLavaBucket
{
	@Final
	@Shadow private BehaviorDefaultDispenseItem field_150841_b;

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
	{
		ItemBucket itembucket = (ItemBucket) itemStack.getItem();
		int i = blockSource.getXInt();
		int j = blockSource.getYInt();
		int k = blockSource.getZInt();
		EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
		World world = blockSource.getWorld();
		int x = i + enumfacing.getFrontOffsetX();
		int y = j + enumfacing.getFrontOffsetY();
		int z = k + enumfacing.getFrontOffsetZ();
		if (world.isAirBlock(x, y, z) || !world.getBlock(x, y, z).getMaterial().isSolid())
		{
			org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(i, j, k);
			CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemStack);
			BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(x, y, z));
			((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
			if (event.isCancelled())
				return itemStack;
			if (!event.getItem().equals(craftItem))
			{
				if (event.getItem().getType() == Material.AIR)
					return new ItemStack(Items.bucket);
				ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
				IBehaviorDispenseItem ibehaviordispenseitem = (IBehaviorDispenseItem) BlockDispenser.dispenseBehaviorRegistry.getObject(eventStack.getItem());
				if (ibehaviordispenseitem != IBehaviorDispenseItem.itemDispenseBehaviorProvider && ibehaviordispenseitem != this)
				{
					ibehaviordispenseitem.dispense(blockSource, eventStack);
					return itemStack;
				}
			}
			itembucket = (ItemBucket) CraftItemStack.asNMSCopy(event.getItem()).getItem();
		}
		if (itembucket.tryPlaceContainedLiquid(blockSource.getWorld(), i + enumfacing.getFrontOffsetX(), j + enumfacing.getFrontOffsetY(), k + enumfacing.getFrontOffsetZ()))
		{
			Item item = Items.bucket;
			if (--itemStack.stackSize == 0)
			{
				itemStack.func_150996_a(Items.bucket);
				itemStack.stackSize = 1;
			}
			else if (((TileEntityDispenser) blockSource.getBlockTileEntity()).func_146019_a(new ItemStack(item)) < 0)
			{
				this.field_150841_b.dispense(blockSource, new ItemStack(item));
			}
			return itemStack;
		}
		else
		{
			return this.field_150841_b.dispense(blockSource, itemStack);
		}
	}
}
