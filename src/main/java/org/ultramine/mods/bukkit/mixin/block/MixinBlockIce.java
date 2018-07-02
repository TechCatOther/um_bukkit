package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockIce;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.Random;

@Mixin(BlockIce.class)
public class MixinBlockIce
{
	/**
	 * @author AtomicInteger
	 */
	@Inject(method = "updateTick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I", shift = Shift.BY, by = 6))
	public void updateTickInject(World world, int x, int y, int z, Random random, CallbackInfo ci)
	{
		if (CraftEventFactory.callBlockFadeEvent(((IMixinWorld) world).getWorld().getBlockAt(x, y, z), Blocks.water).isCancelled())
			ci.cancel();
	}
}
