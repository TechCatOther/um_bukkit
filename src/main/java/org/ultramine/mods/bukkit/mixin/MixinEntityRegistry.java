package org.ultramine.mods.bukkit.mixin;

import com.google.common.collect.Maps;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.IMixinEntityRegistry;

import java.util.Map;

@Mixin(value = cpw.mods.fml.common.registry.EntityRegistry.class, remap = false)
public abstract class MixinEntityRegistry implements IMixinEntityRegistry
{
	private static Map<Class<? extends Entity>, String> entityTypeMap = Maps.newHashMap(); // Cauldron - used by CraftCustomEntity
	private static Map<String, Class<? extends Entity>> entityClassMap = Maps.newHashMap(); // Cauldron - used by CraftWorld

	@Inject(method = "registerGlobalEntityID*", at = @At("RETURN"))
	private static void onRegisterGlobalEntityID(Class<? extends Entity> entityClass, String entityName, int id,
												 int backgroundEggColour, int foregroundEggColour, CallbackInfo ci)
	{
		registerBukkitType(entityClass, entityName);
	}

	@Inject(method = "registerModEntity", at = @At("RETURN"))
	private static void onRegisterModEntity(Class<? extends Entity> entityClass, String entityName, int id,
											Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, CallbackInfo ci)
	{
		registerBukkitType(entityClass, entityName);
	}

	private static void registerBukkitType(Class<? extends Entity> entityClass, String entityName)
	{
		ModContainer activeModContainer = Loader.instance().activeModContainer();
		String modId = "unknown";
		// fixup bad entity names from mods
		if(entityName.contains("."))
		{
			if((entityName.indexOf(".") + 1) < entityName.length())
				entityName = entityName.substring(entityName.indexOf(".") + 1, entityName.length());
		}
		entityName = entityName.replace("entity", "");
		if(entityName.startsWith("ent"))
			entityName = entityName.replace("ent", "");
		entityName = entityName.replaceAll("[^A-Za-z0-9]", ""); // remove all non-digits/alphanumeric
		if(activeModContainer != null)
			modId = activeModContainer.getModId();
		entityName = modId + "-" + entityName;
		entityTypeMap.put(entityClass, entityName);
		entityClassMap.put(entityName, entityClass);
	}

	// used by CraftCustomEntity
	@Override
	public String getCustomEntityTypeName(Class<? extends Entity> entityClass)
	{
		return entityTypeMap.get(entityClass);
	}

	@Override
	public Map<Class<? extends Entity>, String> getEntityTypeMap()
	{
		return entityTypeMap;
	}

	@Override
	public Map<String, Class<? extends Entity>> getEntityClassMap()
	{
		return entityClassMap;
	}
}
