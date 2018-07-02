package org.ultramine.mods.bukkit.mixin.fml.network.handshake;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.fml.network.handshake.IMixinNetworkDispatcher;

@Mixin(cpw.mods.fml.common.network.handshake.NetworkDispatcher.class)
public class MixinNetworkDispatcher implements IMixinNetworkDispatcher
{
	@Shadow private EntityPlayerMP player;

	@Override
	public EntityPlayerMP getPlayer()
	{
		return player;
	}

	@Inject(method = "handleServerSideCustomPacket", at = @At("HEAD"), remap = false)
	private void onHandleServerSideCustomPacket(C17PacketCustomPayload msg, ChannelHandlerContext context, CallbackInfoReturnable<Boolean> ci)
	{
		((CraftPlayer) ((IMixinEntity) player).getBukkitEntity()).addChannel(msg.func_149559_c()); // Cauldron - register channel for bukkit player
	}
}
