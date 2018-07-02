package org.bukkit.craftbukkit.entity;

import net.minecraft.entity.item.EntityBoat;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;

public class CraftBoat extends CraftVehicle implements Boat
{

	public CraftBoat(CraftServer server, EntityBoat entity)
	{
		super(server, entity);
	}

	public double getMaxSpeed()
	{ //TODO
//        return getHandle().maxSpeed;
		return 0;
	}

	public void setMaxSpeed(double speed)
	{
		if(speed >= 0D)
		{
//            getHandle().maxSpeed = speed;
		}
	}

	public double getOccupiedDeceleration()
	{
//        return getHandle().occupiedDeceleration;
		return 0;
	}

	public void setOccupiedDeceleration(double speed)
	{
		if(speed >= 0D)
		{
//            getHandle().occupiedDeceleration = speed;
		}
	}

	public double getUnoccupiedDeceleration()
	{
//        return getHandle().unoccupiedDeceleration;
		return 0;
	}

	public void setUnoccupiedDeceleration(double speed)
	{
//        getHandle().unoccupiedDeceleration = speed;
	}

	public boolean getWorkOnLand()
	{
//        return getHandle().landBoats;
		return false;
	}

	public void setWorkOnLand(boolean workOnLand)
	{
//        getHandle().landBoats = workOnLand;
	}

	@Override
	public EntityBoat getHandle()
	{
		return (EntityBoat) entity;
	}

	@Override
	public String toString()
	{
		return "CraftBoat";
	}

	public EntityType getType()
	{
		return EntityType.BOAT;
	}
}
