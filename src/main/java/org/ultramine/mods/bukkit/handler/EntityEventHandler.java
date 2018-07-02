package org.ultramine.mods.bukkit.handler;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.server.event.EntitySetFireEvent;
import org.ultramine.server.event.WorldEventProxy;
import org.ultramine.server.event.WorldUpdateObject;
import org.ultramine.server.event.WorldUpdateObjectType;

import java.util.ArrayList;
import java.util.List;

public class EntityEventHandler
{
	private final CraftServer server;

	public EntityEventHandler(CraftServer server)
	{
		this.server = server;
	}

//	@SubscribeEvent(priority = EventPriority.HIGHEST)
//	public void onEntitySetFire(EntitySetFireEvent e)
//	{
//		WorldUpdateObject wuo = WorldEventProxy.getCurrent().getUpdateObject();
//		if(wuo.getType() == WorldUpdateObjectType.ENTITY && wuo.getEntity() == e.entity && e.entity instanceof EntityLivingBase && ((IMixinEntity) e.entity).getFireTicks() <= 0)
//		{
//			EntityCombustEvent bevent = new EntityCombustByBlockEvent(null, ((IMixinEntity) e.entity).getBukkitEntity(), e.fireTicks / 20); //TODO null?
//			server.getPluginManager().callEvent(bevent);
//			if(bevent.isCancelled())
//				e.setCanceled(true);
//			else
//				e.fireTicks = bevent.getDuration() * 20;
//		}
//	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDrops(LivingDropsEvent e)
	{
		if(e.drops.size() > 0)
		{
			List<org.bukkit.inventory.ItemStack> loot = new ArrayList<org.bukkit.inventory.ItemStack>();
			for(EntityItem item : e.drops)
				loot.add(CraftItemStack.asCraftMirror(item.getEntityItem()));
			CraftEventFactory.callEntityDeathEvent(e.entityLiving, loot);
		}
		else
		{
			CraftEventFactory.callEntityDeathEvent(e.entityLiving);
		}
	}
}
