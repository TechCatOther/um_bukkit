package org.ultramine.mods.bukkit.mixin.management;

import net.minecraft.server.management.UserListEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.management.IMixinUserList;

import java.util.Collection;
import java.util.Map;

@Mixin(net.minecraft.server.management.UserList.class)
public class MixinUserList implements IMixinUserList
{
	@Shadow
	private
	@Final
	Map field_152696_d;

	public Collection<UserListEntry> getValues()
	{
		return this.field_152696_d.values();
	}
}
