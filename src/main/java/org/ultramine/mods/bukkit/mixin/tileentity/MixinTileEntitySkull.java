package org.ultramine.mods.bukkit.mixin.tileentity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.tileentity.IMixinTileEntitySkull;

@Mixin(net.minecraft.tileentity.TileEntitySkull.class)
public class MixinTileEntitySkull implements IMixinTileEntitySkull
{
	@Shadow
	private int field_145910_i;

	@Override
	public int getRotation()
	{
		return field_145910_i;
	}
}
