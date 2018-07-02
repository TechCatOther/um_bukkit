package org.ultramine.mods.bukkit.mixin.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;

@Mixin(EntityMinecart.class)
public abstract class MixinEntityMinecart extends Entity
{
	@Shadow public abstract float getDamage();
	@Shadow public abstract void setRollingAmplitude(int p_70497_1_);
	@Shadow public abstract void setRollingDirection(int p_70494_1_);
	@Shadow public abstract int getRollingDirection();
	@Shadow public abstract void setDamage(float p_70492_1_);
	@Shadow public abstract boolean hasCustomInventoryName();
	@Shadow public abstract void killMinecart(DamageSource p_94095_1_);

	public MixinEntityMinecart(World p_i1582_1_)
	{
		super(p_i1582_1_);
	}

	@Overwrite
	public boolean attackEntityFrom(DamageSource damageSource, float damage)
	{
		if (!this.worldObj.isRemote && !this.isDead)
		{
			if (this.isEntityInvulnerable())
			{
				return false;
			}
			else
			{
				Vehicle vehicle = (Vehicle) ((IMixinEntity) this).getBukkitEntity();
				org.bukkit.entity.Entity passenger = (damageSource.getEntity() == null) ? null : ((IMixinEntity) damageSource.getEntity()).getBukkitEntity();
				VehicleDamageEvent event = new VehicleDamageEvent(vehicle, passenger, damage);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled())
					return true;
				damage = (float) event.getDamage();
				this.setRollingDirection(-this.getRollingDirection());
				this.setRollingAmplitude(10);
				this.setBeenAttacked();
				this.setDamage(this.getDamage() + damage * 10.0F);
				boolean flag = damageSource.getEntity() instanceof EntityPlayer && ((EntityPlayer) damageSource.getEntity()).capabilities.isCreativeMode;
				if (flag || this.getDamage() > 40.0F)
				{
					if (this.riddenByEntity != null)
						this.riddenByEntity.mountEntity(this);
					VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, passenger);
					Bukkit.getPluginManager().callEvent(destroyEvent);
					if (destroyEvent.isCancelled())
					{
						this.setDamage(40); // Maximize damage so this doesn't get triggered again right away
						return true;
					}
					if (flag && !this.hasCustomInventoryName())
						this.setDead();
					else
						this.killMinecart(damageSource);
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
