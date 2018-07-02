package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockFromToEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(BlockDragonEgg.class)
public class MixinBlockDragonEgg extends Block
{
	protected MixinBlockDragonEgg(Material p_i45394_1_)
	{
		super(p_i45394_1_);
	}

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	private void func_150019_m(World world, int x, int y, int z)
	{
		if (world.getBlock(x, y, z) == this)
			for (int l = 0; l < 1000; ++l)
			{
				int i1 = x + world.rand.nextInt(16) - world.rand.nextInt(16);
				int j1 = y + world.rand.nextInt(8) - world.rand.nextInt(8);
				int k1 = z + world.rand.nextInt(16) - world.rand.nextInt(16);
				if (world.getBlock(i1, j1, k1).getMaterial() == Material.air)
				{
					org.bukkit.block.Block from = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z);
					org.bukkit.block.Block to = ((IMixinWorld) world).getWorld().getBlockAt(i1, j1, k1);
					BlockFromToEvent event = new BlockFromToEvent(from, to);
					org.bukkit.Bukkit.getPluginManager().callEvent(event);
					if (event.isCancelled())
						return;
					i1 = event.getToBlock().getX();
					j1 = event.getToBlock().getY();
					k1 = event.getToBlock().getZ();
					if (!world.isRemote)
					{
						world.setBlock(i1, j1, k1, this, world.getBlockMetadata(x, y, z), 2);
						world.setBlockToAir(x, y, z);
					}
					else
					{
						short short1 = 128;
						for (int l1 = 0; l1 < short1; ++l1)
						{
							double d0 = world.rand.nextDouble();
							float f = (world.rand.nextFloat() - 0.5F) * 0.2F;
							float f1 = (world.rand.nextFloat() - 0.5F) * 0.2F;
							float f2 = (world.rand.nextFloat() - 0.5F) * 0.2F;
							double d1 = (double) i1 + (double) (x - i1) * d0 + (world.rand.nextDouble() - 0.5D) * 1.0D + 0.5D;
							double d2 = (double) j1 + (double) (y - j1) * d0 + world.rand.nextDouble() * 1.0D - 0.5D;
							double d3 = (double) k1 + (double) (z - k1) * d0 + (world.rand.nextDouble() - 0.5D) * 1.0D + 0.5D;
							world.spawnParticle("portal", d1, d2, d3, (double) f, (double) f1, (double) f2);
						}
					}
					return;
				}
			}
		}
}
