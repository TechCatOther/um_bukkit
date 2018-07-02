package org.ultramine.mods.bukkit.mixin.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLivingBase;

@Mixin(net.minecraft.entity.projectile.EntityWitherSkull.class)
public class MixinEntityWitherSkull
{
	@Redirect(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;heal(F)V"))
	private void healRedir(EntityLivingBase entity, float value)
	{
		((IMixinEntityLivingBase) entity).heal(value, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.WITHER);
	}
}
