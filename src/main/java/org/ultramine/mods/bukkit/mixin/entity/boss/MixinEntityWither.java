package org.ultramine.mods.bukkit.mixin.entity.boss;

import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLivingBase;

import java.util.List;

@Mixin(net.minecraft.entity.boss.EntityWither.class)
public abstract class MixinEntityWither extends EntityMob
{
	public MixinEntityWither(World p_i1738_1_)
	{
		super(p_i1738_1_);
	}

	@Final
	@Shadow private static IEntitySelector attackEntitySelector;
	@Shadow private int[] field_82223_h = new int[2];
	@Shadow private int[] field_82224_i = new int[2];
	@Shadow private int field_82222_j;
	@Shadow public abstract int func_82212_n();
	@Shadow public abstract void func_82215_s(int p_82215_1_);
	@Shadow public abstract int getWatchedTargetId(int p_82203_1_);
	@Shadow public abstract void func_82211_c(int p_82211_1_, int p_82211_2_);
	@Shadow protected abstract void func_82216_a(int p_82216_1_, EntityLivingBase p_82216_2_);
	@Shadow protected abstract void func_82209_a(int p_82209_1_, double p_82209_2_, double p_82209_4_, double p_82209_6_, boolean p_82209_8_);

	@Overwrite
	protected void updateAITasks()
	{
		int i;
		if (this.func_82212_n() > 0)
		{
			i = this.func_82212_n() - 1;
			if (i <= 0)
			{
				this.worldObj.newExplosion(this, this.posX, this.posY + this.getEyeHeight(), this.posZ, 7.0F, false, this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"));
				this.worldObj.playBroadcastSound(1013, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
			}
			this.func_82215_s(i);
			if (this.ticksExisted % 10 == 0)
				((IMixinEntityLivingBase) this).heal(10.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.WITHER_SPAWN);
		}
		else
		{
			super.updateAITasks();
			int i1;
			int j1;
			for (i = 1; i < 3; ++i)
			{
				if (this.ticksExisted >= this.field_82223_h[i - 1])
				{
					this.field_82223_h[i - 1] = this.ticksExisted + 10 + this.rand.nextInt(10);
					int k1;
					if (this.worldObj.difficultySetting == EnumDifficulty.NORMAL || this.worldObj.difficultySetting == EnumDifficulty.HARD)
					{
						j1 = i - 1;
						k1 = this.field_82224_i[i - 1];
						this.field_82224_i[j1] = this.field_82224_i[i - 1] + 1;
						if (k1 > 15)
						{
							float f = 10.0F;
							float f1 = 5.0F;
							double d0 = MathHelper.getRandomDoubleInRange(this.rand, this.posX - f, this.posX + f);
							double d1 = MathHelper.getRandomDoubleInRange(this.rand, this.posY - f1, this.posY + f1);
							double d2 = MathHelper.getRandomDoubleInRange(this.rand, this.posZ - f, this.posZ + f);
							this.func_82209_a(i + 1, d0, d1, d2, true);
							this.field_82224_i[i - 1] = 0;
						}
					}
					i1 = this.getWatchedTargetId(i);
					if (i1 > 0)
					{
						Entity entity = this.worldObj.getEntityByID(i1);
						if (entity != null && entity.isEntityAlive() && this.getDistanceSqToEntity(entity) <= 900.0D && this.canEntityBeSeen(entity))
						{
							this.func_82216_a(i + 1, (EntityLivingBase) entity);
							this.field_82223_h[i - 1] = this.ticksExisted + 40 + this.rand.nextInt(20);
							this.field_82224_i[i - 1] = 0;
						}
						else
						{
							this.func_82211_c(i, 0);
						}
					}
					else
					{
						List list = this.worldObj.selectEntitiesWithinAABB(EntityLivingBase.class, this.boundingBox.expand(20.0D, 8.0D, 20.0D), attackEntitySelector);
						for (k1 = 0; k1 < 10 && !list.isEmpty(); ++k1)
						{
							EntityLivingBase entitylivingbase = (EntityLivingBase) list.get(this.rand.nextInt(list.size()));
							if (entitylivingbase != this && entitylivingbase.isEntityAlive() && this.canEntityBeSeen(entitylivingbase))
							{
								if (entitylivingbase instanceof EntityPlayer)
									if (!((EntityPlayer) entitylivingbase).capabilities.disableDamage)
										this.func_82211_c(i, entitylivingbase.getEntityId());
								else
									this.func_82211_c(i, entitylivingbase.getEntityId());
								break;
							}
							list.remove(entitylivingbase);
						}
					}
				}
			}
			if (this.getAttackTarget() != null)
				this.func_82211_c(0, this.getAttackTarget().getEntityId());
			else
				this.func_82211_c(0, 0);
			if (this.field_82222_j > 0)
			{
				--this.field_82222_j;
				if (this.field_82222_j == 0 && this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))
				{
					i = MathHelper.floor_double(this.posY);
					i1 = MathHelper.floor_double(this.posX);
					j1 = MathHelper.floor_double(this.posZ);
					boolean flag = false;
					int l1 = -1;
					while (true)
					{
						if (l1 > 1)
						{
							if (flag)
								this.worldObj.playAuxSFXAtEntity(null, 1012, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
							break;
						}
						for (int i2 = -1; i2 <= 1; ++i2)
						{
							for (int j = 0; j <= 3; ++j)
							{
								int j2 = i1 + l1;
								int k = i + j;
								int l = j1 + i2;
								Block block = this.worldObj.getBlock(j2, k, l);
								if (!block.isAir(this.worldObj, j2, k, l) && block.canEntityDestroy(this.worldObj, j2, k, l, this))
								{
									if (CraftEventFactory.callEntityChangeBlockEvent(this, j2, k, l, Blocks.air, 0).isCancelled())
										continue;
									flag = this.worldObj.func_147480_a(j2, k, l, true) || flag;
								}
							}
						}
						++l1;
					}
				}
			}
			if (this.ticksExisted % 20 == 0)
				((IMixinEntityLivingBase) this).heal(1.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.WITHER_SPAWN);
		}
	}
}
