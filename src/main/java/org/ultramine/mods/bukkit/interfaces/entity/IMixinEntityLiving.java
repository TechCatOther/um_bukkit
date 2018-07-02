package org.ultramine.mods.bukkit.interfaces.entity;

public interface IMixinEntityLiving
{
	boolean isPersistenceRequired();

	void setPersistenceRequired(boolean persistenceRequired);

	boolean isCanPickUpLoot();

	void setCanPickUpLoot(boolean canPickUpLoot);
}
