package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockFarmland;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.Random;

@Mixin(BlockFarmland.class)
public class MixinBlockFarmland
{
	/**
	 * @author AtomicInteger
	 */
	@Inject(method = "updateTick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z", shift = Shift.BEFORE))
	public void updateTickInject(World world, int x, int y, int z, Random random, CallbackInfo ci)
	{
		org.bukkit.block.Block block = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z);
		if (CraftEventFactory.callBlockFadeEvent(block, Blocks.dirt).isCancelled())
			ci.cancel();
	}

	@Inject(method = "onFallenUpon", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z"))
	private void onFallenUponInject(World world, int x, int y, int z, Entity entity, float p_149746_6_, CallbackInfo ci)
	{
		if (entity != null)
		{
			Cancellable cancellable = null;
			if (entity instanceof EntityPlayer)
			{
				cancellable = CraftEventFactory.callPlayerInteractEvent((EntityPlayer) entity, org.bukkit.event.block.Action.PHYSICAL, x, y, z, -1, null);
			}
			else if (((IMixinWorld) world).getWorld() != null)
			{
				cancellable = new EntityInteractEvent(((IMixinEntity) entity).getBukkitEntity(), ((IMixinWorld) world).getWorld().getBlockAt(x, y, z));
				Bukkit.getPluginManager().callEvent((EntityInteractEvent) cancellable);
			}
			if (cancellable != null && cancellable.isCancelled())
				ci.cancel();
		}
	}
}
