package org.ultramine.mods.bukkit.mixin.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntitySmallFireball.class)
public class MixinEntitySmallFireball
{
	@Redirect(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z"))
	public boolean setBlockRedirect(World world, int x, int y, int z, Block block)
	{
		if (!org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(world, x, y, z, (EntitySmallFireball) (Object) this).isCancelled())
			world.setBlock(x, y, z, Blocks.fire);
		return false;
	}
}
