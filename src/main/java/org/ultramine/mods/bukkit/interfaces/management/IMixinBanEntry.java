package org.ultramine.mods.bukkit.interfaces.management;

import java.util.Date;

public interface IMixinBanEntry
{
	Date getCreated();

	String getSource();
}
