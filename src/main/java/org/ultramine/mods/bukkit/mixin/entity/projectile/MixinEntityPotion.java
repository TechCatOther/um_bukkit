package org.ultramine.mods.bukkit.mixin.entity.projectile;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.projectile.IMixinEntityPotion;

@Mixin(net.minecraft.entity.projectile.EntityPotion.class)
public class MixinEntityPotion implements IMixinEntityPotion
{
	@Shadow
	public ItemStack potionDamage;

	@Override
	public ItemStack getPotionDamage()
	{
		return potionDamage;
	}

	@Override
	public void setPotionDamage(ItemStack potionDamage)
	{
		this.potionDamage = potionDamage;
	}
}
