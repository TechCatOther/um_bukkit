package org.ultramine.mods.bukkit.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.IMixinFoodStats;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLivingBase;

@Mixin(net.minecraft.util.FoodStats.class)
public abstract class MixinFoodStats implements IMixinFoodStats
{
	@Shadow
	private int foodLevel;
	@Shadow
	private float foodSaturationLevel;
	@Shadow
	private float foodExhaustionLevel;
	@Shadow
	private int foodTimer;

	@Shadow
	public abstract void addStats(int p_75122_1_, float p_75122_2_);

	private EntityPlayer entityplayer;

	@Override
	public void sendUpdatePacket()
	{
		sendUpdatePacket((EntityPlayerMP) entityplayer);
	}

	@Override
	public void sendUpdatePacket(EntityPlayerMP player)
	{
		player.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(
				((CraftPlayer) ((IMixinEntity) entityplayer).getBukkitEntity()).getScaledHealth(),
				((IMixinFoodStats) entityplayer.getFoodStats()).getFoodLevel(),
				((IMixinFoodStats) entityplayer.getFoodStats()).getFoodSaturationLevel()));
	}

	@Overwrite
	public void func_151686_a(ItemFood p_151686_1_, ItemStack p_151686_2_)
	{
		if(entityplayer == null)
			return;
		// CraftBukkit start
		int oldFoodLevel = foodLevel;
		org.bukkit.event.entity.FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(entityplayer, p_151686_1_.func_150905_g(p_151686_2_) + oldFoodLevel);

		if(!event.isCancelled())
		{
			this.addStats(event.getFoodLevel() - oldFoodLevel, p_151686_1_.func_150906_h(p_151686_2_));
		}

		sendUpdatePacket();
		// CraftBukkit end
	}

	@Inject(method = "onUpdate", at = @At(value = "HEAD"))
	public void onOnUpdate(EntityPlayer p_75118_1_, CallbackInfo ci)
	{
		this.entityplayer = p_75118_1_;
	}

	@Inject(method = "onUpdate", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/util/FoodStats;foodLevel:I", shift = Shift.AFTER))
	private void foodLevelSetRedir(EntityPlayer p_75118_1_, CallbackInfo ci)
	{
		org.bukkit.event.entity.FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(entityplayer, Math.max(this.foodLevel - 1, 0));

		if(!event.isCancelled())
		{
			this.foodLevel = event.getFoodLevel();
		}

		sendUpdatePacket((EntityPlayerMP) p_75118_1_);
	}

	@Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;heal(F)V"))
	private void healRedir(EntityLivingBase entity, float value)
	{
		((IMixinEntityLivingBase) entity).heal(value, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED);
	}

	@Override
	public int getFoodLevel()
	{
		return foodLevel;
	}

	@Override
	public void setFoodLevel(int foodLevel)
	{
		this.foodLevel = foodLevel;
	}

	@Override
	public float getFoodSaturationLevel()
	{
		return foodSaturationLevel;
	}

	@Override
	public void setFoodSaturationLevel(float foodSaturationLevel)
	{
		this.foodSaturationLevel = foodSaturationLevel;
	}

	@Override
	public float getFoodExhaustionLevel()
	{
		return foodExhaustionLevel;
	}

	@Override
	public void setFoodExhaustionLevel(float foodExhaustionLevel)
	{
		this.foodExhaustionLevel = foodExhaustionLevel;
	}

	@Override
	public int getFoodTimer()
	{
		return foodTimer;
	}

	@Override
	public void setFoodTimer(int foodTimer)
	{
		this.foodTimer = foodTimer;
	}
}
