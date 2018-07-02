package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.Random;

@Mixin(BlockSnow.class)
public class MixinBlockSnow extends Block
{
	protected MixinBlockSnow(Material p_i45394_1_)
	{
		super(p_i45394_1_);
	}

	/**
	 * @author AtomicInteger
	 */
	@Inject(method = "updateTick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir(III)Z"))
	public void updateTickInject(World world, int x, int y, int z, Random random, CallbackInfo ci)
	{
		if (org.bukkit.craftbukkit.event.CraftEventFactory.callBlockFadeEvent(((IMixinWorld) world).getWorld().getBlockAt(x, y, z), Blocks.air).isCancelled())
			ci.cancel();
		else
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
	}
}
