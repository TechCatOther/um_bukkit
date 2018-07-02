package org.ultramine.mods.bukkit.mixin.entity.boss;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.entity.boss.IMixinEntityDragon;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(net.minecraft.entity.boss.EntityDragon.class)
public abstract class MixinEntityDragon extends EntityLiving implements IMixinEntityDragon
{
	public MixinEntityDragon(World w)
	{
		super(w);
	}

	@Shadow
	protected abstract boolean func_82195_e(DamageSource source, float amount);

	@Redirect(method = "updateDragonEnderCrystal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;setHealth(F)V"))
	private void setHealthRedir(EntityLivingBase entity, float value)
	{
		EntityRegainHealthEvent event = new EntityRegainHealthEvent(((IMixinEntity) this).getBukkitEntity(), 1.0D, EntityRegainHealthEvent.RegainReason.ENDER_CRYSTAL);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if(!event.isCancelled())
		{
			this.setHealth((float) (this.getHealth() + event.getAmount()));
		}
	}

	@Override
	public boolean realAttackEntityFrom(DamageSource source, float amount)
	{
		return func_82195_e(source, amount);
	}

	@Inject(method = "createEnderPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z", ordinal = 12, shift = Shift.BY, by = 2))
	public void createEnderPortalInject(int x, int z, CallbackInfo ci)
	{
		BlockStateListPopulator world = new BlockStateListPopulator(((IMixinWorld) this.worldObj).getWorld());
		EntityCreatePortalEvent event = new EntityCreatePortalEvent((org.bukkit.entity.LivingEntity) (((IMixinEntity) this).getBukkitEntity()), java.util.Collections.unmodifiableList(world.getList()), org.bukkit.PortalType.ENDER);
		((IMixinWorld) this.worldObj).getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled())
			for (BlockState state : event.getBlocks())
				state.update(true);
		else
			for (BlockState state : event.getBlocks())
			{
				S23PacketBlockChange packet = new S23PacketBlockChange(state.getX(), state.getY(), state.getZ(), this.worldObj);
				for (Object playerEntity : this.worldObj.playerEntities)
				{
					EntityPlayer entity = (EntityPlayer) playerEntity;
					if (entity instanceof EntityPlayerMP)
						((EntityPlayerMP) entity).playerNetServerHandler.sendPacket(packet);
				}
			}
	}
}
