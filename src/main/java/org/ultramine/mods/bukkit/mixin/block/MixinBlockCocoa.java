package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockCocoa.class)
public class MixinBlockCocoa extends BlockDirectional
{
	protected MixinBlockCocoa(Material p_i45401_1_)
	{
		super(p_i45401_1_);
	}

	@Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockMetadataWithNotify(IIIII)Z"))
	public boolean setBlockMetadataWithNotifyRedirect(World world, int x, int y, int z, int data, int p_72921_5_)
	{
		CraftEventFactory.handleBlockGrowEvent(world, x, y, z, this, data);
		return false;
	}
}
