package org.bukkit.craftbukkit.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityCreature;

public class CraftCreature extends CraftLivingEntity implements Creature
{
	public CraftCreature(CraftServer server, EntityCreature entity)
	{
		super(server, entity);
	}

	public void setTarget(LivingEntity target)
	{
		EntityCreature entity = getHandle();
		IMixinEntityCreature entityMix = (IMixinEntityCreature) getHandle();
		if(target == null)
		{
			entityMix.setEntityToAttack(null);
		}
		else if(target instanceof CraftLivingEntity)
		{
			entityMix.setEntityToAttack(((CraftLivingEntity) target).getHandle());
			entityMix.setPathToEntity(entity.worldObj.getPathEntityToEntity(entity, entityMix.getEntityToAttack(), 16.0F, true, false, false, true));
		}
	}

	public CraftLivingEntity getTarget()
	{
		IMixinEntityCreature entity = (IMixinEntityCreature) getHandle();
		if(entity.getEntityToAttack() == null) return null;
		if(!(entity.getEntityToAttack() instanceof EntityLivingBase)) return null;

		return (CraftLivingEntity) ((IMixinEntity) entity.getEntityToAttack()).getBukkitEntity();
	}

	@Override
	public EntityCreature getHandle()
	{
		return (EntityCreature) entity;
	}

	@Override
	public String toString()
	{
		return this.entityName; // Cauldron
	}
}
