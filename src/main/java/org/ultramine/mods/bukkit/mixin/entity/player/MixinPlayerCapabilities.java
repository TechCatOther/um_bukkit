package org.ultramine.mods.bukkit.mixin.entity.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.player.IMixinPlayerCapabilities;

@Mixin(net.minecraft.entity.player.PlayerCapabilities.class)
public abstract class MixinPlayerCapabilities implements IMixinPlayerCapabilities
{
	@Shadow
	private float flySpeed;
	@Shadow
	private float walkSpeed;

	@Override
	public float getFlySpeed()
	{
		return flySpeed;
	}

	@Override
	public void setFlySpeed(float flySpeed)
	{
		this.flySpeed = flySpeed;
	}

	@Override
	public float getWalkSpeed()
	{
		return walkSpeed;
	}

	@Override
	public void setWalkSpeed(float walkSpeed)
	{
		this.walkSpeed = walkSpeed;
	}
}
