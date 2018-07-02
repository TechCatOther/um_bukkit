package org.ultramine.mods.bukkit.interfaces.fml.network.handshake;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IMixinNetworkDispatcher
{
	EntityPlayerMP getPlayer();
}
