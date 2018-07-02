package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.Random;

@Mixin(BlockVine.class)
public abstract class MixinBlockVine
{
	@Shadow protected abstract boolean func_150093_a(Block p_150093_1_);

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		if (!p_149674_1_.isRemote && p_149674_1_.rand.nextInt(4) == 0)
		{
			BlockVine thisBlockVine = (BlockVine) (Object) this;
			byte b0 = 4;
			int l = 5;
			boolean flag = false;
			int i1;
			int j1;
			int k1;
			label134:
			for (i1 = p_149674_2_ - b0; i1 <= p_149674_2_ + b0; ++i1)
				for (j1 = p_149674_4_ - b0; j1 <= p_149674_4_ + b0; ++j1)
					for (k1 = p_149674_3_ - 1; k1 <= p_149674_3_ + 1; ++k1)
						if (p_149674_1_.getBlock(i1, k1, j1) == thisBlockVine)
						{
							--l;
							if (l <= 0)
							{
								flag = true;
								break label134;
							}
						}
			i1 = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_);
			j1 = p_149674_1_.rand.nextInt(6);
			k1 = Direction.facingToDirection[j1];
			int l1;
			if (j1 == 1 && p_149674_3_ < 255 && p_149674_1_.isAirBlock(p_149674_2_, p_149674_3_ + 1, p_149674_4_))
			{
				if (flag)
					return;
				int j2 = p_149674_1_.rand.nextInt(16) & i1;
				if (j2 > 0)
				{
					for (l1 = 0; l1 <= 3; ++l1)
						if (!this.func_150093_a(p_149674_1_.getBlock(p_149674_2_ + Direction.offsetX[l1], p_149674_3_ + 1, p_149674_4_ + Direction.offsetZ[l1])))
							j2 &= ~(1 << l1);
					if (j2 > 0)
					{
						org.bukkit.block.Block source = ((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_);
						org.bukkit.block.Block block = ((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_, p_149674_3_ + 1, p_149674_4_);
						CraftEventFactory.handleBlockSpreadEvent(block, source, thisBlockVine, l1);
					}
				}
			}
			else
			{
				Block block;
				int i2;
				if (j1 >= 2 && j1 <= 5 && (i1 & 1 << k1) == 0)
				{
					if (flag)
						return;
					block = p_149674_1_.getBlock(p_149674_2_ + Direction.offsetX[k1], p_149674_3_, p_149674_4_ + Direction.offsetZ[k1]);
					if (block.getMaterial() == Material.air)
					{
						l1 = k1 + 1 & 3;
						i2 = k1 + 3 & 3;
						org.bukkit.block.Block source = ((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_);
						org.bukkit.block.Block bukkitBlock = ((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_ + Direction.offsetX[k1], p_149674_3_, p_149674_4_ + Direction.offsetZ[k1]);
						if ((i1 & 1 << l1) != 0 && this.func_150093_a(p_149674_1_.getBlock(p_149674_2_ + Direction.offsetX[k1] + Direction.offsetX[l1], p_149674_3_, p_149674_4_ + Direction.offsetZ[k1] + Direction.offsetZ[l1])))
						{
							CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, thisBlockVine, 1 << l1);
						}
						else if ((i1 & 1 << i2) != 0 && this.func_150093_a(p_149674_1_.getBlock(p_149674_2_ + Direction.offsetX[k1] + Direction.offsetX[i2], p_149674_3_, p_149674_4_ + Direction.offsetZ[k1] + Direction.offsetZ[i2])))
						{
							CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, thisBlockVine, 1 << i2);
						}
						else if ((i1 & 1 << l1) != 0 && p_149674_1_.isAirBlock(p_149674_2_ + Direction.offsetX[k1] + Direction.offsetX[l1], p_149674_3_, p_149674_4_ + Direction.offsetZ[k1] + Direction.offsetZ[l1]) && this.func_150093_a(p_149674_1_.getBlock(p_149674_2_ + Direction.offsetX[l1], p_149674_3_, p_149674_4_ + Direction.offsetZ[l1])))
						{
							bukkitBlock = ((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_ + Direction.offsetX[k1] + Direction.offsetX[l1], p_149674_3_, p_149674_4_ + Direction.offsetZ[k1] + Direction.offsetZ[l1]);
							CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, thisBlockVine, 1 << (k1 + 2 & 3));
						}
						else if ((i1 & 1 << i2) != 0 && p_149674_1_.isAirBlock(p_149674_2_ + Direction.offsetX[k1] + Direction.offsetX[i2], p_149674_3_, p_149674_4_ + Direction.offsetZ[k1] + Direction.offsetZ[i2]) && this.func_150093_a(p_149674_1_.getBlock(p_149674_2_ + Direction.offsetX[i2], p_149674_3_, p_149674_4_ + Direction.offsetZ[i2])))
						{
							bukkitBlock = ((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_ + Direction.offsetX[k1] + Direction.offsetX[i2], p_149674_3_, p_149674_4_ + Direction.offsetZ[k1] + Direction.offsetZ[i2]);
							CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, thisBlockVine, 1 << (k1 + 2 & 3));
						}
						else if (this.func_150093_a(p_149674_1_.getBlock(p_149674_2_ + Direction.offsetX[k1], p_149674_3_ + 1, p_149674_4_ + Direction.offsetZ[k1])))
						{
							CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, thisBlockVine, 0);
						}
					}
					else if (block.getMaterial().isOpaque() && block.renderAsNormalBlock())
					{
						p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, i1 | 1 << k1, 2);
					}
				}
				else if (p_149674_3_ > 1)
				{
					block = p_149674_1_.getBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_);
					if (block.getMaterial() == Material.air)
					{
						l1 = p_149674_1_.rand.nextInt(16) & i1;
						if (l1 > 0)
						{
							org.bukkit.block.Block source = ((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_);
							org.bukkit.block.Block bukkitBlock = ((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_, p_149674_3_ - 1, p_149674_4_);
							CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, thisBlockVine, l1);
						}
					}
					else if (block == thisBlockVine)
					{
						l1 = p_149674_1_.rand.nextInt(16) & i1;
						i2 = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_ - 1, p_149674_4_);
						if (i2 != (i2 | l1))
							p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_ - 1, p_149674_4_, i2 | l1, 2);
					}
				}
			}
		}
	}
}
