package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockCrops.class)
public class MixinBlockCrops extends BlockBush
{
	@Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockMetadataWithNotify(IIIII)Z"))
	public boolean setBlockMetadataWithNotifyRedirect(World world, int x, int y, int z, int data, int p_149674_5_)
	{
		CraftEventFactory.handleBlockGrowEvent(world, x, y, z, this, data);
		return false;
	}
}
