package org.ultramine.mods.bukkit.mixin.entity.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.projectile.IMixinEntityArrow;

@Mixin(net.minecraft.entity.projectile.EntityArrow.class)
public class MixinEntityArrow implements IMixinEntityArrow
{
	@Shadow
	public boolean inGround;
	@Shadow
	private int knockbackStrength;

	@Override
	public boolean isInGround()
	{
		return inGround;
	}

	@Override
	public int getKnockbackStrength()
	{
		return knockbackStrength;
	}
}
