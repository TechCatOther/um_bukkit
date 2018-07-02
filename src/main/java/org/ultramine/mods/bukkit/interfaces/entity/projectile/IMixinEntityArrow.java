package org.ultramine.mods.bukkit.interfaces.entity.projectile;

public interface IMixinEntityArrow
{
	boolean isInGround();

	int getKnockbackStrength();
}
