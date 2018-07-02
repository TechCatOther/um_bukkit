package org.ultramine.mods.bukkit.mixin.entity.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;

import java.util.List;

@Mixin(EntityBoat.class)
public abstract class MixinEntityBoat extends Entity
{
	public MixinEntityBoat(World p_i1582_1_)
	{
		super(p_i1582_1_);
	}

	@Shadow public abstract void setDamageTaken(float p_70266_1_);
	@Shadow public abstract float getDamageTaken();
	@Shadow public abstract void setTimeSinceHit(int p_70265_1_);
	@Shadow public abstract int getTimeSinceHit();
	@Shadow private boolean isBoatEmpty;
	@Shadow private double speedMultiplier;
	@Shadow private int boatPosRotationIncrements;
	@Shadow private double boatX;
	@Shadow private double boatY;
	@Shadow private double boatZ;
	@Shadow private double boatYaw;
	@Shadow private double boatPitch;
	@Shadow public abstract void setForwardDirection(int p_70269_1_);
	@Shadow public abstract int getForwardDirection();

	@Overwrite
	public void onUpdate()
	{
		super.onUpdate();
		if (getTimeSinceHit() > 0)
			this.setTimeSinceHit(this.getTimeSinceHit() - 1);
		if (this.getDamageTaken() > 0.0F)
			this.setDamageTaken(this.getDamageTaken() - 1.0F);
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		byte b0 = 5;
		double d0 = 0.0D;
		for (int i = 0; i < b0; ++i)
		{
			double d1 = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * i / b0 - 0.125D;
			double d3 = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * (i + 1) / b0 - 0.125D;
			AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.boundingBox.minX, d1, this.boundingBox.minZ, this.boundingBox.maxX, d3, this.boundingBox.maxZ);
			if (this.worldObj.isAABBInMaterial(axisalignedbb, Material.water))
				d0 += 1.0D / b0;
		}
		double d10 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		double d2;
		double d4;
		int j;
		double d11;
		double d12;
		if (d10 > 0.26249999999999996D)
		{
			d2 = Math.cos(this.rotationYaw * 3.141592653589793D / 180.0D);
			d4 = Math.sin(this.rotationYaw * 3.141592653589793D / 180.0D);

			for (j = 0; j < 1.0D + d10 * 60.0D; ++j)
			{
				d11 = this.rand.nextFloat() * 2.0F - 1.0F;
				d12 = (this.rand.nextInt(2) * 2 - 1) * 0.7D;
				double d8;
				double d9;
				if (this.rand.nextBoolean())
				{
					d8 = this.posX - d2 * d11 * 0.8D + d4 * d12;
					d9 = this.posZ - d4 * d11 * 0.8D - d2 * d12;
					this.worldObj.spawnParticle("splash", d8, this.posY - 0.125D, d9, this.motionX, this.motionY, this.motionZ);
				}
				else
				{
					d8 = this.posX + d2 + d4 * d11 * 0.7D;
					d9 = this.posZ + d4 - d2 * d11 * 0.7D;
					this.worldObj.spawnParticle("splash", d8, this.posY - 0.125D, d9, this.motionX, this.motionY, this.motionZ);
				}
			}
		}

