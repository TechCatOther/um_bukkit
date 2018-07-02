package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.world.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockSpreadEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.Random;

@Mixin(BlockMushroom.class)
public abstract class MixinBlockMushroom
{
	@Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;II)Z"))
	public boolean setBlockRedirect(World world, int x, int y, int z, Block block, int meta, int flags, World sourceWorld, int sourceX, int sourceY, int sourceZ, Random random)
	{
		org.bukkit.World bworld = ((IMixinWorld) world).getWorld();
		BlockState blockState = bworld.getBlockAt(x, y, z).getState();
		blockState.setTypeId(Block.getIdFromBlock((BlockMushroom)(Object) this)); // nms: this.id, 0, 2
		BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(sourceX, sourceY, sourceZ), blockState);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled())
			blockState.update(true);
		return false;
	}
}
