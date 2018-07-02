package org.ultramine.mods.bukkit.mixin.management;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.management.IMixinUserListEntry;

@Mixin(value = net.minecraft.server.management.UserListEntry.class)
public abstract class MixinUserListEntry implements IMixinUserListEntry
{
	@Shadow
	private
	@Final
	Object field_152642_a;

	@Shadow
	abstract boolean hasBanExpired();

	@Override
	public boolean hasBanExpiredPub()
	{
		return hasBanExpired();
	}

	@Override
	public Object getKey()
	{
		return field_152642_a;
	}
}
