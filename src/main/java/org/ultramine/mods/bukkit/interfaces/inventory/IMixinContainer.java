package org.ultramine.mods.bukkit.interfaces.inventory;

import net.minecraft.inventory.Container;
import org.bukkit.inventory.InventoryView;

import javax.annotation.Nullable;

public interface IMixinContainer
{
	@Nullable InventoryView getBukkitView();

	void setBukkitView(InventoryView bukkitView);

	void transferTo(Container other, org.bukkit.craftbukkit.entity.CraftHumanEntity player);

	void setOpened(boolean isOpened);

	boolean isOpened();

	void setClosedByEventCancelling(boolean closedByEventCancelling);

	boolean isClosedByEventCancelling();
}
