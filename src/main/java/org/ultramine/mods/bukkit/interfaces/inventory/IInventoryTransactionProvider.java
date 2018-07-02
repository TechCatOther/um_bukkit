package org.ultramine.mods.bukkit.interfaces.inventory;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

import java.util.List;

public interface IInventoryTransactionProvider
{
	void onOpen(CraftHumanEntity who);

	void onClose(CraftHumanEntity who);

	List<HumanEntity> getViewers();
}
