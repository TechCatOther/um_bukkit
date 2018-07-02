package org.ultramine.mods.bukkit.mixin.fml.network.handshake;

import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.fml.network.handshake.IMixinNetworkDispatcher;

@Mixin(cpw.mods.fml.common.network.handshake.ChannelRegistrationHandler.class)
public class MixinChannelRegistrationHandler
{
	@Redirect(method = "channelRead0", remap = false,
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableSet;copyOf([Ljava/lang/Object;)Lcom/google/common/collect/ImmutableSet;", ordinal = 2))
	private ImmutableSet<?> onChannelRead0(Object[] splitObjs, ChannelHandlerContext ctx, FMLProxyPacket msg)
	{
		String[] split = (String[])splitObjs;
		// Cauldron start - register bukkit channels for players
		NetworkDispatcher dispatcher = ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
		CraftPlayer player = (CraftPlayer)((IMixinEntity)((IMixinNetworkDispatcher) dispatcher).getPlayer()).getBukkitEntity();
		if (msg.channel().equals("REGISTER"))
		{
			for (String channel : split)
			{
				player.addChannel(channel);
			}
		}
		else
		{
			for (String channel : split)
			{
				player.removeChannel(channel);
			}
		}
		// Cauldron end
		return ImmutableSet.copyOf(splitObjs);
	}
}
