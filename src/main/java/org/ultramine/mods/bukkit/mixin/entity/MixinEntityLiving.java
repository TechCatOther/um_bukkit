package org.ultramine.mods.bukkit.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLiving;

@Mixin(net.minecraft.entity.EntityLiving.class)
public abstract class MixinEntityLiving implements IMixinEntityLiving
{
	@Shadow
	private boolean persistenceRequired;
	@Shadow
	private boolean canPickUpLoot;

	@Override
	public boolean isPersistenceRequired()
	{
		return persistenceRequired;
	}

	@Override
	public void setPersistenceRequired(boolean persistenceRequired)
	{
		this.persistenceRequired = persistenceRequired;
	}

	@Override
	public boolean isCanPickUpLoot()
	{
		return canPickUpLoot;
	}

	@Override
	public void setCanPickUpLoot(boolean canPickUpLoot)
	{
		this.canPickUpLoot = canPickUpLoot;
	}
}
