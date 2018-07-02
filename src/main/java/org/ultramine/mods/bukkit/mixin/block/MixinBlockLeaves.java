package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.event.block.LeavesDecayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(net.minecraft.block.BlockLeaves.class)
public abstract class MixinBlockLeaves
{
	@Inject(method = "removeLeaves", cancellable = true, at = @At("HEAD"))
	private void onRemoveLeaves(World p_150126_1_, int p_150126_2_, int p_150126_3_, int p_150126_4_, CallbackInfo ci)
	{
		LeavesDecayEvent event = new LeavesDecayEvent(((IMixinWorld) p_150126_1_).getWorld().getBlockAt(p_150126_2_, p_150126_3_, p_150126_4_));
		Bukkit.getServer().getPluginManager().callEvent(event);

		if(event.isCancelled())
			ci.cancel();
	}
}
