package org.ultramine.mods.bukkit.mixin.entity.passive;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityPig;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(net.minecraft.entity.passive.EntityPig.class)
public class MixinEntityPig
{
	@Inject(method = "onStruckByLightning", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
	public void onStruckByLightningInject(EntityLightningBolt p_70077_1_, CallbackInfo ci, EntityPigZombie entitypigzombie)
	{
		if (CraftEventFactory.callPigZapEvent((EntityPig)(Object)this, p_70077_1_, entitypigzombie).isCancelled())
			ci.cancel();
	}
}
