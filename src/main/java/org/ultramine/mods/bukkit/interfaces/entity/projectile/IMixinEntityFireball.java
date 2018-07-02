package org.ultramine.mods.bukkit.interfaces.entity.projectile;

public interface IMixinEntityFireball
{
	void setDirection(double x, double y, double z);

	float getBukkitYield();

	void setBukkitYield(float bukkitYield);

	boolean isIncendiary();

	void setIncendiary(boolean isIncendiary);
}
