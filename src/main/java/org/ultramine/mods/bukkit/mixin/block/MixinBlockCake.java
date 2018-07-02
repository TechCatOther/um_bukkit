package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.IMixinFoodStats;

@Mixin(net.minecraft.block.BlockCake.class)
public class MixinBlockCake
{
	@Redirect(method = "func_150036_b", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/FoodStats;addStats(IF)V"))
	private void addStatsRedir(FoodStats stats, int val, float f, World p_150036_1_, int p_150036_2_, int p_150036_3_, int p_150036_4_, EntityPlayer p_150036_5_)
	{
		IMixinFoodStats mstats = (IMixinFoodStats) stats;
		int oldFoodLevel = mstats.getFoodLevel();
		org.bukkit.event.entity.FoodLevelChangeEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callFoodLevelChangeEvent(p_150036_5_, val + oldFoodLevel);

		if(!event.isCancelled())
		{
			p_150036_5_.getFoodStats().addStats(event.getFoodLevel() - oldFoodLevel, f);
		}

		mstats.sendUpdatePacket((EntityPlayerMP) p_150036_5_);
	}
}
