package org.ultramine.mods.bukkit.interfaces.entity.player;

public interface IMixinPlayerCapabilities
{
	float getFlySpeed();

	void setFlySpeed(float flySpeed);

	float getWalkSpeed();

	void setWalkSpeed(float walkSpeed);
}
