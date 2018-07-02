package org.ultramine.mods.bukkit.mixin.entity.effect;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.util.LightningEffectSwitcher;

import java.util.List;

@Mixin(EntityLightningBolt.class)
public class MixinEntityLightningBolt extends EntityWeatherEffect
{
	public MixinEntityLightningBolt(World p_i1702_1_)
	{
		super(p_i1702_1_);
	}

	@Shadow private int lightningState;

	@Shadow public long boltVertex;

	@Shadow private int boltLivingTime;

	public void constructorInject(World p_i1703_1_, double p_i1703_2_, double p_i1703_4_, double p_i1703_6_)
	{
		this.setLocationAndAngles(p_i1703_2_, p_i1703_4_, p_i1703_6_, 0.0F, 0.0F);
		this.lightningState = 2;
		this.boltVertex = this.rand.nextLong();
		this.boltLivingTime = this.rand.nextInt(3) + 1;
		if (!LightningEffectSwitcher.isEffect && !p_i1703_1_.isRemote && p_i1703_1_.getGameRules().getGameRuleBooleanValue("doFireTick") && (p_i1703_1_.difficultySetting == EnumDifficulty.NORMAL || p_i1703_1_.difficultySetting == EnumDifficulty.HARD) && p_i1703_1_.doChunksNearChunkExist(MathHelper.floor_double(p_i1703_2_), MathHelper.floor_double(p_i1703_4_), MathHelper.floor_double(p_i1703_6_), 10))
		{
			int i = MathHelper.floor_double(p_i1703_2_);
			int j = MathHelper.floor_double(p_i1703_4_);
			int k = MathHelper.floor_double(p_i1703_6_);
			if (p_i1703_1_.getBlock(i, j, k).getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(p_i1703_1_, i, j, k))
				if (!CraftEventFactory.callBlockIgniteEvent(p_i1703_1_, i, j, k, this).isCancelled())
					p_i1703_1_.setBlock(i, j, k, Blocks.fire);
			for (i = 0; i < 4; ++i)
			{
				j = MathHelper.floor_double(p_i1703_2_) + this.rand.nextInt(3) - 1;
				k = MathHelper.floor_double(p_i1703_4_) + this.rand.nextInt(3) - 1;
				int l = MathHelper.floor_double(p_i1703_6_) + this.rand.nextInt(3) - 1;
				if (p_i1703_1_.getBlock(j, k, l).getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(p_i1703_1_, j, k, l))
					if (!CraftEventFactory.callBlockIgniteEvent(p_i1703_1_, j, k, l, this).isCancelled())
						p_i1703_1_.setBlock(j, k, l, Blocks.fire);
			}
		}
		LightningEffectSwitcher.isEffect = false;
	}

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public void onUpdate()
	{
		super.onUpdate();
		if (this.lightningState == 2)
		{
			this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "ambient.weather.thunder", 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);
			this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "random.explode", 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
		}
		--this.lightningState;
		if (this.lightningState < 0)
		{
			if (this.boltLivingTime == 0)
			{
				this.setDead();
			}
			else if (this.lightningState < -this.rand.nextInt(10))
			{
				--this.boltLivingTime;
				this.lightningState = 1;
				this.boltVertex = this.rand.nextLong();
				if (!LightningEffectSwitcher.isEffect && !this.worldObj.isRemote && this.worldObj.getGameRules().getGameRuleBooleanValue("doFireTick") && this.worldObj.doChunksNearChunkExist(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ), 10))
				{
					int i = MathHelper.floor_double(this.posX);
					int j = MathHelper.floor_double(this.posY);
					int k = MathHelper.floor_double(this.posZ);
					if (this.worldObj.getBlock(i, j, k).getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(this.worldObj, i, j, k))
						if (!CraftEventFactory.callBlockIgniteEvent(worldObj, i, j, k, this).isCancelled())
							this.worldObj.setBlock(i, j, k, Blocks.fire);
				}
			}
		}
		if (this.lightningState >= 0 && !LightningEffectSwitcher.isEffect)
		{
			if (this.worldObj.isRemote)
			{
				this.worldObj.lastLightningBolt = 2;
			}
			else
			{
				double d0 = 3.0D;
				List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(this.posX - d0, this.posY - d0, this.posZ - d0, this.posX + d0, this.posY + 6.0D + d0, this.posZ + d0));
				for (Object aList : list)
				{
					Entity entity = (Entity) aList;
					if (!ForgeEventFactory.onEntityStruckByLightning(entity, (EntityLightningBolt) (Object) this))
						entity.onStruckByLightning((EntityLightningBolt) (Object) this);
				}
			}
		}
	}

	@Override
	protected void entityInit()
	{

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbtTagCompound)
	{

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbtTagCompound)
	{

	}
}
