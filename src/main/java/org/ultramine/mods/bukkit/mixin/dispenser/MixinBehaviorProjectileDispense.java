package org.ultramine.mods.bukkit.mixin.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(BehaviorProjectileDispense.class)
public abstract class MixinBehaviorProjectileDispense
{
	@Shadow protected abstract float func_82498_a();

	@Shadow protected abstract float func_82500_b();

	@Shadow protected abstract IProjectile getProjectileEntity(World var1, IPosition var2);

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
	{
		World world = blockSource.getWorld();
		IPosition iposition = BlockDispenser.func_149939_a(blockSource);
		EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
		IProjectile iprojectile = this.getProjectileEntity(world, iposition);
		ItemStack itemstack1 = itemStack.splitStack(1);
		org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(enumfacing.getFrontOffsetX(), enumfacing.getFrontOffsetY() + 0.1F, enumfacing.getFrontOffsetZ()));
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
		iprojectile.setThrowableHeading(enumfacing.getFrontOffsetX(), enumfacing.getFrontOffsetY() + 0.1F, enumfacing.getFrontOffsetZ(), this.func_82500_b(), this.func_82498_a());
		((IMixinEntity) iprojectile).setProjectileSource(new org.bukkit.craftbukkit.projectiles.CraftBlockProjectileSource((TileEntityDispenser) blockSource.getBlockTileEntity()));
		world.spawnEntityInWorld((Entity) iprojectile);
		return itemStack;
	}
}
