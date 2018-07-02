package org.ultramine.mods.bukkit.interfaces;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;

public interface IMixinPotion
{
	void applyInstantEffect(EntityLivingBase p_76402_1_, EntityLivingBase p_76402_2_, int p_76402_3_, double p_76402_4_, EntityPotion potion);
}
