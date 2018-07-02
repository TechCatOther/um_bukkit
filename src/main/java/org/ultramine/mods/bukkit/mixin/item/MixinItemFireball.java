package org.ultramine.mods.bukkit.mixin.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockIgniteEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFireball.class)
public class MixinItemFireball
{
	@Inject(method = "onItemUse", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSoundEffect(DDDLjava/lang/String;FF)V", shift = Shift.BEFORE))
	public void onItemUseInject(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_, CallbackInfoReturnable<Boolean> cir)
	{
		if (org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(world, x, y, z, BlockIgniteEvent.IgniteCause.FIREBALL, player).isCancelled())
		{
			if (!player.capabilities.isCreativeMode)
				--itemStack.stackSize;
			cir.setReturnValue(false);
		}
	}
}
