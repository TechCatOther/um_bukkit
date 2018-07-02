package org.ultramine.mods.bukkit.mixin.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(net.minecraft.entity.monster.EntitySnowman.class)
public class MixinEntitySnowman
{
	@Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", ordinal = 1))
	private boolean attackEntityFromRedir(EntityLivingBase entity, DamageSource source, float value)
	{
		return entity.attackEntityFrom(CraftEventFactory.MELTING, value);
	}

	@Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z"))
	public boolean setBlockRedirect(World world, int x, int y, int z, Block block)
	{
		BlockState blockState = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z).getState();
		blockState.setType(CraftMagicNumbers.getMaterial(Blocks.snow_layer));
		EntityBlockFormEvent event = new EntityBlockFormEvent(((IMixinEntity) this).getBukkitEntity(), blockState.getBlock(), blockState);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled())
			blockState.update(true);
		return false;
	}
}
