package org.ultramine.mods.bukkit.mixin.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ultramine.mods.bukkit.interfaces.entity.projectile.IMixinEntityFireball;

@Mixin(net.minecraft.entity.projectile.EntityFireball.class)
public abstract class MixinEntityFireball extends Entity implements IMixinEntityFireball
{
	public MixinEntityFireball(World w)
	{
		super(w);
	}

	@Shadow
	public double accelerationX;
	@Shadow
	public double accelerationY;
	@Shadow
	public double accelerationZ;

	public float bukkitYield = 1; // CraftBukkit
	public boolean isIncendiary = true; // CraftBukkit

	@Override
	public float getBukkitYield()
	{
		return bukkitYield;
	}

	@Override
	public void setBukkitYield(float bukkitYield)
	{
		this.bukkitYield = bukkitYield;
	}

	@Override
	public boolean isIncendiary()
	{
		return isIncendiary;
	}

	@Override
	public void setIncendiary(boolean isIncendiary)
	{
		this.isIncendiary = isIncendiary;
	}

	@Inject(method = "attackEntityFrom", cancellable = true,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/DamageSource;getEntity()Lnet/minecraft/entity/Entity;"))
	public void onAttackEntityFrom(DamageSource p_70097_1_, float p_70097_2_, CallbackInfoReturnable<Boolean> ci)
	{
		if(CraftEventFactory.handleNonLivingEntityDamageEvent(this, p_70097_1_, p_70097_2_))
			ci.setReturnValue(false);
	}

	@Override
	public void setDirection(double p_i1761_3_, double p_i1761_5_, double p_i1761_7_)
	{
		// CraftBukkit end
		p_i1761_3_ += this.rand.nextGaussian() * 0.4D;
		p_i1761_5_ += this.rand.nextGaussian() * 0.4D;
		p_i1761_7_ += this.rand.nextGaussian() * 0.4D;
		double d3 = (double) MathHelper.sqrt_double(p_i1761_3_ * p_i1761_3_ + p_i1761_5_ * p_i1761_5_ + p_i1761_7_ * p_i1761_7_);
		this.accelerationX = p_i1761_3_ / d3 * 0.1D;
		this.accelerationY = p_i1761_5_ / d3 * 0.1D;
		this.accelerationZ = p_i1761_7_ / d3 * 0.1D;
	}
}
