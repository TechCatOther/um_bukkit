package org.ultramine.mods.bukkit.mixin.inventory;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.inventory.AnimalChest;
import org.spongepowered.asm.mixin.Mixin;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinAnimalChest;

@Mixin(AnimalChest.class)
public class MixinAnimalChest implements IMixinAnimalChest
{
	private EntityAnimal entityAnimal;

	@Override
	public EntityAnimal getAnimal()
	{
		return this.entityAnimal;
	}

	@Override
	public void setAnimal(EntityAnimal entityAnimal)
	{
		this.entityAnimal = entityAnimal;
	}
}
