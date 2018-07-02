package org.ultramine.mods.bukkit.mixin.item;

import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemHangingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(ItemHangingEntity.class)
public class MixinItemHangingEntity
{
	@Inject(method = "onItemUse", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z", shift = Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int notch, float p_77648_8_, float p_77648_9_, float p_77648_10_, CallbackInfoReturnable<Boolean> cir, int i1, EntityHanging entityHanging)
	{
		Player who = (Player) ((IMixinEntity) player).getBukkitEntity();
		Block blockClicked = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z);
		BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(notch);
		HangingPlaceEvent event = new HangingPlaceEvent((org.bukkit.entity.Hanging) ((IMixinEntity) entityHanging).getBukkitEntity(), who, blockClicked, blockFace);
		Bukkit.getPluginManager().callEvent(event);
		PaintingPlaceEvent paintingEvent = null;
		if (entityHanging instanceof EntityPainting)
		{
			// Fire old painting event until it can be removed
			paintingEvent = new PaintingPlaceEvent((org.bukkit.entity.Painting) ((IMixinEntity) entityHanging).getBukkitEntity(), who, blockClicked, blockFace);
			paintingEvent.setCancelled(event.isCancelled());
			Bukkit.getPluginManager().callEvent(paintingEvent);
		}
		if (event.isCancelled() || (paintingEvent != null && paintingEvent.isCancelled()))
		{
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
