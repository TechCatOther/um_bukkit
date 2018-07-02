package org.ultramine.mods.bukkit.mixin.stats;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatFileWriter;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatFileWriter.class)
public abstract class MixinStatFileWriter
{
	@Shadow public abstract int writeStat(StatBase p_77444_1_);

	@Inject(method = "func_150871_b", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatFileWriter;func_150873_a(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/stats/StatBase;I)V"))
	public void func_150871_bInject(EntityPlayer player, StatBase statBase, int current, CallbackInfo ci)
	{
		Cancellable cancellableEvent = CraftEventFactory.handleStatisticsIncrease(player, statBase, this.writeStat(statBase), current);
		if (cancellableEvent != null && cancellableEvent.isCancelled())
			ci.cancel();
	}
}
