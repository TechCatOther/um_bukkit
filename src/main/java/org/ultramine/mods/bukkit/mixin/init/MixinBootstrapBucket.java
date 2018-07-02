package org.ultramine.mods.bukkit.mixin.init;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(targets = "net/minecraft/init/Bootstrap$11")
public abstract class MixinBootstrapBucket extends BehaviorDefaultDispenseItem
{
	@Final
	@Shadow private BehaviorDefaultDispenseItem field_150840_b;

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
		Material material = world.getBlock(i, j, k).getMaterial();
		int l = world.getBlockMetadata(i, j, k);
		Item item;
		if (Material.water.equals(material) && l == 0)
		{
			item = Items.water_bucket;
		}
		else
		{
			if (!Material.lava.equals(material) || l != 0)
				return super.dispenseStack(blockSource, itemStack);
			item = Items.lava_bucket;
		}
		org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemStack);
		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(i, j, k));
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
			return itemStack;
		if (!event.getItem().equals(craftItem))
		{
			if (event.getItem().getType() == org.bukkit.Material.AIR)
				return itemStack;
			ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
			IBehaviorDispenseItem ibehaviordispenseitem = (IBehaviorDispenseItem) BlockDispenser.dispenseBehaviorRegistry.getObject(eventStack.getItem());
			if (ibehaviordispenseitem != IBehaviorDispenseItem.itemDispenseBehaviorProvider && ibehaviordispenseitem != this)
			{
				ibehaviordispenseitem.dispense(blockSource, eventStack);
				return itemStack;
			}
		}
		world.setBlockToAir(i, j, k);
		if (--itemStack.stackSize == 0)
		{
			itemStack.func_150996_a(item);
			itemStack.stackSize = 1;
		}
		else if (((TileEntityDispenser) blockSource.getBlockTileEntity()).func_146019_a(new ItemStack(item)) < 0)
		{
			this.field_150840_b.dispense(blockSource, new ItemStack(item));
		}
		return itemStack;
	}
}
