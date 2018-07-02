package org.ultramine.mods.bukkit.interfaces.entity.boss;

import net.minecraft.util.DamageSource;

public interface IMixinEntityDragon
{
	boolean realAttackEntityFrom(DamageSource source, float amount);
}
