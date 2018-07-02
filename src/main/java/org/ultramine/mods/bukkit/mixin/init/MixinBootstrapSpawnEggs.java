package org.ultramine.mods.bukkit.mixin.init;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(targets = "net/minecraft/init/Bootstrap$6")
public class MixinBootstrapSpawnEggs
{
	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
	{
		EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
		double d0 = blockSource.getX() + (double) enumfacing.getFrontOffsetX();
		double d1 = (double) ((float) blockSource.getYInt() + 0.2F);
		double d2 = blockSource.getZ() + (double) enumfacing.getFrontOffsetZ();
		World world = blockSource.getWorld();
		ItemStack itemstack1 = itemStack.splitStack(1);
		org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(d0, d1, d2));
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
		itemstack1 = CraftItemStack.asNMSCopy(event.getItem());
		Entity entity = ItemMonsterPlacer.spawnCreature(blockSource.getWorld(), itemStack.getItemDamage(), event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
		if (entity instanceof EntityLivingBase && itemStack.hasDisplayName())
			((EntityLiving) entity).setCustomNameTag(itemStack.getDisplayName());
		return itemStack;
	}
}
