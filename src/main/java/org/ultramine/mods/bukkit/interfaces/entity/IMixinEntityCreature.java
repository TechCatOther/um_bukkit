package org.ultramine.mods.bukkit.interfaces.entity;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;

public interface IMixinEntityCreature
{
	PathEntity getPathToEntity();

	void setPathToEntity(PathEntity pathToEntity);

	Entity getEntityToAttack();

	void setEntityToAttack(Entity entityToAttack);
}
