package org.ultramine.mods.bukkit.mixin.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.entity.item.EntityItemFrame.class)
public abstract class MixinEntityItemFrame extends Entity
{
	public MixinEntityItemFrame(World w)
	{
		super(w);
	}

	@Inject(method = "attackEntityFrom", cancellable = true,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItemFrame;func_146065_b(Lnet/minecraft/entity/Entity;Z)V"))
	public void onAttackEntityFrom(DamageSource p_70097_1_, float p_70097_2_, CallbackInfoReturnable<Boolean> ci)
	{
		if(org.bukkit.craftbukkit.event.CraftEventFactory.handleNonLivingEntityDamageEvent(this, p_70097_1_, p_70097_2_) || this.isDead)
			ci.setReturnValue(true);
	}
}
