package org.bukkit.craftbukkit.util;

import org.bukkit.BlockChangeDelegate;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.material.MaterialData;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.ArrayList;
import java.util.List;

public class StructureGrowDelegate implements BlockChangeDelegate
{
	private final CraftWorld world;
	private final List<BlockState> blocks = new ArrayList<BlockState>();

	public StructureGrowDelegate(net.minecraft.world.World world)
	{
		this.world = ((IMixinWorld) world).getWorld();
	}

	public boolean setRawTypeId(int x, int y, int z, int type)
	{
		return setRawTypeIdAndData(x, y, z, type, 0);
	}

	public boolean setRawTypeIdAndData(int x, int y, int z, int type, int data)
	{
		BlockState state = world.getBlockAt(x, y, z).getState();
		state.setTypeId(type);
		state.setData(new MaterialData(type, (byte) data));
		blocks.add(state);
		return true;
	}

	public boolean setTypeId(int x, int y, int z, int typeId)
	{
		return setRawTypeId(x, y, z, typeId);
	}

	public boolean setTypeIdAndData(int x, int y, int z, int typeId, int data)
	{
		return setRawTypeIdAndData(x, y, z, typeId, data);
	}

	public int getTypeId(int x, int y, int z)
	{
		for(BlockState state : blocks)
		{
			if(state.getX() == x && state.getY() == y && state.getZ() == z)
			{
				return state.getTypeId();
			}
		}

		return world.getBlockTypeIdAt(x, y, z);
	}

	public int getHeight()
	{
		return world.getMaxHeight();
	}

	public List<BlockState> getBlocks()
	{
		return blocks;
	}

	public boolean isEmpty(int x, int y, int z)
	{
		for(BlockState state : blocks)
		{
			if(state.getX() == x && state.getY() == y && state.getZ() == z)
			{
				return net.minecraft.block.Block.getBlockById(state.getTypeId()) == net.minecraft.init.Blocks.air;
			}
		}

		return world.getBlockAt(x, y, z).isEmpty();
	}
}
