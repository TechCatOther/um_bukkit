package org.ultramine.mods.bukkit.mixin.entity;

import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(net.minecraft.entity.Entity.class)
public abstract class MixinEntity implements IMixinEntity
{
	@Shadow private int fire;
	@Shadow public World worldObj;
	@Shadow public float rotationYaw;
	@Shadow public float rotationPitch;
	@Shadow public Entity ridingEntity;
	@Shadow private double entityRiderPitchDelta;
	@Shadow private double entityRiderYawDelta;
	@Shadow protected boolean isImmuneToFire;
	@Shadow protected DataWatcher dataWatcher;
	@Shadow public int hurtResistantTime;
	@Shadow public double posX;
	@Shadow public double posY;
	@Shadow public double posZ;

	@Shadow public abstract void setLocationAndAngles(double x, double y, double z, float yaw, float pitch);
	@Shadow public abstract void setFire(int p_70015_1_);
	@Shadow public abstract boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_);
	@Shadow public abstract int getAir();
	@Shadow public abstract void setAir(int p_70050_1_);
	@Shadow public abstract boolean isEntityInvulnerable();
	@Shadow protected abstract void setBeenAttacked();
	@Shadow public abstract void playSound(String p_85030_1_, float p_85030_2_, float p_85030_3_);
	@Shadow public abstract ItemStack[] getLastActiveItems();

	protected CraftEntity bukkitEntity;
	public ProjectileSource projectileSource;
	public String spawnReason;

	@Override
	public int getFireTicks()
	{
		return fire;
	}

	@Override
	public void setFireTicks(int ticks)
	{
		this.fire = ticks;
	}

	@Override
	public CraftEntity getBukkitEntity()
	{
		if(bukkitEntity == null)
			bukkitEntity = CraftEntity.getEntity(((IMixinWorld) worldObj).getServer(), (Entity) (Object) this);
		return bukkitEntity;
	}

	@Override
	public ProjectileSource getProjectileSource()
	{
		return projectileSource;
	}

	@Override
	public String getSpawnReason()
	{
		return spawnReason;
	}

	@Override
	public void setSpawnReason(String spawnReason)
	{
		this.spawnReason = spawnReason;
	}

	@Override
	public void setProjectileSource(ProjectileSource projectileSource)
	{
		this.projectileSource = projectileSource;
	}

	@Override
	public void teleportTo(Location exit, boolean portal)
	{
		throw new UnsupportedOperationException(); //TODO
	}

	@Overwrite
	public void mountEntity(Entity entity)
	{
		setPassengerOf(entity);
	}

	@Override
	public void setPassengerOf(Entity entity)
	{
		// mountEntity(null) doesn't really fly for overloaded methods,
		// so this method is needed
		Entity originalVehicle = this.ridingEntity;
		Entity originalPassenger = this.ridingEntity == null ? null : this.ridingEntity.riddenByEntity;
		PluginManager pluginManager = Bukkit.getPluginManager();
		this.getBukkitEntity(); // make sure bukkitEntity is initialised
		// CraftBukkit end
		this.entityRiderPitchDelta = 0.0D;
		this.entityRiderYawDelta = 0.0D;

		if(entity == null)
		{
			if(this.ridingEntity != null)
			{
				// CraftBukkit start
				if((this.bukkitEntity instanceof LivingEntity) && (((IMixinEntity) ridingEntity).getBukkitEntity() instanceof Vehicle))
				{
					VehicleExitEvent event = new VehicleExitEvent((Vehicle) ((IMixinEntity) ridingEntity).getBukkitEntity(), (LivingEntity) this.bukkitEntity);
					pluginManager.callEvent(event);

					if(event.isCancelled() || this.ridingEntity != originalVehicle)
					{
						return;
					}
				}

				// CraftBukkit end
				pluginManager.callEvent(new org.spigotmc.event.entity.EntityDismountEvent(this.getBukkitEntity(), ((IMixinEntity) ridingEntity).getBukkitEntity()));     // Spigot
				this.setLocationAndAngles(this.ridingEntity.posX, this.ridingEntity.boundingBox.minY + (double) this.ridingEntity.height, this.ridingEntity.posZ, this.rotationYaw, this.rotationPitch);
				this.ridingEntity.riddenByEntity = null;
			}

			this.ridingEntity = null;
		}
		else
		{
			// CraftBukkit start
			if((this.bukkitEntity instanceof LivingEntity) && (((IMixinEntity) entity).getBukkitEntity() instanceof Vehicle) && entity.worldObj.chunkExists((int) entity.posX >> 4, (int) entity.posZ >> 4))
			{
				// It's possible to move from one vehicle to another.  We need to check if they're already in a vehicle, and fire an exit event if they are.
				VehicleExitEvent exitEvent = null;

				if(this.ridingEntity != null && ((IMixinEntity) ridingEntity).getBukkitEntity() instanceof Vehicle)
				{
					exitEvent = new VehicleExitEvent((Vehicle) ((IMixinEntity) ridingEntity).getBukkitEntity(), (LivingEntity) this.bukkitEntity);
					pluginManager.callEvent(exitEvent);

					if(exitEvent.isCancelled() || this.ridingEntity != originalVehicle || (this.ridingEntity != null && this.ridingEntity.riddenByEntity != originalPassenger))
					{
						return;
					}
				}

				VehicleEnterEvent event = new VehicleEnterEvent((Vehicle) ((IMixinEntity) entity).getBukkitEntity(), this.bukkitEntity);
				pluginManager.callEvent(event);

				// If a plugin messes with the vehicle or the vehicle's passenger
				if(event.isCancelled() || this.ridingEntity != originalVehicle || (this.ridingEntity != null && this.ridingEntity.riddenByEntity != originalPassenger))
				{
					// If we only cancelled the enterevent then we need to put the player in a decent position.
					if(exitEvent != null && this.ridingEntity == originalVehicle && this.ridingEntity != null && this.ridingEntity.riddenByEntity == originalPassenger)
					{
						this.setLocationAndAngles(this.ridingEntity.posX, this.ridingEntity.boundingBox.minY + (double) this.ridingEntity.height, this.ridingEntity.posZ, this.rotationYaw, this.rotationPitch);
						this.ridingEntity.riddenByEntity = null;
						this.ridingEntity = null;
					}

					return;
				}
			}

			// CraftBukkit end
			// Spigot Start
			if(entity.worldObj.chunkExists((int) entity.posX >> 4, (int) entity.posZ >> 4))
			{
				org.spigotmc.event.entity.EntityMountEvent event = new org.spigotmc.event.entity.EntityMountEvent(this.getBukkitEntity(), ((IMixinEntity) entity).getBukkitEntity());
				pluginManager.callEvent(event);

				if(event.isCancelled())
				{
					return;
				}
			}

			// Spigot End

			if(this.ridingEntity != null)
			{
				this.ridingEntity.riddenByEntity = null;
			}

			this.ridingEntity = entity;
			entity.riddenByEntity = (Entity) (Object) this;
		}
	}

	public void onStruckByLightning(EntityLightningBolt entity)
	{
		// CraftBukkit start
		final org.bukkit.entity.Entity thisBukkitEntity = this.getBukkitEntity();
		if(thisBukkitEntity == null) return; // Cauldron - skip mod entities with no wrapper (TODO: create a wrapper)
		if(entity == null) return; // Cauldron - skip null entities, see #392
		final org.bukkit.entity.Entity stormBukkitEntity = ((IMixinEntity) entity).getBukkitEntity();
		if(stormBukkitEntity == null) return; // Cauldron - skip mod entities with no wrapper (TODO: create a wrapper)
		final PluginManager pluginManager = Bukkit.getPluginManager();

		if(thisBukkitEntity instanceof Hanging)
		{
			HangingBreakByEntityEvent hangingEvent = new HangingBreakByEntityEvent((Hanging) thisBukkitEntity, stormBukkitEntity);
			PaintingBreakByEntityEvent paintingEvent = null;

			if(thisBukkitEntity instanceof Painting)
			{
				paintingEvent = new PaintingBreakByEntityEvent((Painting) thisBukkitEntity, stormBukkitEntity);
			}

			pluginManager.callEvent(hangingEvent);

			if(paintingEvent != null)
			{
				paintingEvent.setCancelled(hangingEvent.isCancelled());
				pluginManager.callEvent(paintingEvent);
			}

			if(hangingEvent.isCancelled() || (paintingEvent != null && paintingEvent.isCancelled()))
			{
				return;
			}
		}

		if(this.isImmuneToFire)
		{
			return;
		}
		CraftEventFactory.entityDamage = entity;
		if(!this.attackEntityFrom(DamageSource.inFire, 5.0F))
		{
			CraftEventFactory.entityDamage = null;
			return;
		}

		// CraftBukkit end
		++this.fire;

		if(this.fire == 0)
		{
			// CraftBukkit start - Call a combust event when lightning strikes
			EntityCombustByEntityEvent entityCombustEvent = new EntityCombustByEntityEvent(stormBukkitEntity, thisBukkitEntity, 8);
			pluginManager.callEvent(entityCombustEvent);

			if(!entityCombustEvent.isCancelled())
			{
				this.setFire(entityCombustEvent.getDuration());
			}

			// CraftBukkit end
		}
	}

	//TODO VehicleBlockCollisionEvent, EntityCombustEvent
//	public void moveEntity(double p_70091_1_, double p_70091_3_, double p_70091_5_)
//	{
//
//	}
}
