package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.Random;

@Mixin(net.minecraft.block.BlockFire.class)
public abstract class MixinBlockFire extends Block
{
	protected MixinBlockFire(Material p_i45394_1_)
	{
		super(p_i45394_1_);
	}

	@Inject(method = "tryCatchFire(Lnet/minecraft/world/World;IIIILjava/util/Random;ILnet/minecraftforge/common/util/ForgeDirection;)V", cancellable = true,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;", ordinal = 1))
	private void onTryCatchFire(World w, int x, int y, int z, int r1, Random rng, int r2, ForgeDirection face, CallbackInfo ci)
	{
		org.bukkit.block.Block theBlock = ((IMixinWorld) w).getWorld().getBlockAt(x, y, z);
		BlockBurnEvent event = new BlockBurnEvent(theBlock);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (event.isCancelled())
		{
			ci.cancel();
			return;
		}
	}

	@Redirect(method = "onNeighborBlockChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir(III)Z"))
	public boolean setBlockToAirNeighborBlockChangeRedirect(World world, int x, int y, int z)
	{
		if (!CraftEventFactory.callBlockFadeEvent(((IMixinWorld) world).getWorld().getBlockAt(x, y, z), Blocks.air).isCancelled())
			world.setBlockToAir(x, y, z);
		return false;
	}

	@Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir(III)Z"))
	public boolean setBlockToAirBlockAddedRedirect(World world, int x, int y, int z)
	{
		if (!CraftEventFactory.callBlockFadeEvent(((IMixinWorld) world).getWorld().getBlockAt(x, y, z), Blocks.air).isCancelled())
			world.setBlockToAir(x, y, z);
		return false;
	}

	@Shadow
	protected abstract void tryCatchFire(World p_149841_1_, int p_149841_2_, int p_149841_3_, int p_149841_4_, int p_149841_5_, Random p_149841_6_, int p_149841_7_, ForgeDirection face);

	@Shadow
	protected abstract boolean canNeighborBurn(World p_149847_1_, int p_149847_2_, int p_149847_3_, int p_149847_4_);

	@Shadow
	public abstract boolean canPlaceBlockAt(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_);

	@Shadow
	public abstract int tickRate(World p_149738_1_);

	@Shadow
	public abstract boolean canCatchFire(IBlockAccess world, int x, int y, int z, ForgeDirection face);

