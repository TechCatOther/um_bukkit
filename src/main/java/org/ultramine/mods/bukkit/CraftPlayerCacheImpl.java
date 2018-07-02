package org.ultramine.mods.bukkit;

import com.google.common.base.Function;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.ultramine.mods.bukkit.api.CraftPlayerCreationForgeEvent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CraftPlayerCacheImpl implements CraftPlayerCache
{
	private final Map<UUID, WeakReference<CraftPlayer>> cache = new HashMap<UUID, WeakReference<CraftPlayer>>();
	private final CraftServer server;

	public CraftPlayerCacheImpl(CraftServer server)
	{
		this.server = server;
	}

	@Override
	public CraftPlayer getOrCreate(EntityPlayerMP player)
	{
		UUID uuid = player.getGameProfile().getId();
		WeakReference<CraftPlayer> ref = cache.get(uuid);
		CraftPlayer craft = ref == null ? null : ref.get();
		if(craft == null)
		{
			craft = new CraftPlayer(server, player);
			MinecraftForge.EVENT_BUS.post(new CraftPlayerCreationForgeEvent(craft));
			cache.put(uuid, new WeakReference<CraftPlayer>(craft));
		}
		else
		{
			craft.setHandle(player);
		}
		return craft;
	}

	@Override
	public void updateReferences(EntityPlayerMP player)
	{
		WeakReference<CraftPlayer> ref = cache.get(player.getGameProfile().getId());
		CraftPlayer craft = ref == null ? null : ref.get();
		if(craft != null)
			craft.setHandle(player);
	}

	@Override
	public void forEach(Function<CraftPlayer, Void> consumer)
	{
		for(WeakReference<CraftPlayer> ref : cache.values())
		{
			CraftPlayer craft = ref.get();
			if(craft != null)
				consumer.apply(craft);
		}
	}
}
