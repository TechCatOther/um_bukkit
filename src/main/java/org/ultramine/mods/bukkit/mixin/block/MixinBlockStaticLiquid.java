package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(BlockStaticLiquid.class)
public abstract class MixinBlockStaticLiquid
{
	@Shadow protected abstract boolean isFlammable(World p_149817_1_, int p_149817_2_, int p_149817_3_, int p_149817_4_);

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public void updateTick(World world, int x, int y, int z, Random random)
	{
		if (((BlockStaticLiquid) (Object) this).getMaterial() == Material.lava)
		{
			int l = random.nextInt(3);
			int i1 = 0;
			int igniterX = x;
			int igniterY = y;
			int igniterZ = z;
			while (true)
			{
				if (i1 >= l)
				{
					if (l == 0)
					{
						i1 = x;
						int k1 = z;

						for (int j1 = 0; j1 < 3; ++j1)
						{
							x = i1 + random.nextInt(3) - 1;
							z = k1 + random.nextInt(3) - 1;
							if (world.isAirBlock(x, y + 1, z) && this.isFlammable(world, x, y, z))
							{
								if (world.getBlock(x, y + 1, z) != Blocks.fire)
									if (CraftEventFactory.callBlockIgniteEvent(world, x, y + 1, z, igniterX, igniterY, igniterZ).isCancelled())
										continue;
								world.setBlock(x, y + 1, z, Blocks.fire);
							}
						}
					}
					break;
				}
				x += random.nextInt(3) - 1;
				++y;
				z += random.nextInt(3) - 1;
				Block block = world.getBlock(x, y, z);
				if (block.getMaterial() == Material.air)
				{
					if (this.isFlammable(world, x - 1, y, z) || this.isFlammable(world, x + 1, y, z) || this.isFlammable(world, x, y, z - 1) || this.isFlammable(world, x, y, z + 1) || this.isFlammable(world, x, y - 1, z) || this.isFlammable(world, x, y + 1, z))
					{
						if (world.getBlock(x, y, z) != Blocks.fire)
							if (CraftEventFactory.callBlockIgniteEvent(world, x, y, z, igniterX, igniterY, igniterZ).isCancelled())
								continue;
						world.setBlock(x, y, z, Blocks.fire);
						return;
					}
				}
				else if (block.getMaterial().blocksMovement())
				{
					return;
				}
				++i1;
			}
		}
	}
}
