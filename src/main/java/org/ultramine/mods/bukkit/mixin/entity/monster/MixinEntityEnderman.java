package org.ultramine.mods.bukkit.mixin.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(EntityEnderman.class)
public abstract class MixinEntityEnderman
{
	private Block block;

	@Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/EntityEnderman;func_146081_a(Lnet/minecraft/block/Block;)V"))
	private void onLivingUpdateFunc_146081_aRedirect(EntityEnderman entityEnderman, Block argumentBlock)
	{
		if(this.block == null)
			this.block = argumentBlock;
	}

	@Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/EntityEnderman;setCarryingData(I)V"))
	private void onLivingUpdateSetCarryingDataRedirect(EntityEnderman entityEnderman, int p_70817_1_)
	{

	}

	@Shadow public abstract int getCarryingData();
	@Shadow public abstract Block func_146080_bZ();
	@Shadow public abstract void func_146081_a(Block p_146081_1_);
	@Shadow public abstract void setCarryingData(int p_70817_1_);

	@Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z"))
	private boolean onLivingUpdateSetBlockRedirect(World world, int x, int y, int z, Block block)
	{
		if (((IMixinWorld) world).getWorld() == null || !CraftEventFactory.callEntityChangeBlockEvent((EntityEnderman) (Object) this, ((IMixinWorld) world).getWorld().getBlockAt(x, y, z), org.bukkit.Material.AIR).isCancelled()) // Cauldron
		{
			this.func_146081_a(block);
			this.setCarryingData(world.getBlockMetadata(x, y, z));
			world.setBlock(x, y, z, Blocks.air);
		}
		return false;
	}

	@Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;II)Z"))
	private boolean onLivingUpdateSetBlockRedirect(World world, int x, int y, int z, Block block, int meta, int flags)
	{
		if (!CraftEventFactory.callEntityChangeBlockEvent((EntityEnderman)(Object)this, x, y, z, this.func_146080_bZ(), this.getCarryingData()).isCancelled())
		{
			world.setBlock(x, y, z, this.func_146080_bZ(), this.getCarryingData(), 3);
			this.func_146081_a(Blocks.air);
		}
		return false;
	}
}
