package org.ultramine.mods.bukkit.mixin.init;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(targets = "net/minecraft/init/Bootstrap$14")
public class MixinBootstrapTNT
{
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
		ItemStack itemstack1 = itemStack.splitStack(1);
		org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(i + 0.5, j + 0.5, k + 0.5));
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
		EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(world, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), (EntityLivingBase) null);
		world.spawnEntityInWorld(entitytntprimed);
		return itemStack;
	}
}
