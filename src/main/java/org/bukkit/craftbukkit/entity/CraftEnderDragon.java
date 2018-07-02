package org.bukkit.craftbukkit.entity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;

import java.util.Set;

public class CraftEnderDragon extends CraftComplexLivingEntity implements EnderDragon
{
	public CraftEnderDragon(CraftServer server, EntityDragon entity)
	{
		super(server, entity);
	}

	public Set<ComplexEntityPart> getParts()
	{
		Builder<ComplexEntityPart> builder = ImmutableSet.builder();

		for(EntityDragonPart part : getHandle().dragonPartArray)
		{
			builder.add((ComplexEntityPart) ((IMixinEntity) part).getBukkitEntity());
		}

		return builder.build();
	}

	@Override
	public EntityDragon getHandle()
	{
		return (EntityDragon) entity;
	}

	@Override
	public String toString()
	{
		return "CraftEnderDragon";
	}

	public EntityType getType()
	{
		return EntityType.ENDER_DRAGON;
	}
}
