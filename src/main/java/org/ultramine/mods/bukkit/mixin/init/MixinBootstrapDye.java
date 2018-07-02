package org.ultramine.mods.bukkit.mixin.init;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemDye;
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

@Mixin(targets = "net/minecraft/init/Bootstrap$13")
public class MixinBootstrapDye extends BehaviorDefaultDispenseItem
{
	@Shadow
	private boolean field_150838_b;

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
	{
		if (itemStack.getItemDamage() == 15)
		{
			EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
			World world = blockSource.getWorld();
			int i = blockSource.getXInt() + enumfacing.getFrontOffsetX();
			int j = blockSource.getYInt() + enumfacing.getFrontOffsetY();
			int k = blockSource.getZInt() + enumfacing.getFrontOffsetZ();
			org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt());
			CraftItemStack craftItem = CraftItemStack.asNewCraftStack(itemStack.getItem());
			BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(0, 0, 0));
			((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
			if (event.isCancelled())
				return itemStack;
			if (!event.getItem().equals(craftItem))
			{
				if (event.getItem().getType() == Material.AIR)
					return itemStack;
				ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
				IBehaviorDispenseItem ibehaviordispenseitem = (IBehaviorDispenseItem) BlockDispenser.dispenseBehaviorRegistry.getObject(eventStack.getItem());
				if (ibehaviordispenseitem != IBehaviorDispenseItem.itemDispenseBehaviorProvider && ibehaviordispenseitem != this)
				{
					ibehaviordispenseitem.dispense(blockSource, eventStack);
					return itemStack;
				}
			}
			if (ItemDye.func_150919_a(itemStack, world, i, j, k))
				if (!world.isRemote)
					world.playAuxSFX(2005, i, j, k, 0);
				else
					this.field_150838_b = false;
			return itemStack;
		}
		else
		{
			return super.dispenseStack(blockSource, itemStack);
		}
	}
}
