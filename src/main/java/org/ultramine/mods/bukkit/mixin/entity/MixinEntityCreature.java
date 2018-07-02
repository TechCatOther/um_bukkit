package org.ultramine.mods.bukkit.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityCreature;

@Mixin(net.minecraft.entity.EntityCreature.class)
public class MixinEntityCreature implements IMixinEntityCreature
{
	@Shadow
	private PathEntity pathToEntity;
	@Shadow
	protected Entity entityToAttack;

	@Override
	public PathEntity getPathToEntity()
	{
		return pathToEntity;
	}

	@Override
	public void setPathToEntity(PathEntity pathToEntity)
	{
		this.pathToEntity = pathToEntity;
	}

	@Override
	public Entity getEntityToAttack()
	{
		return entityToAttack;
	}

	@Override
	public void setEntityToAttack(Entity entityToAttack)
	{
		this.entityToAttack = entityToAttack;
	}
}
