package org.bukkit.craftbukkit.entity;

import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.entity.projectile.IMixinEntityFireball;

public class CraftFireball extends AbstractProjectile implements Fireball
{
	public CraftFireball(CraftServer server, EntityFireball entity)
	{
		super(server, entity);
	}

	public float getYield()
	{
		return ((IMixinEntityFireball) getHandle()).getBukkitYield();
	}

	public boolean isIncendiary()
	{
		return ((IMixinEntityFireball) getHandle()).isIncendiary();
	}

	public void setIsIncendiary(boolean isIncendiary)
	{
		((IMixinEntityFireball) getHandle()).setIncendiary(isIncendiary);
	}

	public void setYield(float yield)
	{
		((IMixinEntityFireball) getHandle()).setBukkitYield(yield);
	}

	public ProjectileSource getShooter()
	{
		return ((IMixinEntity) getHandle()).getProjectileSource();
	}

	public void setShooter(ProjectileSource shooter)
	{
		if(shooter instanceof CraftLivingEntity)
		{
			getHandle().shootingEntity = ((CraftLivingEntity) shooter).getHandle();
		}
		else
		{
			getHandle().shootingEntity = null;
		}
		((IMixinEntity) getHandle()).setProjectileSource(shooter);
	}

	public Vector getDirection()
	{
		return new Vector(getHandle().accelerationX, getHandle().accelerationY, getHandle().accelerationZ);
	}

	public void setDirection(Vector direction)
	{
		Validate.notNull(direction, "Direction can not be null");
		double x = direction.getX();
		double y = direction.getY();
		double z = direction.getZ();
		double magnitude = (double) MathHelper.sqrt_double(x * x + y * y + z * z);
		getHandle().accelerationX = x / magnitude;
		getHandle().accelerationY = y / magnitude;
		getHandle().accelerationZ = z / magnitude;
	}

	@Override
	public EntityFireball getHandle()
	{
		return (EntityFireball) entity;
	}

	@Override
	public String toString()
	{
		return "CraftFireball";
	}

	public EntityType getType()
	{
		return EntityType.UNKNOWN;
	}

	@Deprecated
	public void _INVALID_setShooter(LivingEntity shooter)
	{
		setShooter(shooter);
	}

	@Deprecated
	public LivingEntity _INVALID_getShooter()
	{
		if(getHandle().shootingEntity != null)
		{
			return (LivingEntity) ((IMixinEntity) getHandle().shootingEntity).getBukkitEntity();
		}
		return null;
	}
}
