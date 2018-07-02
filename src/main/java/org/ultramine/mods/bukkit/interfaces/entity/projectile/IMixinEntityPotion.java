package org.ultramine.mods.bukkit.interfaces.entity.projectile;

import net.minecraft.item.ItemStack;

public interface IMixinEntityPotion
{
	ItemStack getPotionDamage();

	void setPotionDamage(ItemStack potionDamage);
}
