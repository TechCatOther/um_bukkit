package org.ultramine.mods.bukkit.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.ultramine.mods.bukkit.interfaces.network.ITimestampedPacket;

@Mixin(net.minecraft.network.play.client.C08PacketPlayerBlockPlacement.class)
public class MixinC08PacketPlayerBlockPlacement implements ITimestampedPacket
{
	public final long timestamp = System.currentTimeMillis();

	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
}