		if (this.worldObj.isRemote && this.isBoatEmpty)
		{
			if (this.boatPosRotationIncrements > 0)
			{
				d2 = this.posX + (this.boatX - this.posX) / this.boatPosRotationIncrements;
				d4 = this.posY + (this.boatY - this.posY) / this.boatPosRotationIncrements;
				d11 = this.posZ + (this.boatZ - this.posZ) / this.boatPosRotationIncrements;
				d12 = MathHelper.wrapAngleTo180_double(this.boatYaw - this.rotationYaw);
				this.rotationYaw = (float) (this.rotationYaw + d12 / this.boatPosRotationIncrements);
				this.rotationPitch = (float) (this.rotationPitch + (this.boatPitch - this.rotationPitch) / this.boatPosRotationIncrements);
				--this.boatPosRotationIncrements;
				this.setPosition(d2, d4, d11);
				this.setRotation(this.rotationYaw, this.rotationPitch);
			}
			else
			{
				d2 = this.posX + this.motionX;
				d4 = this.posY + this.motionY;
				d11 = this.posZ + this.motionZ;
				this.setPosition(d2, d4, d11);
				if (this.onGround)
				{
					this.motionX *= 0.5D;
					this.motionY *= 0.5D;
					this.motionZ *= 0.5D;
				}

				this.motionX *= 0.9900000095367432D;
				this.motionY *= 0.949999988079071D;
				this.motionZ *= 0.9900000095367432D;
			}
		}
		else
		{
			if (d0 < 1.0D)
			{
				d2 = d0 * 2.0D - 1.0D;
				this.motionY += 0.03999999910593033D * d2;
			}
			else
			{
				if (this.motionY < 0.0D)
					this.motionY /= 2.0D;
				this.motionY += 0.007000000216066837D;
			}
			if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityLivingBase)
			{
				EntityLivingBase entitylivingbase = (EntityLivingBase) this.riddenByEntity;
				float f = this.riddenByEntity.rotationYaw + -entitylivingbase.moveStrafing * 90.0F;
				this.motionX += -Math.sin(f * 3.1415927F / 180.0F) * this.speedMultiplier * entitylivingbase.moveForward * 0.05000000074505806D;
				this.motionZ += Math.cos(f * 3.1415927F / 180.0F) * this.speedMultiplier * entitylivingbase.moveForward * 0.05000000074505806D;
			}
			d2 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			if (d2 > 0.35D)
			{
				d4 = 0.35D / d2;
				this.motionX *= d4;
				this.motionZ *= d4;
				d2 = 0.35D;
			}
			if (d2 > d10 && this.speedMultiplier < 0.35D)
			{
				this.speedMultiplier += (0.35D - this.speedMultiplier) / 35.0D;
				if (this.speedMultiplier > 0.35D)
					this.speedMultiplier = 0.35D;
			}
			else
			{
				this.speedMultiplier -= (this.speedMultiplier - 0.07D) / 35.0D;
				if (this.speedMultiplier < 0.07D)
					this.speedMultiplier = 0.07D;
			}
			int l;
			for (l = 0; l < 4; ++l)
			{
				int i1 = MathHelper.floor_double(this.posX + ((l % 2) - 0.5D) * 0.8D);
				j = MathHelper.floor_double(this.posZ + ((l / 2) - 0.5D) * 0.8D);
				for (int j1 = 0; j1 < 2; ++j1)
				{
					int k = MathHelper.floor_double(this.posY) + j1;
					Block block = this.worldObj.getBlock(i1, k, j);
					if (block == Blocks.snow_layer)
					{
						if (CraftEventFactory.callEntityChangeBlockEvent(this, i1, k, j, Blocks.air, 0).isCancelled())
							continue;
						this.worldObj.setBlockToAir(i1, k, j);
						this.isCollidedHorizontally = false;
					}
					else if (block == Blocks.waterlily)
					{
						if (CraftEventFactory.callEntityChangeBlockEvent(this, i1, k, j, Blocks.air, 0).isCancelled())
							continue;
						this.worldObj.func_147480_a(i1, k, j, true);
						this.isCollidedHorizontally = false;
					}
				}
			}
			if (this.onGround)
			{
				this.motionX *= 0.5D;
				this.motionY *= 0.5D;
				this.motionZ *= 0.5D;
			}
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			if (this.isCollidedHorizontally && d10 > 0.2D)
			{
				if (!this.worldObj.isRemote && !this.isDead)
				{
					Vehicle vehicle = (Vehicle) ((IMixinEntity) this).getBukkitEntity();
					VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, null);
					Bukkit.getPluginManager().callEvent(destroyEvent);
					if (!destroyEvent.isCancelled())
					{
						this.setDead();
						for (l = 0; l < 3; ++l)
							this.func_145778_a(Item.getItemFromBlock(Blocks.planks), 1, 0.0F);
						for (l = 0; l < 2; ++l)
							this.func_145778_a(Items.stick, 1, 0.0F);
					}
				}
			}
			else
			{
				this.motionX *= 0.9900000095367432D;
				this.motionY *= 0.949999988079071D;
				this.motionZ *= 0.9900000095367432D;
			}
			this.rotationPitch = 0.0F;
			d4 = (double) this.rotationYaw;
			d11 = this.prevPosX - this.posX;
			d12 = this.prevPosZ - this.posZ;
			if (d11 * d11 + d12 * d12 > 0.001D)
				d4 = (Math.atan2(d12, d11) * 180.0D / 3.141592653589793D);
			double d7 = MathHelper.wrapAngleTo180_double(d4 - (double) this.rotationYaw);
			if (d7 > 20.0D)
				d7 = 20.0D;
			if (d7 < -20.0D)
				d7 = -20.0D;
			this.rotationYaw = (float) (this.rotationYaw + d7);
			this.setRotation(this.rotationYaw, this.rotationPitch);
			if (!this.worldObj.isRemote)
			{
				List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));
				if (list != null && !list.isEmpty())
					for (Object aList : list)
					{
						Entity entity = (Entity) aList;
						if (entity != this.riddenByEntity && entity.canBePushed() && entity instanceof EntityBoat)
							entity.applyEntityCollision(this);
					}
				if (this.riddenByEntity != null && this.riddenByEntity.isDead)
					this.riddenByEntity = null;
			}
		}
	}

	@Overwrite
	public boolean attackEntityFrom(DamageSource damageSource, float damage)
	{
		if(!this.worldObj.isRemote && !this.isDead)
		{
			if(this.isEntityInvulnerable())
			{
				return false;
			}
			else
			{
				Vehicle vehicle = (Vehicle) ((IMixinEntity) this).getBukkitEntity();
				org.bukkit.entity.Entity attacker = (damageSource.getEntity() == null) ? null : ((IMixinEntity) damageSource.getEntity()).getBukkitEntity();
				VehicleDamageEvent event = new VehicleDamageEvent(vehicle, attacker, damage);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled())
					return true;
				damage = (float) event.getDamage();
				this.setForwardDirection(-this.getForwardDirection());
				this.setTimeSinceHit(10);
				this.setDamageTaken(this.getDamageTaken() + damage * 10.0F);
				this.setBeenAttacked();
				boolean flag = damageSource.getEntity() instanceof EntityPlayer && ((EntityPlayer) damageSource.getEntity()).capabilities.isCreativeMode;
				if (flag || this.getDamageTaken() > 40.0F)
				{
					VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, attacker);
					Bukkit.getPluginManager().callEvent(destroyEvent);
					if (destroyEvent.isCancelled())
					{
						this.setDamageTaken(40F); // Maximize damage so this doesn't get triggered again right away
						return true;
					}
					if (this.riddenByEntity != null)
						this.riddenByEntity.mountEntity(this);
					if (!flag)
						this.func_145778_a(Items.boat, 1, 0.0F);
					this.setDead();
				}
				return true;
			}
		}
		else
		{
			return true;
		}
	}
}
