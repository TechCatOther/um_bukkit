package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.block.BlockFromToEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.Random;

@Mixin(BlockDynamicLiquid.class)
public abstract class MixinBlockDynamicLiquid extends BlockLiquid
{
	protected MixinBlockDynamicLiquid(Material p_i45394_1_)
	{
		super(p_i45394_1_);
	}

	@Shadow
	int field_149815_a;

	@Shadow
	protected abstract void func_149813_h(World p_149813_1_, int p_149813_2_, int p_149813_3_, int p_149813_4_, int p_149813_5_);

	@Shadow
	protected abstract boolean func_149809_q(World p_149809_1_, int p_149809_2_, int p_149809_3_, int p_149809_4_);

	@Shadow
	protected abstract int func_149810_a(World p_149810_1_, int p_149810_2_, int p_149810_3_, int p_149810_4_, int p_149810_5_);

	@Shadow
	protected abstract void func_149811_n(World p_149811_1_, int p_149811_2_, int p_149811_3_, int p_149811_4_);

	@Shadow
	protected abstract boolean func_149807_p(World p_149807_1_, int p_149807_2_, int p_149807_3_, int p_149807_4_);

	@Shadow
	protected abstract boolean[] func_149808_o(World p_149808_1_, int p_149808_2_, int p_149808_3_, int p_149808_4_);

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public void updateTick(World world, int x, int y, int z, Random random)
	{
		org.bukkit.block.Block source = ((IMixinWorld) world).getWorld() == null ? null : ((IMixinWorld) world).getWorld().getBlockAt(x, y, z);
		int l = this.func_149804_e(world, x, y, z);
		byte b0 = 1;
		if (this.blockMaterial == Material.lava && !world.provider.isHellWorld)
			b0 = 2;
		boolean flag = true;
		int i1 = this.tickRate(world);
		int j1;
		if (l > 0)
		{
			byte b1 = -100;
			this.field_149815_a = 0;
			int l1 = this.func_149810_a(world, x - 1, y, z, b1);
			l1 = this.func_149810_a(world, x + 1, y, z, l1);
			l1 = this.func_149810_a(world, x, y, z - 1, l1);
			l1 = this.func_149810_a(world, x, y, z + 1, l1);
			j1 = l1 + b0;
			if (j1 >= 8 || l1 < 0)
				j1 = -1;
			if (this.func_149804_e(world, x, y + 1, z) >= 0)
			{
				int k1 = this.func_149804_e(world, x, y + 1, z);
				if (k1 >= 8)
					j1 = k1;
				else
					j1 = k1 + 8;
			}
			if (this.field_149815_a >= 2 && this.blockMaterial == Material.water)
			{
				if (world.getBlock(x, y - 1, z).getMaterial().isSolid())
					j1 = 0;
				else if (world.getBlock(x, y - 1, z).getMaterial() == this.blockMaterial && world.getBlockMetadata(x, y - 1, z) == 0)
					j1 = 0;
			}
			if (this.blockMaterial == Material.lava && l < 8 && j1 < 8 && j1 > l && random.nextInt(4) != 0)
				i1 *= 4;
			if (j1 == l)
			{
				if (flag)
					this.func_149811_n(world, x, y, z);
			}
			else
			{
				l = j1;
				if (j1 < 0)
				{
					world.setBlockToAir(x, y, z);
				}
				else
				{
					world.setBlockMetadataWithNotify(x, y, z, j1, 2);
					world.scheduleBlockUpdate(x, y, z, this, i1);
					world.notifyBlocksOfNeighborChange(x, y, z, this);
				}
			}
		}
		else
		{
			this.func_149811_n(world, x, y, z);
		}

		if (this.func_149809_q(world, x, y - 1, z))
		{
			if (((IMixinWorld) world).getType(x, y, z).getMaterial() != this.blockMaterial)
				return;
			BlockFromToEvent event = new BlockFromToEvent(source, BlockFace.DOWN);
			CraftServer server = ((IMixinWorld) world).getServer();
			if (server != null && source != null)
				server.getPluginManager().callEvent(event);
			if (!event.isCancelled())
			{
				if (this.blockMaterial == Material.lava && world.getBlock(x, y - 1, z).getMaterial() == Material.water)
				{
					world.setBlock(x, y - 1, z, Blocks.stone);
					this.func_149799_m(world, x, y - 1, z);
					return;
				}
				if (l >= 8)
					this.func_149813_h(world, x, y - 1, z, l);
				else
					this.func_149813_h(world, x, y - 1, z, l + 8);
			}
		}
		else if (l >= 0 && (l == 0 || this.func_149807_p(world, x, y - 1, z)))
		{
			boolean[] aboolean = this.func_149808_o(world, x, y, z);
			j1 = l + b0;
			if (l >= 8)
				j1 = 1;
			if (j1 >= 8)
				return;
			// CraftBukkit start - All four cardinal directions. Do not change the order!
			BlockFace[] faces = new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH};
			int index = 0;
			CraftServer server = ((IMixinWorld) world).getServer();
			for (BlockFace currentFace : faces)
			{
				if (aboolean[index])
				{
					BlockFromToEvent event = new BlockFromToEvent(source, currentFace);
					if (server != null && source != null)
						server.getPluginManager().callEvent(event);
					if (!event.isCancelled())
						this.func_149813_h(world, x + currentFace.getModX(), y, z + currentFace.getModZ(), j1);
				}
				index++;
			}
		}
	}
}
