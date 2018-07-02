package org.ultramine.mods.bukkit.mixin.network.play.client;

import net.minecraft.network.play.client.C0DPacketCloseWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.network.play.client.IMixinC0DPacketCloseWindow;

@Mixin(C0DPacketCloseWindow.class)
public class MixinC0DPacketCloseWindow implements IMixinC0DPacketCloseWindow
{
	@Shadow private int field_149556_a;

	@Override
	public void setWindowId(int windowId)
	{
		this.field_149556_a = windowId;
	}
}
