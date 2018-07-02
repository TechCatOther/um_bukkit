package org.ultramine.mods.bukkit.mixin.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntitySilverfish.class)
public class MixinEntitySilverfish extends EntityMob
{
	public MixinEntitySilverfish(World world)
	{
		super(world);
	}

	@Shadow private int allySummonCooldown;

	@Overwrite
	protected void updateEntityActionState()
	{
		super.updateEntityActionState();
		if (!this.worldObj.isRemote)
		{
			int i;
			int j;
			int k;
			int i1;
			if (this.allySummonCooldown > 0)
			{
				--this.allySummonCooldown;
				if (this.allySummonCooldown == 0)
				{
					i = MathHelper.floor_double(this.posX);
					j = MathHelper.floor_double(this.posY);
					k = MathHelper.floor_double(this.posZ);
					boolean flag = false;
					for (int l = 0; !flag && l <= 5 && l >= -5; l = l <= 0 ? 1 - l : 0 - l)
						for (i1 = 0; !flag && i1 <= 10 && i1 >= -10; i1 = i1 <= 0 ? 1 - i1 : 0 - i1)
							for (int j1 = 0; !flag && j1 <= 10 && j1 >= -10; j1 = j1 <= 0 ? 1 - j1 : 0 - j1)
								if (this.worldObj.getBlock(i + i1, j + l, k + j1) == Blocks.monster_egg)
								{
									if (CraftEventFactory.callEntityChangeBlockEvent(this, i + i1, j + l, k + j1, Blocks.air, 0).isCancelled())
										continue;
									if (!this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))
									{
										int k1 = this.worldObj.getBlockMetadata(i + i1, j + l, k + j1);
										ImmutablePair immutablepair = BlockSilverfish.func_150197_b(k1);
										this.worldObj.setBlock(i + i1, j + l, k + j1, (Block) immutablepair.getLeft(), (Integer) immutablepair.getRight(), 3);
									}
									else
									{
										this.worldObj.func_147480_a(i + i1, j + l, k + j1, false);
									}
									Blocks.monster_egg.onBlockDestroyedByPlayer(this.worldObj, i + i1, j + l, k + j1, 0);
									if (this.rand.nextBoolean())
									{
										flag = true;
										break;
									}
								}
				}
			}
			if (this.entityToAttack == null && !this.hasPath())
			{
				i = MathHelper.floor_double(this.posX);
				j = MathHelper.floor_double(this.posY + 0.5D);
				k = MathHelper.floor_double(this.posZ);
				int l1 = this.rand.nextInt(6);
				Block block = this.worldObj.getBlock(i + Facing.offsetsXForSide[l1], j + Facing.offsetsYForSide[l1], k + Facing.offsetsZForSide[l1]);
				i1 = this.worldObj.getBlockMetadata(i + Facing.offsetsXForSide[l1], j + Facing.offsetsYForSide[l1], k + Facing.offsetsZForSide[l1]);
				if (BlockSilverfish.func_150196_a(block))
				{
					if (CraftEventFactory.callEntityChangeBlockEvent(this, i + Facing.offsetsXForSide[l1], j + Facing.offsetsYForSide[l1], k + Facing.offsetsZForSide[l1], Blocks.monster_egg, Block.getIdFromBlock(BlockSilverfish.getBlockById(i1))).isCancelled())
						return;
					this.worldObj.setBlock(i + Facing.offsetsXForSide[l1], j + Facing.offsetsYForSide[l1], k + Facing.offsetsZForSide[l1], Blocks.monster_egg, BlockSilverfish.func_150195_a(block, i1), 3);
					this.spawnExplosionParticle();
					this.setDead();
				}
				else
				{
					this.updateWanderPath();
				}
			}
			else if (this.entityToAttack != null && !this.hasPath())
			{
				this.entityToAttack = null;
			}
		}
	}
}
