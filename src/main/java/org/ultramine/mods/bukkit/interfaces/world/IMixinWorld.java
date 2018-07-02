package org.ultramine.mods.bukkit.interfaces.world;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public interface IMixinWorld
{
	CraftWorld getWorld();

	CraftServer getServer();

	boolean addEntity(Entity entity, SpawnReason spawnReason);

	// this method is used by ForgeMultipart and Immibis's Microblocks
	boolean canPlaceMultipart(Block block, int x, int y, int z);

	boolean setRawTypeId(int x, int y, int z, int typeId);

	boolean setRawTypeIdAndData(int x, int y, int z, int typeId, int data);

	boolean setTypeId(int x, int y, int z, int typeId);

	boolean setTypeIdAndData(int x, int y, int z, int typeId, int data);

	int getTypeId(int x, int y, int z);

	boolean setTypeAndData(int x, int y, int z, Block block, int data, int flag);

	boolean setData(int x, int y, int z, int data, int flag);

	int getData(int x, int y, int z);

	Block getType(int x, int y, int z);
}
