package org.ultramine.mods.bukkit.mixin.tileentity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.tileentity.IMixinTileEntityBrewingStand;

@Mixin(net.minecraft.tileentity.TileEntityBrewingStand.class)
public class MixinTileEntityBrewingStand implements IMixinTileEntityBrewingStand
{
	@Shadow
	private int brewTime;

	@Override
	public void setBrewTime(int brewTime)
	{
		this.brewTime = brewTime;
	}
}
