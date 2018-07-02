package org.ultramine.mods.bukkit.mixin.init;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;
import java.util.Random;

@Mixin(targets = "net/minecraft/init/Bootstrap$8")
public class MixinBootstrapFireCharge
{
	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
	{
		EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
		IPosition iposition = BlockDispenser.func_149939_a(blockSource);
		double d0 = iposition.getX() + (double) ((float) enumfacing.getFrontOffsetX() * 0.3F);
		double d1 = iposition.getY() + (double) ((float) enumfacing.getFrontOffsetX() * 0.3F);
		double d2 = iposition.getZ() + (double) ((float) enumfacing.getFrontOffsetZ() * 0.3F);
		World world = blockSource.getWorld();
		Random random = world.rand;
		double d3 = random.nextGaussian() * 0.05D + (double) enumfacing.getFrontOffsetX();
		double d4 = random.nextGaussian() * 0.05D + (double) enumfacing.getFrontOffsetY();
		double d5 = random.nextGaussian() * 0.05D + (double) enumfacing.getFrontOffsetZ();
		ItemStack itemstack1 = itemStack.splitStack(1);
		org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(d3, d4, d5));
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
		{
			itemStack.stackSize++;
			return itemStack;
		}
		if (!event.getItem().equals(craftItem))
		{
			if (event.getItem().getType() == Material.AIR)
				return itemStack;
			itemStack.stackSize++;
			ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
			IBehaviorDispenseItem ibehaviordispenseitem = (IBehaviorDispenseItem) BlockDispenser.dispenseBehaviorRegistry.getObject(eventStack.getItem());
			if (ibehaviordispenseitem != IBehaviorDispenseItem.itemDispenseBehaviorProvider && ibehaviordispenseitem != this)
			{
				ibehaviordispenseitem.dispense(blockSource, eventStack);
				return itemStack;
			}
		}
		EntitySmallFireball entitysmallfireball = new EntitySmallFireball(world, d0, d1, d2, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
		((IMixinEntity) entitysmallfireball).setProjectileSource(new  org.bukkit.craftbukkit.projectiles.CraftBlockProjectileSource((TileEntityDispenser) blockSource.getBlockTileEntity()));
		world.spawnEntityInWorld(entitysmallfireball);
		return itemStack;
	}
}