	@Shadow
	protected abstract int getChanceOfNeighborsEncouragingFire(World p_149845_1_, int p_149845_2_, int p_149845_3_, int p_149845_4_);

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		if (p_149674_1_.getGameRules().getGameRuleBooleanValue("doFireTick"))
		{
			boolean flag = p_149674_1_.getBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_).isFireSource(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_, ForgeDirection.UP);
			if (!this.canPlaceBlockAt(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_))
			{
				if (!CraftEventFactory.callBlockFadeEvent(((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), Blocks.air).isCancelled())
					p_149674_1_.setBlockToAir(p_149674_2_, p_149674_3_, p_149674_4_);
			}

			if (!flag && p_149674_1_.isRaining() && (p_149674_1_.canLightningStrikeAt(p_149674_2_, p_149674_3_, p_149674_4_) || p_149674_1_.canLightningStrikeAt(p_149674_2_ - 1, p_149674_3_, p_149674_4_) || p_149674_1_.canLightningStrikeAt(p_149674_2_ + 1, p_149674_3_, p_149674_4_) || p_149674_1_.canLightningStrikeAt(p_149674_2_, p_149674_3_, p_149674_4_ - 1) || p_149674_1_.canLightningStrikeAt(p_149674_2_, p_149674_3_, p_149674_4_ + 1)))
			{
				if (!CraftEventFactory.callBlockFadeEvent(((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), Blocks.air).isCancelled())
					p_149674_1_.setBlockToAir(p_149674_2_, p_149674_3_, p_149674_4_);
			}
			else
			{
				int l = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_);
				if (l < 15)
					p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, l + p_149674_5_.nextInt(3) / 2, 4);
				p_149674_1_.scheduleBlockUpdate(p_149674_2_, p_149674_3_, p_149674_4_, this, this.tickRate(p_149674_1_) + p_149674_5_.nextInt(10));
				if (!flag && !this.canNeighborBurn(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_))
				{
					if (!World.doesBlockHaveSolidTopSurface(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_) || l > 3)
						if (!CraftEventFactory.callBlockFadeEvent(((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), Blocks.air).isCancelled())
							p_149674_1_.setBlockToAir(p_149674_2_, p_149674_3_, p_149674_4_);
				}
				else if (!flag && !this.canCatchFire(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_, ForgeDirection.UP) && l == 15 && p_149674_5_.nextInt(4) == 0)
				{
					if (!CraftEventFactory.callBlockFadeEvent(((IMixinWorld) p_149674_1_).getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), Blocks.air).isCancelled())
						p_149674_1_.setBlockToAir(p_149674_2_, p_149674_3_, p_149674_4_);
				}
				else
				{
					boolean flag1 = p_149674_1_.isBlockHighHumidity(p_149674_2_, p_149674_3_, p_149674_4_);
					byte b0 = 0;
					if (flag1)
						b0 = -50;
					this.tryCatchFire(p_149674_1_, p_149674_2_ + 1, p_149674_3_, p_149674_4_, 300 + b0, p_149674_5_, l, ForgeDirection.WEST);
					this.tryCatchFire(p_149674_1_, p_149674_2_ - 1, p_149674_3_, p_149674_4_, 300 + b0, p_149674_5_, l, ForgeDirection.EAST);
					this.tryCatchFire(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_, 250 + b0, p_149674_5_, l, ForgeDirection.UP);
					this.tryCatchFire(p_149674_1_, p_149674_2_, p_149674_3_ + 1, p_149674_4_, 250 + b0, p_149674_5_, l, ForgeDirection.DOWN);
					this.tryCatchFire(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_ - 1, 300 + b0, p_149674_5_, l, ForgeDirection.SOUTH);
					this.tryCatchFire(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_ + 1, 300 + b0, p_149674_5_, l, ForgeDirection.NORTH);
					for (int i1 = p_149674_2_ - 1; i1 <= p_149674_2_ + 1; ++i1)
						for (int j1 = p_149674_4_ - 1; j1 <= p_149674_4_ + 1; ++j1)
							for (int k1 = p_149674_3_ - 1; k1 <= p_149674_3_ + 4; ++k1)
								if (i1 != p_149674_2_ || k1 != p_149674_3_ || j1 != p_149674_4_)
								{
									int l1 = 100;
									if (k1 > p_149674_3_ + 1)
									{
										l1 += (k1 - (p_149674_3_ + 1)) * 100;
									}

									int i2 = this.getChanceOfNeighborsEncouragingFire(p_149674_1_, i1, k1, j1);
									if (i2 > 0)
									{
										int j2 = (i2 + 40 + p_149674_1_.difficultySetting.getDifficultyId() * 7) / (l + 30);
										if (flag1)
										{
											j2 /= 2;
										}

										if (j2 > 0 && p_149674_5_.nextInt(l1) <= j2 && (!p_149674_1_.isRaining() || !p_149674_1_.canLightningStrikeAt(i1, k1, j1)) && !p_149674_1_.canLightningStrikeAt(i1 - 1, k1, p_149674_4_) && !p_149674_1_.canLightningStrikeAt(i1 + 1, k1, j1) && !p_149674_1_.canLightningStrikeAt(i1, k1, j1 - 1) && !p_149674_1_.canLightningStrikeAt(i1, k1, j1 + 1))
										{
											int k2 = l + p_149674_5_.nextInt(5) / 4;
											if (k2 > 15)
											{
												k2 = 15;
											}

											if (p_149674_1_.getBlock(i1, k1, j1) != Blocks.fire)
											{
												if (CraftEventFactory.callBlockIgniteEvent(p_149674_1_, i1, k1, j1, p_149674_2_, p_149674_3_, p_149674_4_).isCancelled())
													continue;
												org.bukkit.Server server = ((IMixinWorld) p_149674_1_).getServer();
												org.bukkit.World bworld = ((IMixinWorld) p_149674_1_).getWorld();
												org.bukkit.block.BlockState blockState = bworld.getBlockAt(i1, k1, j1).getState();
												blockState.setTypeId(Block.getIdFromBlock(this));
												blockState.setData(new org.bukkit.material.MaterialData(Block.getIdFromBlock(this), (byte) k2));
												BlockSpreadEvent spreadEvent = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), blockState);
												server.getPluginManager().callEvent(spreadEvent);
												if (!spreadEvent.isCancelled())
												{
													p_149674_1_.setBlock(i1, k1, j1, ((org.bukkit.craftbukkit.block.CraftBlock) blockState.getBlock()).getNMSBlock(), blockState.getRawData(), 3); // KCauldron - DragonAPI uses this call
													blockState.update(true);
												}
											}
										}
									}
								}
				}
			}
		}
	}
}
