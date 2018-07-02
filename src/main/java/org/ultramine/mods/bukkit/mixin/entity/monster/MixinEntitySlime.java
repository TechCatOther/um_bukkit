package org.ultramine.mods.bukkit.mixin.entity.monster;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.monster.IMixinEntitySlime;

@Mixin(net.minecraft.entity.monster.EntitySlime.class)
public abstract class MixinEntitySlime implements IMixinEntitySlime
{
	@Shadow
	protected abstract void setSlimeSize(int p_70799_1_);

	@Override
	public void setSlimeSizePub(int size)
	{
		setSlimeSize(size);
	}
}
