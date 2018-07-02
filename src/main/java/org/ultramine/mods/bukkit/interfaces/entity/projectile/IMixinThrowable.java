package org.ultramine.mods.bukkit.interfaces.entity.projectile;

import net.minecraft.entity.EntityLivingBase;

public interface IMixinThrowable
{
	EntityLivingBase getThrower();

	void setThrower(EntityLivingBase thrower);

	String getThrowerName();

	void setThrowerName(String throwerName);
}
