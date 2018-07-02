package org.ultramine.mods.bukkit.mixin.management;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.network.NetHandlerLoginServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.core.service.InjectService;
import org.ultramine.mods.bukkit.CraftPlayerCache;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.management.IMixinServerConfigurationManager;
import org.ultramine.mods.bukkit.interfaces.management.IMixinUserListEntry;

import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;

@Mixin(value = net.minecraft.server.management.ServerConfigurationManager.class)
public abstract class MixinServerConfigurationManager implements IMixinServerConfigurationManager
{
	@InjectService
	private static CraftPlayerCache cPlayerCache;
	@Shadow
	private static SimpleDateFormat dateFormat;
	@Shadow
	private MinecraftServer mcServer;
	@Shadow
	public List<EntityPlayerMP> playerEntityList;
	@Shadow
	private UserListBans bannedPlayers;
	@Shadow
	private BanList bannedIPs;
	@Shadow
	protected int maxPlayers;

	@Shadow
	protected abstract void func_96456_a(ServerScoreboard p_96456_1_, EntityPlayerMP p_96456_2_);

	@Shadow
	public abstract boolean func_152607_e(GameProfile p_152607_1_);

	// CraftBukkit start - Whole method, SocketAddress to LoginListener, added hostname to signature, return EntityPlayer
	@Override
	public EntityPlayerMP attemptLogin(NetHandlerLoginServer loginlistener, GameProfile gameprofile, String hostname, EntityPlayerMP entity)
	{
		// Instead of kicking then returning, we need to store the kick reason
		// in the event, check with plugins to see if it's ok, and THEN kick
		// depending on the outcome.
		SocketAddress socketaddress = loginlistener.field_147333_a.getSocketAddress();
		cPlayerCache.updateReferences(entity);
		Player player = (Player) ((IMixinEntity) entity).getBukkitEntity();
		PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((java.net.InetSocketAddress) socketaddress).getAddress(),
				((java.net.InetSocketAddress) loginlistener.field_147333_a.channel().remoteAddress()).getAddress()); // Spigot
		String s;

		if(this.bannedPlayers.func_152702_a(gameprofile) && !((IMixinUserListEntry) this.bannedPlayers.func_152683_b(gameprofile)).hasBanExpiredPub())
		{
			UserListBansEntry banentry = (UserListBansEntry) this.bannedPlayers.func_152683_b(gameprofile);
			s = "You are banned from this server!\nReason: " + banentry.getBanReason();

			if(banentry.getBanEndDate() != null)
			{
				s = s + "\nYour ban will be removed on " + dateFormat.format(banentry.getBanEndDate());
			}

			// return s;
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, s);
		}
		else if(!this.func_152607_e(gameprofile))
		{
			// return "You are not white-listed on this server!";
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "You are not white-listed on this server!");
		}
		else if(this.bannedIPs.func_152708_a(socketaddress) && !((IMixinUserListEntry) this.bannedPlayers.func_152683_b(gameprofile)).hasBanExpiredPub())
		{
			IPBanEntry ipbanentry = this.bannedIPs.func_152709_b(socketaddress);
			s = "Your IP address is banned from this server!\nReason: " + ipbanentry.getBanReason();

			if(ipbanentry.getBanEndDate() != null)
			{
				s = s + "\nYour ban will be removed on " + dateFormat.format(ipbanentry.getBanEndDate());
			}
			// return s;
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, s);
		}
		else
		{
			// return this.players.size() >= this.maxPlayers ? "The server is full!" : null;
			if(this.playerEntityList.size() >= this.maxPlayers)
			{
				event.disallow(PlayerLoginEvent.Result.KICK_FULL, "The server is full!");
			}
		}

		Bukkit.getServer().getPluginManager().callEvent(event);

		if(event.getResult() != PlayerLoginEvent.Result.ALLOWED)
		{
			loginlistener.func_147322_a(event.getKickMessage());
			return null;
		}

		return entity;
	}
	// CraftBukkit end

	public void sendScoreboard(ServerScoreboard sb, EntityPlayerMP player)
	{
		func_96456_a(sb, player);
	}
}
