package org.ultramine.mods.bukkit.mixin.entity.item;

import com.avaje.ebean.enhance.asm.Opcodes;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.entity.item.EntityEnderCrystal.class)
public abstract class MixinEntityEnderCrystal
{
	@Inject(method = "attackEntityFrom", cancellable = true, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/item/EntityEnderCrystal;health:I"))
	public void onAttackEntityFrom(DamageSource p_70097_1_, float p_70097_2_, CallbackInfoReturnable<Boolean> ci)
	{
		if(CraftEventFactory.handleNonLivingEntityDamageEvent((EntityEnderCrystal) (Object) this, p_70097_1_, p_70097_2_))
			ci.setReturnValue(false);
	}

	@Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z"))
	public boolean setBlockRedirect(World world, int x, int y, int z, Block block)
	{
		if (!CraftEventFactory.callBlockIgniteEvent(world, x, y, z, (EntityEnderCrystal) (Object) this).isCancelled())
			world.setBlock(x, y, z, Blocks.fire);
		return false;
	}
}
