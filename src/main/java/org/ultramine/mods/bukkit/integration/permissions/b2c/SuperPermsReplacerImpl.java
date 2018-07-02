package org.ultramine.mods.bukkit.integration.permissions.b2c;

import com.google.common.base.Function;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.permissions.PermissibleBase;
import org.ultramine.core.service.EventBusRegisteredService;
import org.ultramine.core.service.InjectService;
import org.ultramine.mods.bukkit.CraftPlayerCache;
import org.ultramine.mods.bukkit.api.CraftPlayerCreationForgeEvent;

import javax.annotation.Nullable;

public class SuperPermsReplacerImpl extends EventBusRegisteredService implements SuperPermsReplacer
{
	@InjectService
	private static CraftPlayerCache craftPlayerCache;
	private boolean enabled;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCraftPlayerCreate(CraftPlayerCreationForgeEvent e)
	{
		replace(e.getPlayer());
	}

	private void replace(CraftPlayer player)
	{
		if(enabled)
			player.setPermissible(new UmPermissible(player));
	}

	@Override
	public void setEnabled(boolean newEnabled)
	{
		if(this.enabled != newEnabled)
		{
			this.enabled = newEnabled;
			craftPlayerCache.forEach(new Function<CraftPlayer, Void>()
			{
				@Nullable
				@Override
				public Void apply(CraftPlayer input)
				{
					if(enabled)
						input.setPermissible(new UmPermissible(input));
					else if(input.getPermissible().getClass() == UmPermissible.class)
						input.setPermissible(new PermissibleBase(input));

					return null;
				}
			});
		}
	}
}
