package org.ultramine.mods.bukkit.mixin;

import net.minecraft.world.WorldProvider;
import org.bukkit.World.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ultramine.mods.bukkit.util.BukkitEnumHelper;

@Mixin(value = net.minecraftforge.common.DimensionManager.class, remap = false)
public abstract class MixinDimensionManager
{
	@Inject(method = "registerProviderType", at = @At("HEAD"))
	private static void onRegisterProviderType(int id, Class<? extends WorldProvider> provider, boolean keepLoaded, CallbackInfoReturnable<?> ci)
	{
		String worldType = provider.getSimpleName().toLowerCase();
		worldType = worldType.replace("worldprovider", "");
		worldType = worldType.replace("provider", "");
		registerBukkitEnvironment(id, worldType);
	}

	private static Environment registerBukkitEnvironment(int dim, String providerName)
	{
		@SuppressWarnings("deprecation")
		Environment env = Environment.getEnvironment(dim);
		if(env == null) // Cauldron  if environment not found, register one
		{
			providerName = providerName.replace("WorldProvider", "");
			env = BukkitEnumHelper.addBukkitEnvironment(dim, providerName.toUpperCase());
			Environment.registerEnvironment(env);
		}
		return env;
	}
}
