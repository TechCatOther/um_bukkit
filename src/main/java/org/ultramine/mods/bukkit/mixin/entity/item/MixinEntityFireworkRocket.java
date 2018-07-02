package org.ultramine.mods.bukkit.mixin.entity.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.item.IMixinEntityFireworkRocket;

@Mixin(net.minecraft.entity.item.EntityFireworkRocket.class)
public class MixinEntityFireworkRocket implements IMixinEntityFireworkRocket
{
	@Shadow
	private int lifetime;

	@Override
	public int getLifetime()
	{
		return lifetime;
	}

	@Override
	public void setLifetime(int lifetime)
	{
		this.lifetime = lifetime;
	}
}
