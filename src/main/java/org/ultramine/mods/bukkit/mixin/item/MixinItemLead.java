package org.ultramine.mods.bukkit.mixin.item;

import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemLead;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.List;

@Mixin(ItemLead.class)
public class MixinItemLead
{
	@Overwrite
	public static boolean func_150909_a(EntityPlayer player, World world, int x, int y, int z)
	{
		EntityLeashKnot entityLeashKnot = EntityLeashKnot.getKnotForBlock(world, x, y, z);
		double d0 = 7.0D;
		boolean flag = false;
		List list = world.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(x - d0, y - d0, z - d0, x + d0, y + d0, z + d0));
		if (list != null)
		{
			for (Object entityLivingObject : list)
			{
				EntityLiving entityLiving = (EntityLiving) entityLivingObject;
				if (entityLiving.getLeashed() && entityLiving.getLeashedToEntity() == player)
				{
					if (entityLeashKnot == null)
					{
						entityLeashKnot = EntityLeashKnot.func_110129_a(world, x, y, z);
						HangingPlaceEvent event = new HangingPlaceEvent((Hanging) ((IMixinEntity) entityLeashKnot).getBukkitEntity(), player != null ? (Player) ((IMixinEntity) player).getBukkitEntity() : null, ((IMixinWorld) world).getWorld().getBlockAt(x, y, z), BlockFace.SELF);
						Bukkit.getPluginManager().callEvent(event);
						if (event.isCancelled())
						{
							entityLeashKnot.setDead();
							return false;
						}
					}
					// TODO: callPlayerLeashEntityEvent
					entityLiving.setLeashedToEntity(entityLeashKnot, true);
					flag = true;
				}
			}
		}
		return flag;
	}
}
