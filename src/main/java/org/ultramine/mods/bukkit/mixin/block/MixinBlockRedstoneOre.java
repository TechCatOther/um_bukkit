package org.ultramine.mods.bukkit.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(BlockRedstoneOre.class)
public abstract class MixinBlockRedstoneOre extends Block
{
	@Shadow protected abstract void func_150185_e(net.minecraft.world.World p_150185_1_, int p_150185_2_, int p_150185_3_, int p_150185_4_);

	protected MixinBlockRedstoneOre(Material p_i45394_1_)
	{
		super(p_i45394_1_);
	}

	@Overwrite
	public void onEntityWalking(World world, int x, int y, int z, Entity entity)
	{
		if (entity instanceof EntityPlayer)
		{
			PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent((EntityPlayer) entity, Action.PHYSICAL, x, y, z, -1, null);
			if (!event.isCancelled())
			{
				this.func_150185_e(world, x, y, z);
				super.onEntityWalking(world, x, y, z, entity);
			}
		}
		else
		{
			EntityInteractEvent event = new EntityInteractEvent(((IMixinEntity) entity).getBukkitEntity(), ((IMixinWorld) world).getWorld().getBlockAt(x, y, z));
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled())
			{
				this.func_150185_e(world, x, y, z);
				super.onEntityWalking(world, x, y, z, entity);
			}
		}
	}
}
