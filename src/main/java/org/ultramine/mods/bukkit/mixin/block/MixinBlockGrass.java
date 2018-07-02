package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.Random;

@Mixin(BlockGrass.class)
public class MixinBlockGrass
{
	/**
	 * @author AtomicInteger
	 */
	@Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z", ordinal = 0))
	public boolean setBlockRedirect(World world, int x, int y, int z, Block block)
	{
		org.bukkit.World bworld = ((IMixinWorld) world).getWorld();
		BlockState blockState = bworld.getBlockAt(x, y, z).getState();
		blockState.setTypeId(Block.getIdFromBlock(Blocks.dirt));
		BlockFadeEvent event = new BlockFadeEvent(blockState.getBlock(), blockState);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled())
			blockState.update(true);
		return false;
	}

	@Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z", ordinal = 1))
	public boolean setBlockSpreadRedirect(World world, int x, int y, int z, Block block, World sourceWorld, int sourceX, int sourceY, int sourceZ, Random random)
	{
		org.bukkit.World bworld = ((IMixinWorld) world).getWorld();
		BlockState blockState = bworld.getBlockAt(x, y, z).getState();
		blockState.setTypeId(Block.getIdFromBlock(Blocks.grass));
		BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(sourceX, sourceY, sourceZ), blockState);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled())
			blockState.update(true);
		return false;
	}
}
