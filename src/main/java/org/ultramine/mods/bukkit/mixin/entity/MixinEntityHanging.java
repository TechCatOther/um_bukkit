package org.ultramine.mods.bukkit.mixin.entity;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Painting;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;

@Mixin(EntityHanging.class)
public abstract class MixinEntityHanging extends Entity
{
	public MixinEntityHanging(World p_i1582_1_)
	{
		super(p_i1582_1_);
	}

	@Inject(method = "onUpdate", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;setDead()V", shift = Shift.BEFORE))
	public void onUpdateInject(CallbackInfo ci)
	{
		Material material = this.worldObj.getBlock((int) this.posX, (int) this.posY, (int) this.posZ).getMaterial();
		HangingBreakEvent.RemoveCause cause;
		if (!material.equals(Material.air))
		{
			// TODO: This feels insufficient to catch 100% of suffocation cases
			cause = HangingBreakEvent.RemoveCause.OBSTRUCTION;
		}
		else
		{
			cause = HangingBreakEvent.RemoveCause.PHYSICS;
		}
		HangingBreakEvent event = new HangingBreakEvent((Hanging) ((IMixinEntity) this).getBukkitEntity(), cause);
		Bukkit.getPluginManager().callEvent(event);
		PaintingBreakEvent paintingEvent = null;
		if (((EntityHanging) (Object) this) instanceof EntityPainting)
		{
			// Fire old painting event until it can be removed
			paintingEvent = new PaintingBreakEvent((Painting) ((IMixinEntity) this).getBukkitEntity(), PaintingBreakEvent.RemoveCause.valueOf(cause.name()));
			paintingEvent.setCancelled(event.isCancelled());
			Bukkit.getPluginManager().callEvent(paintingEvent);
		}
		if (isDead || event.isCancelled() || (paintingEvent != null && paintingEvent.isCancelled()))
			ci.cancel();
	}

	@Inject(method = "attackEntityFrom", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;setDead()V", shift = Shift.BEFORE))
	public void attackEntityFromInject(DamageSource damageSource, float p_70097_2_, CallbackInfoReturnable<Boolean> cir)
	{
		HangingBreakEvent event = new HangingBreakEvent((Hanging) ((IMixinEntity) this).getBukkitEntity(), HangingBreakEvent.RemoveCause.DEFAULT);
		PaintingBreakEvent paintingEvent = null;
		if (damageSource.getEntity() != null)
		{
			event = new org.bukkit.event.hanging.HangingBreakByEntityEvent((Hanging) ((IMixinEntity) this).getBukkitEntity(), damageSource.getEntity() == null ? null : ((IMixinEntity) damageSource.getEntity()).getBukkitEntity());
			if (((EntityHanging) (Object) this) instanceof EntityPainting)
			{
				// Fire old painting event until it can be removed
				paintingEvent = new org.bukkit.event.painting.PaintingBreakByEntityEvent((Painting) ((IMixinEntity) this).getBukkitEntity(), damageSource.getEntity() == null ? null : ((IMixinEntity) damageSource.getEntity()).getBukkitEntity());
			}
		}
		else if (damageSource.isExplosion())
		{
			event = new HangingBreakEvent((Hanging) ((IMixinEntity) this).getBukkitEntity(), HangingBreakEvent.RemoveCause.EXPLOSION);
		}
		Bukkit.getPluginManager().callEvent(event);
		if (paintingEvent != null)
		{
			paintingEvent.setCancelled(event.isCancelled());
			Bukkit.getPluginManager().callEvent(paintingEvent);
		}
		if (this.isDead || event.isCancelled() || (paintingEvent != null && paintingEvent.isCancelled()))
		{
			cir.setReturnValue(true);
			cir.cancel();
		}
	}

	@Inject(method = "moveEntity", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;setDead()V", shift = Shift.BEFORE))
	public void moveEntity(double x, double y, double z, CallbackInfo ci)
	{
		if (this.isDead)
		{
			ci.cancel();
			return;
		}
		//TODO - Does this need its own cause? Seems to only be triggered by pistons
		HangingBreakEvent event = new HangingBreakEvent((Hanging) ((IMixinEntity) this).getBukkitEntity(), HangingBreakEvent.RemoveCause.PHYSICS);
		Bukkit.getPluginManager().callEvent(event);
		if (this.isDead || event.isCancelled())
			ci.cancel();
	}
}
