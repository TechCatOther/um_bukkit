package org.ultramine.mods.bukkit.mixin.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.projectile.IMixinThrowable;

@Mixin(net.minecraft.entity.projectile.EntityThrowable.class)
public abstract class MixinThrowable implements IMixinThrowable
{
	@Shadow
	private EntityLivingBase thrower;
	@Shadow
	private String throwerName;

	@Override
	public EntityLivingBase getThrower()
	{
		return thrower;
	}

	@Override
	public void setThrower(EntityLivingBase thrower)
	{
		this.thrower = thrower;
	}

	@Override
	public String getThrowerName()
	{
		return throwerName;
	}

	@Override
	public void setThrowerName(String throwerName)
	{
		this.throwerName = throwerName;
	}
}
