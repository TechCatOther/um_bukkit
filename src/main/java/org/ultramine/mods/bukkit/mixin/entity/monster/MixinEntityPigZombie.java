package org.ultramine.mods.bukkit.mixin.entity.monster;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.monster.IMixinEntityPigZombie;

@Mixin(net.minecraft.entity.monster.EntityPigZombie.class)
public class MixinEntityPigZombie implements IMixinEntityPigZombie
{
	@Shadow
	private int angerLevel;

	@Override
	public int getAngerLevel()
	{
		return angerLevel;
	}

	@Override
	public void setAngerLevel(int angerLevel)
	{
		this.angerLevel = angerLevel;
	}
}
