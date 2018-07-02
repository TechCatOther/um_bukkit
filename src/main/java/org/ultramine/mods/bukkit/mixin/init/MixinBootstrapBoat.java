package org.ultramine.mods.bukkit.mixin.init;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(targets = "net/minecraft/init/Bootstrap$9")
public class MixinBootstrapBoat
{
	@Final
	@Shadow private BehaviorDefaultDispenseItem field_150842_b;

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
	{
		EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
		World world = blockSource.getWorld();
		double d0 = blockSource.getX() + (double) ((float) enumfacing.getFrontOffsetX() * 1.125F);
		double d1 = blockSource.getY() + (double) ((float) enumfacing.getFrontOffsetY() * 1.125F);
		double d2 = blockSource.getZ() + (double) ((float) enumfacing.getFrontOffsetZ() * 1.125F);
		int i = blockSource.getXInt() + enumfacing.getFrontOffsetX();
		int j = blockSource.getYInt() + enumfacing.getFrontOffsetY();
		int k = blockSource.getZInt() + enumfacing.getFrontOffsetZ();
		Material material = world.getBlock(i, j, k).getMaterial();
		double d3;
		if (Material.water.equals(material))
		{
			d3 = 1.0D;
		}
		else
		{
			if (!Material.air.equals(material) || !Material.water.equals(world.getBlock(i, j - 1, k).getMaterial()))
				return this.field_150842_b.dispense(blockSource, itemStack);
			d3 = 0.0D;
		}
		ItemStack itemstack1 = itemStack.splitStack(1);
		org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(d0, d1 + d3, d2));
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
		{
			itemStack.stackSize++;
			return itemStack;
		}
		if (!event.getItem().equals(craftItem))
		{
			if (event.getItem().getType() == org.bukkit.Material.AIR)
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
		EntityBoat entityboat = new EntityBoat(world, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
		world.spawnEntityInWorld(entityboat);
		return itemStack;
	}
}
