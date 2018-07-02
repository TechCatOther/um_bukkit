package org.ultramine.mods.bukkit.mixin.init;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(targets = "net/minecraft/init/Bootstrap$12")
public class MixinBootstrapFlintAndSteel
{
	@Shadow private boolean field_150839_b;

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
	{
		EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
		World world = blockSource.getWorld();
		int i = blockSource.getXInt() + enumfacing.getFrontOffsetX();
		int j = blockSource.getYInt() + enumfacing.getFrontOffsetY();
		int k = blockSource.getZInt() + enumfacing.getFrontOffsetZ();
		org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemStack);
		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(0, 0, 0));
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
			return itemStack;
		if (!event.getItem().equals(craftItem))
		{
			if (event.getItem().getType() == Material.AIR)
			{
				if (itemStack.attemptDamageItem(1, world.rand))
					itemStack.stackSize = 0;
				return itemStack;
			}
			ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
			IBehaviorDispenseItem ibehaviordispenseitem = (IBehaviorDispenseItem) BlockDispenser.dispenseBehaviorRegistry.getObject(eventStack.getItem());
			if (ibehaviordispenseitem != IBehaviorDispenseItem.itemDispenseBehaviorProvider && ibehaviordispenseitem != this)
			{
				ibehaviordispenseitem.dispense(blockSource, eventStack);
				return itemStack;
			}
		}
		if (world.isAirBlock(i, j, k))
		{
			if (!org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(world, i, j, k, blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt()).isCancelled())
			{
				world.setBlock(i, j, k, Blocks.fire);
				if (itemStack.attemptDamageItem(1, world.rand))
					itemStack.stackSize = 0;
			}
		}
		else if (world.getBlock(i, j, k) == Blocks.tnt)
		{
			Blocks.tnt.onBlockDestroyedByPlayer(world, i, j, k, 1);
			world.setBlockToAir(i, j, k);
		}
		else
		{
			this.field_150839_b = false;
		}
		return itemStack;
	}
}
