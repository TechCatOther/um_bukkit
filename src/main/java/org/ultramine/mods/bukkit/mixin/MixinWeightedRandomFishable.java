package org.ultramine.mods.bukkit.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.IMixinWeightedRandomFishable;

@Mixin(net.minecraft.util.WeightedRandomFishable.class)
public class MixinWeightedRandomFishable implements IMixinWeightedRandomFishable
{
	@Shadow
	private
	@Final
	ItemStack field_150711_b;
	@Shadow
	private float field_150712_c;
	@Shadow
	private boolean field_150710_d;

	@Override
	public ItemStack getField_150711_b()
	{
		return field_150711_b;
	}

	@Override
	public float getField_150712_c()
	{
		return field_150712_c;
	}

	@Override
	public boolean getField_150710_d()
	{
		return field_150710_d;
	}
}
