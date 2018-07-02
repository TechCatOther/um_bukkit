package org.ultramine.mods.bukkit.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.IMixinPotion;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLivingBase;

@Mixin(net.minecraft.potion.Potion.class)
public class MixinPotion implements IMixinPotion
{
	@Shadow
	public int id;

	@Redirect(method = "performEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;heal(F)V"))
	private void healRedir(EntityLivingBase entity, float value)
	{
		if(this.id == Potion.regeneration.id)
			((IMixinEntityLivingBase) entity).heal(value, RegainReason.MAGIC_REGEN);
		else
			((IMixinEntityLivingBase) entity).heal(value, RegainReason.MAGIC);
	}

	@Redirect(method = "performEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
	private boolean attackEntityFromRedir(EntityLivingBase entity, DamageSource source, float value)
	{
		if(source == DamageSource.magic && value == 1.0F)
			return entity.attackEntityFrom(CraftEventFactory.POISON, value);
		else
			return entity.attackEntityFrom(source, value);
	}

	@Overwrite
	public void affectEntity(EntityLivingBase p_76402_1_, EntityLivingBase p_76402_2_, int p_76402_3_, double p_76402_4_)
	{
		// CraftBukkit start - Delegate; we need EntityPotion
		applyInstantEffect(p_76402_1_, p_76402_2_, p_76402_3_, p_76402_4_, null);
	}

	@Override
	public void applyInstantEffect(EntityLivingBase p_76402_1_, EntityLivingBase p_76402_2_, int p_76402_3_, double p_76402_4_, EntityPotion potion)
	{
		// CraftBukkit end
		int j;

		if((this.id != Potion.heal.id || p_76402_2_.isEntityUndead()) && (this.id != Potion.harm.id || !p_76402_2_.isEntityUndead()))
		{
			if(this.id == Potion.harm.id && !p_76402_2_.isEntityUndead() || this.id == Potion.heal.id && p_76402_2_.isEntityUndead())
			{
				j = (int) (p_76402_4_ * (double) (6 << p_76402_3_) + 0.5D);

				if(p_76402_1_ == null)
				{
					p_76402_2_.attackEntityFrom(DamageSource.magic, (float) j);
				}
				else
				{
					// CraftBukkit - The "damager" needs to be the potion
					p_76402_2_.attackEntityFrom(DamageSource.causeIndirectMagicDamage(potion != null ? potion : p_76402_2_, p_76402_1_), (float) j);
				}
			}
		}
		else
		{
			j = (int) (p_76402_4_ * (double) (4 << p_76402_3_) + 0.5D);
			((IMixinEntityLivingBase) p_76402_2_).heal((float) j, RegainReason.MAGIC); // CraftBukkit
		}
	}
}
