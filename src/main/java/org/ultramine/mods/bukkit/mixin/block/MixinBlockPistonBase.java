package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import static net.minecraft.block.BlockPistonBase.getPistonOrientation;
import static net.minecraft.block.BlockPistonBase.isExtended;

@Mixin(BlockPistonBase.class)
public abstract class MixinBlockPistonBase
{
	@Shadow protected abstract boolean isIndirectlyPowered(World p_150072_1_, int p_150072_2_, int p_150072_3_, int p_150072_4_, int p_150072_5_);

	@Shadow private static boolean canPushBlock(Block block, World world, int x, int y, int z, boolean p_150080_5_)
	{
		return false;
	}
	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	private void updatePistonState(World world, int x, int y, int z)
	{
		int l = world.getBlockMetadata(x, y, z);
		int i1 = getPistonOrientation(l);
		if (i1 != 7)
		{
			boolean flag = this.isIndirectlyPowered(world, x, y, z, i1);
			if (flag && !isExtended(l))
			{
				int length = canExtend_IntCB(world, x, y, z, i1);
				if (length >= 0)
				{
					org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z);
					BlockPistonExtendEvent event = new BlockPistonExtendEvent(block, length, CraftBlock.notchToBlockFace(i1));
					((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
					if (event.isCancelled())
						return;
					world.addBlockEvent(x, y, z, (BlockPistonBase) (Object) this, 0, i1);
				}
			}
			else if (!flag && isExtended(l))
			{
				org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z);
				BlockPistonRetractEvent event = new BlockPistonRetractEvent(block, CraftBlock.notchToBlockFace(i1));
				((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
				if (event.isCancelled())
					return;
				world.setBlockMetadataWithNotify(x, y, z, i1, 2);
				world.addBlockEvent(x, y, z, (BlockPistonBase) (Object) this, 1, i1);
			}
		}
	}

	private int canExtend_IntCB(World world, int x, int y, int z, int p_150077_4_)
	{
		int i1 = x + Facing.offsetsXForSide[p_150077_4_];
		int j1 = y + Facing.offsetsYForSide[p_150077_4_];
		int k1 = z + Facing.offsetsZForSide[p_150077_4_];
		int l1 = 0;
		while (true)
		{
			if (l1 < 13)
			{
				if (j1 <= 0 || j1 >= world.getHeight())
					return -1;
				Block block = world.getBlock(i1, j1, k1);
				if (!block.isAir(world, i1, j1, k1))
				{
					if (!canPushBlock(block, world, i1, j1, k1, true))
						return -1;
					if (block.getMobilityFlag() != 1)
					{
						if (l1 == 12)
							return -1;
						i1 += Facing.offsetsXForSide[p_150077_4_];
						j1 += Facing.offsetsYForSide[p_150077_4_];
						k1 += Facing.offsetsZForSide[p_150077_4_];
						++l1;
						continue;
					}
				}
			}
			return l1;
		}
	}
}
