package org.ultramine.mods.bukkit.mixin.network;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.management.IMixinServerConfigurationManager;

@Mixin(net.minecraft.server.network.NetHandlerLoginServer.class)
public abstract class MixinNetHLoginS
{
	private static final Object LoginState_ACCEPTED;

	static
	{
		try
		{
			LoginState_ACCEPTED = Enum.valueOf((Class<Enum>) Class.forName("net.minecraft.server.network.NetHandlerLoginServer$LoginState"), "ACCEPTED");
		} catch(ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Shadow
	private MinecraftServer field_147327_f;
	@Shadow
	public NetworkManager field_147333_a;
	@Shadow
	private GameProfile field_147337_i;

	@Shadow
	protected abstract GameProfile func_152506_a(GameProfile p_152506_1_);

	@Shadow
	public abstract void func_147322_a(String p_147322_1_);

	private void setConnState(Object val)
	{
		ReflectionHelper.setPrivateValue(NetHandlerLoginServer.class, (NetHandlerLoginServer) (Object) this, val, "field_147328_g");
	}

	@Overwrite
	public void func_147326_c()
	{
		if(!this.field_147337_i.isComplete())
		{
			this.field_147337_i = this.func_152506_a(this.field_147337_i);
		}

		EntityPlayerMP player = this.field_147327_f.getConfigurationManager().createPlayerForUser(this.field_147337_i);
		Object s = ((IMixinServerConfigurationManager) field_147327_f.getConfigurationManager())
				.attemptLogin((NetHandlerLoginServer) (Object) this, this.field_147337_i, "", player); //TODO add server hostname

		if(s == null)
		{
//			this.func_147322_a(s);
		}
		else
		{
			setConnState(LoginState_ACCEPTED);
			this.field_147333_a.scheduleOutboundPacket(new S02PacketLoginSuccess(this.field_147337_i));
			FMLNetworkHandler.fmlServerHandshake(this.field_147327_f.getConfigurationManager(), this.field_147333_a, player);
		}
	}
}
