package org.ultramine.mods.bukkit.interfaces.entity.passive;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.AnimalChest;

public interface IMixinEntityHorse
{
	void createChest();

	AnimalChest getHorseChest();

	void setHorseChest(AnimalChest horseChest);

	int getMaxDomestication();

	void setMaxDomestication(int maxDomestication);

	IAttribute getStaticHorseJumpStrength();
}
