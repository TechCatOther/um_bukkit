package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.List;

@Mixin(BlockPressurePlate.class)
public abstract class MixinBlockPressurePlate extends BlockBasePressurePlate
{
	@Shadow private BlockPressurePlate.Sensitivity field_150069_a;

	protected MixinBlockPressurePlate(String p_i45387_1_, Material p_i45387_2_)
	{
		super(p_i45387_1_, p_i45387_2_);
	}

	@Overwrite
	protected int func_150065_e(World world, int x, int y, int z)
	{
		List entityList = null;
		if (this.field_150069_a == BlockPressurePlate.Sensitivity.everything)
			entityList = world.getEntitiesWithinAABBExcludingEntity(null, this.func_150061_a(x, y, z));
		if (this.field_150069_a == BlockPressurePlate.Sensitivity.mobs)
			entityList = world.getEntitiesWithinAABB(EntityLivingBase.class, this.func_150061_a(x, y, z));
		if (this.field_150069_a == BlockPressurePlate.Sensitivity.players)
			entityList = world.getEntitiesWithinAABB(EntityPlayer.class, this.func_150061_a(x, y, z));
		if (entityList != null && !entityList.isEmpty())
			for (Object entityObject : entityList)
			{
				Entity entity = (Entity) entityObject;
				if (this.func_150060_c(world.getBlockMetadata(x, y, z)) == 0)
				{
					Cancellable cancellable;
					if (entity instanceof EntityPlayer)
					{
						cancellable = CraftEventFactory.callPlayerInteractEvent((EntityPlayer) entity, Action.PHYSICAL, x, y, z, -1, null);
					}
					else
					{
						cancellable = new EntityInteractEvent(((IMixinEntity) entity).getBukkitEntity(), ((IMixinWorld) world).getWorld().getBlockAt(x, y, z));
						Bukkit.getPluginManager().callEvent((EntityInteractEvent) cancellable);
					}
					if (cancellable.isCancelled())
						continue;
				}
				if (!entity.doesEntityNotTriggerPressurePlate())
					return 15;
			}
		return 0;
	}
}
