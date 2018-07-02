package org.ultramine.mods.bukkit.interfaces.inventory;

import net.minecraft.entity.passive.EntityAnimal;

public interface IMixinAnimalChest
{
	EntityAnimal getAnimal();

	void setAnimal(EntityAnimal entityAnimal);
}
