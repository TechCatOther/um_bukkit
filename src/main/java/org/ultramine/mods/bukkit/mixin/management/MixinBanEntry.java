package org.ultramine.mods.bukkit.mixin.management;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.management.IMixinBanEntry;

import java.util.Date;

@Mixin(net.minecraft.server.management.BanEntry.class)
public class MixinBanEntry implements IMixinBanEntry
{
	@Shadow
	protected
	@Final
	Date banStartDate;
	@Shadow
	protected
	@Final
	String bannedBy;

	@Override
	public Date getCreated()
	{
		return banStartDate;
	}

	@Override
	public String getSource()
	{
		return bannedBy;
	}
}
