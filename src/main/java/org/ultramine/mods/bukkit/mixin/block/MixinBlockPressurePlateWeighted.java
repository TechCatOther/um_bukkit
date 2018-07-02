package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockPressurePlateWeighted;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(BlockPressurePlateWeighted.class)
public abstract class MixinBlockPressurePlateWeighted extends BlockBasePressurePlate
{
	@Final
	@Shadow private int field_150068_a;

	protected MixinBlockPressurePlateWeighted(String p_i45387_1_, Material p_i45387_2_)
	{
		super(p_i45387_1_, p_i45387_2_);
	}

	@Overwrite
	protected int func_150065_e(World world, int x, int y, int z)
	{
		int l = 0;
		for (Object entityObject : world.getEntitiesWithinAABB(Entity.class, this.func_150061_a(x, y, z)))
		{
			Entity entity = (Entity) entityObject;
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
			if (!cancellable.isCancelled())
				l++;
		}
		l = Math.min(l, this.field_150068_a);
		if (l <= 0)
			return 0;
		float f = Math.min(this.field_150068_a, l) / this.field_150068_a;
		return MathHelper.ceiling_float_int(f * 15.0F);
	}
}
