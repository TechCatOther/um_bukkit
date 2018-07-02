package org.bukkit.craftbukkit.entity;


import net.minecraft.entity.EntityLivingBase;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.entity.projectile.IMixinThrowable;

public class CraftProjectile extends AbstractProjectile implements Projectile
{ // Cauldron - concrete
	public CraftProjectile(CraftServer server, net.minecraft.entity.Entity entity)
	{
		super(server, entity);
	}

	public ProjectileSource getShooter()
	{
		return ((IMixinEntity) getHandle()).getProjectileSource();
	}

	public void setShooter(ProjectileSource shooter)
	{
		if(shooter instanceof CraftLivingEntity)
		{
			((IMixinThrowable) getHandle()).setThrower((net.minecraft.entity.EntityLivingBase) ((CraftLivingEntity) shooter).entity);
			if(shooter instanceof CraftHumanEntity)
			{
				((IMixinThrowable) getHandle()).setThrowerName(((CraftHumanEntity) shooter).getName());
			}
		}
		else
		{
			((IMixinThrowable) getHandle()).setThrower(null);
			((IMixinThrowable) getHandle()).setThrowerName(null);
		}
		((IMixinEntity) getHandle()).setProjectileSource(shooter);
	}

	@Override
	public net.minecraft.entity.projectile.EntityThrowable getHandle()
	{
		return (net.minecraft.entity.projectile.EntityThrowable) entity;
	}

	@Override
	public String toString()
	{
		return "CraftProjectile";
	}

	// Cauldron start
	@Override
	public EntityType getType()
	{
		return EntityType.UNKNOWN;
	}
	// Cauldron end

	@Deprecated
	public LivingEntity _INVALID_getShooter()
	{
		EntityLivingBase thrower = getHandle().getThrower();
		if(thrower == null)
		{
			return null;
		}
		return (LivingEntity) ((IMixinEntity) thrower).getBukkitEntity();
	}

	@Deprecated
	public void _INVALID_setShooter(LivingEntity shooter)
	{
		if(shooter == null)
		{
			return;
		}
		((IMixinThrowable) getHandle()).setThrower(((CraftLivingEntity) shooter).getHandle());
		if(shooter instanceof CraftHumanEntity)
		{
			((IMixinThrowable) getHandle()).setThrowerName(((CraftHumanEntity) shooter).getName());
		}
	}
}
