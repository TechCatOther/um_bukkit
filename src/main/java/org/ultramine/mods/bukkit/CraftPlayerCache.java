package org.ultramine.mods.bukkit;

import com.google.common.base.Function;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.ultramine.core.service.Service;

@Service
public interface CraftPlayerCache
{
	CraftPlayer getOrCreate(EntityPlayerMP player);

	void updateReferences(EntityPlayerMP player);

	void forEach(Function<CraftPlayer, Void> consumer);
}
