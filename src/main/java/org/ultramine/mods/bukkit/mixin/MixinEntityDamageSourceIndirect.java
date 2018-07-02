package org.ultramine.mods.bukkit.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.ultramine.mods.bukkit.interfaces.IMixinEntityDamageSourceIndirect;

@Mixin(net.minecraft.util.EntityDamageSourceIndirect.class)
public abstract class MixinEntityDamageSourceIndirect extends EntityDamageSource implements IMixinEntityDamageSourceIndirect
{
	public MixinEntityDamageSourceIndirect(String s, Entity e)
	{
		super(s, e);
	}

	@Override
	public Entity getProximateDamageSource()
	{
		return super.getEntity();
	}
}
