package org.ultramine.mods.bukkit.interfaces;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IMixinFoodStats
{
	public void sendUpdatePacket();

	public void sendUpdatePacket(EntityPlayerMP player);

	public int getFoodLevel();

	public void setFoodLevel(int foodLevel);

	public float getFoodSaturationLevel();

	public void setFoodSaturationLevel(float foodSaturationLevel);

	public float getFoodExhaustionLevel();

	public void setFoodExhaustionLevel(float foodExhaustionLevel);

	public int getFoodTimer();

	public void setFoodTimer(int foodTimer);
}
