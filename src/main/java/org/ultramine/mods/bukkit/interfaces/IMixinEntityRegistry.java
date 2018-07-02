package org.ultramine.mods.bukkit.interfaces;

import net.minecraft.entity.Entity;

import java.util.Map;

public interface IMixinEntityRegistry
{
	String getCustomEntityTypeName(Class<? extends Entity> entityClass);

	Map<Class<? extends Entity>, String> getEntityTypeMap();

	Map<String, Class<? extends Entity>> getEntityClassMap();
}
