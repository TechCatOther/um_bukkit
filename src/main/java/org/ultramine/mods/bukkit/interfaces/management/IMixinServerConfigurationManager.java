package org.ultramine.mods.bukkit.interfaces.management;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.network.NetHandlerLoginServer;

public interface IMixinServerConfigurationManager
{
	EntityPlayerMP attemptLogin(NetHandlerLoginServer loginlistener, GameProfile gameprofile, String hostname, EntityPlayerMP entity);

	void sendScoreboard(ServerScoreboard sb, EntityPlayerMP player);
}
