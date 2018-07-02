package org.ultramine.mods.bukkit.interfaces.entity.player;

import org.bukkit.Location;

public interface IMixinPlayerMP extends IMixinPlayer
{
	String getTranslator();

	int getLastExperience();

	void setLastExperience(int lastExperience);

	int getField_147101_bU();

	void setField_147101_bU(int field_147101_bU);


	String getBukkitDisplayName();

	void setBukkitDisplayName(String displayName);

	String getBukkitListName();

	void setBukkitListName(String listName);

	Location getCompassTarget();

	void setCompassTarget(Location compassTarget);

	int getNewExp();

	void setNewExp(int newExp);

	int getNewLevel();

	void setNewLevel(int newLevel);

	int getNewTotalExp();

	void setNewTotalExp(int newTotalExp);

	boolean isKeepLevel();

	void setKeepLevel(boolean keepLevel);

	double getMaxHealthCache();

	void setMaxHealthCache(double maxHealthCache);

	boolean isCollidesWithEntities();

	void setCollidesWithEntities(boolean collidesWithEntities);

	int nextContainerCounter();

	void closeScreenSilent();
}
