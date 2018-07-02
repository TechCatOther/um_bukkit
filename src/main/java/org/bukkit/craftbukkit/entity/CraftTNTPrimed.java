package org.bukkit.craftbukkit.entity;


import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;

public class CraftTNTPrimed extends CraftEntity implements TNTPrimed
{
	private float yield = 4;
	private boolean isIncendiary = false;

	public CraftTNTPrimed(CraftServer server, net.minecraft.entity.item.EntityTNTPrimed entity)
	{
		super(server, entity);
	}

	public float getYield()
	{
		return yield;
	}

	public boolean isIncendiary()
	{
		return isIncendiary;
	}

	public void setIsIncendiary(boolean isIncendiary)
	{
		this.isIncendiary = isIncendiary;
	}

	public void setYield(float yield)
	{
		this.yield = yield;
	}

	public int getFuseTicks()
	{
		return getHandle().fuse;
	}

	public void setFuseTicks(int fuseTicks)
	{
		getHandle().fuse = fuseTicks;
	}

	@Override
	public net.minecraft.entity.item.EntityTNTPrimed getHandle()
	{
		return (net.minecraft.entity.item.EntityTNTPrimed) entity;
	}

	@Override
	public String toString()
	{
		return "CraftTNTPrimed";
	}

	public EntityType getType()
	{
		return EntityType.PRIMED_TNT;
	}

	public Entity getSource()
	{
		net.minecraft.entity.EntityLivingBase source = getHandle().getTntPlacedBy();

		if(source != null)
		{
			Entity bukkitEntity = ((IMixinEntity) source).getBukkitEntity();

			if(bukkitEntity.isValid())
			{
				return bukkitEntity;
			}
		}

		return null;
	}
}
