package org.ultramine.mods.bukkit.mixin.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(BehaviorDefaultDispenseItem.class)
public class MixinBehaviorDefaultDispenseItem
{
	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	protected ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
	{
		EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
		if (!doDispense(blockSource.getWorld(), itemStack.splitStack(1), 6, enumfacing, blockSource))
			itemStack.stackSize++;
		return itemStack;
	}

	private boolean doDispense(World world, ItemStack itemstack, int i, EnumFacing enumfacing, IBlockSource iblocksource)
	{
		IPosition iposition = BlockDispenser.func_149939_a(iblocksource);
		double d0 = iposition.getX();
		double d1 = iposition.getY();
		double d2 = iposition.getZ();
		EntityItem entityitem = new EntityItem(world, d0, d1 - 0.3D, d2, itemstack);
		double d3 = world.rand.nextDouble() * 0.1D + 0.2D;
		entityitem.motionX = (double) enumfacing.getFrontOffsetX() * d3;
		entityitem.motionY = 0.20000000298023224D;
		entityitem.motionZ = (double) enumfacing.getFrontOffsetZ() * d3;
		entityitem.motionX = world.rand.nextGaussian() * 0.007499999832361937D * (double) i;
		entityitem.motionY = world.rand.nextGaussian() * 0.007499999832361937D * (double) i;
		entityitem.motionZ = world.rand.nextGaussian() * 0.007499999832361937D * (double) i;
		Block block = ((IMixinWorld) world).getWorld().getBlockAt(iblocksource.getXInt(), iblocksource.getYInt(), iblocksource.getZInt());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);
		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(entityitem.motionX, entityitem.motionY, entityitem.motionZ));
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
		if (event.getItem().getType() != Material.AIR)
		{
			entityitem.setEntityItemStack(CraftItemStack.asNMSCopy(event.getItem()));
			entityitem.motionX = event.getVelocity().getX();
			entityitem.motionY = event.getVelocity().getY();
			entityitem.motionZ = event.getVelocity().getZ();
			if (!event.getItem().equals(craftItem))
			{
				ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
				IBehaviorDispenseItem ibehaviordispenseitem = (IBehaviorDispenseItem) BlockDispenser.dispenseBehaviorRegistry.getObject(eventStack.getItem());
				if (ibehaviordispenseitem != IBehaviorDispenseItem.itemDispenseBehaviorProvider && ibehaviordispenseitem.getClass() != BehaviorDefaultDispenseItem.class)
					ibehaviordispenseitem.dispense(iblocksource, eventStack);
				else
					world.spawnEntityInWorld(entityitem);
				return false;
			}
			world.spawnEntityInWorld(entityitem);
		}
		return true;
	}
}
