package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockButton;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.List;

@Mixin(BlockButton.class)
public class MixinBlockButton
{
	@Inject(method = "func_150046_n", cancellable = true, at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 0, shift = Shift.BY, by = 11), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void func_150046_nInject(World world, int x, int y, int z, CallbackInfo ci, int l, int i1, boolean flag, List list, boolean flag1)
	{
		if (flag != flag1 && flag1)
		{
			Block block = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z);
			boolean allowed = false;
			for (Object entityObject : list)
				if (entityObject != null)
				{
					EntityInteractEvent event = new EntityInteractEvent(((IMixinEntity) entityObject).getBukkitEntity(), block);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled())
					{
						allowed = true;
						break;
					}
				}
			if (!allowed)
				ci.cancel();
		}
	}
}
