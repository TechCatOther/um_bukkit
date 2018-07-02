package org.ultramine.mods.bukkit.interfaces.entity;

import net.minecraft.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.projectiles.ProjectileSource;

public interface IMixinEntity
{
	int getFireTicks();

	void setFireTicks(int ticks);

	CraftEntity getBukkitEntity();

	ProjectileSource getProjectileSource();

	void setProjectileSource(ProjectileSource projectileSource);

	void setPassengerOf(Entity entity);

	void teleportTo(Location exit, boolean portal);

	String getSpawnReason();

	void setSpawnReason(String spawnReason);
}
