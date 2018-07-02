package org.ultramine.mods.bukkit.mixin.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLivingBase;
import org.ultramine.mods.bukkit.interfaces.entity.player.IMixinPlayer;
import org.ultramine.mods.bukkit.interfaces.entity.player.IMixinPlayerMP;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinContainer;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinInventoryEnderChest;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;
import org.ultramine.mods.bukkit.mixin.entity.MixinEntityLivingBase;

@Mixin(net.minecraft.entity.player.EntityPlayer.class)
public abstract class MixinPlayer extends MixinEntityLivingBase implements IMixinPlayer
{
	@Shadow
	protected boolean sleeping;
	@Shadow
	private int sleepTimer;
	@Shadow
	public int experienceLevel;

	@Shadow private InventoryEnderChest theInventoryEnderChest;

	@Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;heal(F)V"))
	private void healRedir(EntityLivingBase entity, float value)
	{
		((IMixinEntityLivingBase) entity).heal(value, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.REGEN);
	}

	@Override
	public boolean isSleeping()
	{
		return sleeping;
	}

	@Override
	public int getSleepTimer()
	{
		return sleepTimer;
	}

	@Overwrite
	protected void damageEntity(DamageSource p_70665_1_, float p_70665_2_)
	{
		super.damageEntity(p_70665_1_, p_70665_2_);
	}

	@Overwrite
	protected int getExperiencePoints(EntityPlayer p_70693_1_)
	{
		if(((IMixinPlayerMP) this).isKeepLevel())
		{
			return 0;
		}
		else
		{
			int i = this.experienceLevel * 7;
			return i > 100 ? 100 : i;
		}
	}

	@Override
	public void mountEntity(Entity entity)
	{
		setPassengerOf(entity);
	}

	@Override
	public void setPassengerOf(Entity p_70078_1_)
	{
		// CraftBukkit end
		if(this.ridingEntity != null && p_70078_1_ == null)
		{
			Bukkit.getServer().getPluginManager()
					.callEvent(new org.spigotmc.event.entity.EntityDismountEvent(getBukkitEntity(), ((IMixinEntity) ridingEntity).getBukkitEntity())); // Spigot
			// CraftBukkit start - use parent method instead to correctly fire
			// VehicleExitEvent
			Entity originalVehicle = this.ridingEntity;
			// First statement moved down, second statement handled in parent
			// method.
			/*
			 * if (!this.world.isStatic) { this.l(this.vehicle); }
             * 
             * if (this.vehicle != null) { this.vehicle.passenger = null; }
             * 
             * this.vehicle = null;
             */
			super.setPassengerOf(null);

			if(!this.worldObj.isRemote && this.ridingEntity == null)
			{
				this.dismountEntity(originalVehicle);
			}
			// CraftBukkit end
		}
		else
		{
			super.setPassengerOf(p_70078_1_); // CraftBukkit - call new parent
		}
	}

	@Inject(method = "func_146097_a", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;joinEntityItemWithWorld(Lnet/minecraft/entity/item/EntityItem;)V"))
	private void onFunc_146097_a(ItemStack p_146097_1_, boolean p_146097_2_, boolean p_146097_3_, CallbackInfoReturnable<EntityItem> ci, EntityItem entityitem)
	{
		Player player = (Player) this.getBukkitEntity();
		CraftItem drop = new CraftItem((CraftServer) Bukkit.getServer(), entityitem);
		PlayerDropItemEvent event = new PlayerDropItemEvent(player, drop);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if(event.isCancelled())
		{
			// player.getInventory().addItem(drop.getItemStack());
			org.bukkit.inventory.ItemStack cur = player.getInventory().getItemInHand();
			if(p_146097_3_ && (cur == null || cur.getAmount() == 0))
			{
				// The complete stack was dropped
				player.getInventory().setItemInHand(drop.getItemStack());
			}
			else if(p_146097_3_ && cur.isSimilar(drop.getItemStack()) && drop.getItemStack().getAmount() == 1)
			{
				// Only one item is dropped
				cur.setAmount(cur.getAmount() + 1);
				player.getInventory().setItemInHand(cur);
			}
			else
			{
				// Fallback
				player.getInventory().addItem(drop.getItemStack());
			}
			ci.setReturnValue(null);
		}
	}

	@Inject(method = "sleepInBedAt", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;mountEntity(Lnet/minecraft/entity/Entity;)V", shift = Shift.BY, by = 2))
	public void sleepInBedAtInject(int x, int y, int z, CallbackInfoReturnable<EnumStatus> cir)
	{
		if (this.getBukkitEntity() instanceof Player)
		{
			org.bukkit.block.Block bedBlock = ((IMixinWorld) this.worldObj).getWorld().getBlockAt(x, y, z);
			PlayerBedEnterEvent event = new PlayerBedEnterEvent((Player) this.getBukkitEntity(), bedBlock);
			((IMixinWorld) this.worldObj).getServer().getPluginManager().callEvent(event);
			if (event.isCancelled())
			{
				cir.setReturnValue(EntityPlayer.EnumStatus.OTHER_PROBLEM);
				cir.cancel();
			}
		}
	}

	@Inject(method = "setDead", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;onContainerClosed(Lnet/minecraft/entity/player/EntityPlayer;)V", ordinal = 1, shift = Shift.BEFORE))
	public void setDeadInject(CallbackInfo ci) {
		InventoryCloseEvent event = new InventoryCloseEvent(((IMixinContainer)((EntityPlayer)(Object)this).openContainer).getBukkitView());
		if (((IMixinContainer)((EntityPlayer)(Object)this).openContainer).getBukkitView() != null) Bukkit.getServer().getPluginManager().callEvent(event); // Cauldron - allow vanilla mods to bypass
	}

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	public void EntityPlayerInject(World p_i45324_1_, GameProfile p_i45324_2_, CallbackInfo ci)
	{
		((IMixinInventoryEnderChest) theInventoryEnderChest).setOwner((EntityPlayer)(Object) this);
	}
}
