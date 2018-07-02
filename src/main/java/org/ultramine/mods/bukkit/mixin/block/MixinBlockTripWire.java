package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockTripWire;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.List;

@Mixin(BlockTripWire.class)
public abstract class MixinBlockTripWire
{
	@Inject(method = "func_150140_e", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;doesEntityNotTriggerPressurePlate()Z", shift = Shift.BY, by = 15), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void func_150140_e(World world, int x, int y, int z, CallbackInfo ci, int l, boolean flag, boolean flag1, List list)
	{
		if (flag != flag1 && flag1 && ((world.getBlockMetadata(x, y, z) & 4) == 4 || world.getBlockMetadata(x, y, z) == 0))
		{
			boolean allowed = false;
			for (Object entityObject : list)
				if (entityObject != null && entityObject instanceof Entity)
				{
					Cancellable cancellable;
					if (entityObject instanceof EntityPlayer)
					{
						cancellable = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent((EntityPlayer) entityObject, org.bukkit.event.block.Action.PHYSICAL, x, y, z, -1, null);
					}
					else
					{
						cancellable = new EntityInteractEvent(((IMixinEntity) entityObject).getBukkitEntity(), ((IMixinWorld) world).getWorld().getBlockAt(x, y, z));
						Bukkit.getPluginManager().callEvent((EntityInteractEvent) cancellable);
					}
					if (!cancellable.isCancelled())
					{
						allowed = true;
						break;
					}
				}
			if (!allowed)
				ci.cancel();
		}
	}
}
