package org.ultramine.mods.bukkit.mixin.world;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(net.minecraft.world.WorldServer.class)
public abstract class MixinWorldServer extends World
{
	public MixinWorldServer(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_, WorldSettings p_i45368_4_, Profiler p_i45368_5_)
	{super(p_i45368_1_, p_i45368_2_, p_i45368_3_, p_i45368_4_, p_i45368_5_);}

	public boolean spawnEntityInWorld(Entity entity)
	{
		return ((IMixinWorld)this).addEntity(entity, CreatureSpawnEvent.SpawnReason.DEFAULT);
	}

	public boolean real_spawnEntityInWorld(Entity entity)
	{
		return super.spawnEntityInWorld(entity);
	}

	@Redirect(method = "func_147456_g", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;setBlock(IIILnet/minecraft/block/Block;)Z", ordinal = 0))
	public boolean setBlockRedirectIceForm(WorldServer world, int x, int y, int z, Block block)
	{
		BlockState blockState = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z).getState();
		blockState.setTypeId(Block.getIdFromBlock(Blocks.ice));
		BlockFormEvent iceBlockForm = new BlockFormEvent(blockState.getBlock(), blockState);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(iceBlockForm);
		if (!iceBlockForm.isCancelled())
			blockState.update(true);
		return false;
	}

	@Redirect(method = "func_147456_g", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;setBlock(IIILnet/minecraft/block/Block;)Z", ordinal = 1))
	public boolean setBlockRedirectSnowForm(WorldServer world, int x, int y, int z, Block block)
	{
		BlockState blockState = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z).getState();
		blockState.setTypeId(Block.getIdFromBlock(Blocks.snow_layer));
		BlockFormEvent snow = new BlockFormEvent(blockState.getBlock(), blockState);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(snow);
		if (!snow.isCancelled())
			blockState.update(true);
		return false;
	}
}
