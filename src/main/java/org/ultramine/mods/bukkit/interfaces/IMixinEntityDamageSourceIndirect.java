package org.ultramine.mods.bukkit.interfaces;

import net.minecraft.entity.Entity;

public interface IMixinEntityDamageSourceIndirect
{
	Entity getProximateDamageSource();
}
