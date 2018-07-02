package org.ultramine.mods.bukkit.interfaces.entity.player;

import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLivingBase;

public interface IMixinPlayer extends IMixinEntityLivingBase
{
	boolean isSleeping();

	int getSleepTimer();
}
