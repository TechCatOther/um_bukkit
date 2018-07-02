package org.ultramine.mods.bukkit.mixin.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLivingBase;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(net.minecraft.entity.EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity implements IMixinEntityLivingBase
{
	@Shadow protected float lastDamage;
	@Shadow protected EntityPlayer attackingPlayer;
	@Shadow private HashMap<Integer, PotionEffect> activePotionsMap;
	@Shadow protected int entityAge;
	@Shadow public float prevLimbSwingAmount;
	@Shadow public float limbSwingAmount;
	@Shadow public int maxHurtResistantTime;
	@Shadow public float prevHealth;
	@Shadow public int hurtTime;
	@Shadow public int maxHurtTime;
	@Shadow public float attackedAtYaw;
	@Shadow protected int recentlyHit;

	@Shadow public abstract ItemStack getEquipmentInSlot(int p_71124_1_);
	@Shadow public abstract boolean isPotionActive(Potion p_70644_1_);
	@Shadow public abstract PotionEffect getActivePotionEffect(Potion p_70660_1_);
	@Shadow public abstract float getAbsorptionAmount();
	@Shadow protected abstract void damageArmor(float p_70675_1_);
	@Shadow public abstract void setAbsorptionAmount(float p_110149_1_);
	@Shadow public abstract CombatTracker func_110142_aN();
	@Shadow public abstract void setRevengeTarget(EntityLivingBase p_70604_1_);
	@Shadow public abstract void knockBack(Entity p_70653_1_, float p_70653_2_, double p_70653_3_, double p_70653_5_);
	@Shadow protected abstract String getDeathSound();
	@Shadow protected abstract float getSoundVolume();
	@Shadow protected abstract float getSoundPitch();
	@Shadow protected abstract String getHurtSound();
	@Shadow public abstract void onDeath(DamageSource p_70645_1_);
	@Shadow public abstract float getMaxHealth();
	@Shadow protected abstract int getExperiencePoints(EntityPlayer p_70693_1_);
	@Shadow protected abstract boolean isPlayer();
	@Shadow protected abstract boolean func_146066_aG();
	@Shadow public abstract void dismountEntity(Entity p_110145_1_);
	@Shadow public abstract int getTotalArmorValue();

	public int expToDrop;
	public int maxAirTicks = 300;

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;setHealth(F)V"))
	public void setHealthOriginal(EntityLivingBase entity, float value)
	{
		dataWatcher.updateObject(6, value);
	}

	@Override
	public int getExpToDrop()
	{
		return expToDrop;
	}

	@Override
	public void setExpToDrop(int expToDrop)
	{
		this.expToDrop = expToDrop;
	}

	@Override
	public int getMaxAirTicks()
	{
		return maxAirTicks;
	}

	@Override
	public void setMaxAirTicks(int maxAirTicks)
	{
		this.maxAirTicks = maxAirTicks;
	}

	@Override
	public float getLastDamage()
	{
		return lastDamage;
	}

	@Override
	public void setLastDamage(float lastDamage)
	{
		this.lastDamage = lastDamage;
	}

	@Override
	public int getRecentlyHit()
	{
		return recentlyHit;
	}

	@Override
	public void setRecentlyHit(int recentlyHit)
	{
		this.recentlyHit = recentlyHit;
	}

	@Override
	public EntityPlayer getAttackingPlayer()
	{
		return attackingPlayer;
	}

	@Override
	public HashMap<Integer, PotionEffect> getActivePotionsMap()
	{
		return activePotionsMap;
	}

	@Override
	public float applyArmorCalculationsP(DamageSource source, float damage)
	{
		return applyArmorCalculations(source, damage);
	}

	@Overwrite
	protected float applyArmorCalculations(DamageSource p_70655_1_, float p_70655_2_)
	{
		if (!p_70655_1_.isUnblockable())
		{
			int i = 25 - this.getTotalArmorValue();
			float f1 = p_70655_2_ * (float)i;
			// this.damageArmor(p_70655_2_); // CraftBukkit - Moved into damageEntity_CB(DamageSource, float)
			p_70655_2_ = f1 / 25.0F;
		}

		return p_70655_2_;
	}

	@Override
	public float applyPotionDamageCalculationsP(DamageSource source, float damage)
	{
		return applyPotionDamageCalculations(source, damage);
	}

	@Override
	public int getExpReward()
	{
		int exp = this.getExperiencePoints(this.attackingPlayer);

		if(!this.worldObj.isRemote && (this.recentlyHit > 0 || this.isPlayer()) && this.func_146066_aG())
		{
			return exp;
		}
		else
		{
			return 0;
		}
	}

	@Overwrite
	public void heal(float p_70691_1_)
	{
		heal(p_70691_1_, EntityRegainHealthEvent.RegainReason.CUSTOM);
	}

	@Override
	public void heal(float p_70691_1_, EntityRegainHealthEvent.RegainReason regainReason)
	{
		p_70691_1_ = net.minecraftforge.event.ForgeEventFactory.onLivingHeal((EntityLivingBase) (Object) this, p_70691_1_);
		if(p_70691_1_ <= 0) return;
		float f1 = this.getHealth();

		if(f1 > 0.0F)
		{
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(this.getBukkitEntity(), p_70691_1_, regainReason);
			((IMixinWorld) worldObj).getServer().getPluginManager().callEvent(event);

			if(!event.isCancelled())
			{
				this.setHealth((float) (this.getHealth() + event.getAmount()));
			}
		}
	}

	@Overwrite
	public float getHealth()
	{
		// CraftBukkit start - Use unscaled health
		if((Object) this instanceof EntityPlayerMP)
		{
			return (float) ((Player) this.getBukkitEntity()).getHealth();
		}
		// CraftBukkit end
		return this.dataWatcher.getWatchableObjectFloat(6);
	}

	@Overwrite
	public void setHealth(float p_70606_1_)
	{
		// CraftBukkit start - Handle scaled health
		if((Object) this instanceof EntityPlayerMP)
		{
			CraftPlayer player = (CraftPlayer) this.getBukkitEntity();

			// Squeeze
			if(p_70606_1_ < 0.0F)
			{
				player.setRealHealth(0.0D);
			}
			else if(p_70606_1_ > player.getMaxHealth())
			{
				player.setRealHealth(player.getMaxHealth());
			}
			else
			{
				player.setRealHealth(p_70606_1_);
			}

			this.dataWatcher.updateObject(6, Float.valueOf(player.getScaledHealth()));
			return;
		}
		// CraftBukkit end
		this.dataWatcher.updateObject(6, Float.valueOf(MathHelper.clamp_float(p_70606_1_, 0.0F, this.getMaxHealth())));
	}

	@Redirect(method = "onEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setAir(I)V", ordinal = 2))
	private void onEntityUpdate_setAir(EntityLivingBase _this, int value)
	{
		if(getAir() == value)
			return;
		if(value == 300)
			setAir(maxAirTicks);
	}

	@Redirect(method = "onDeathUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getExperiencePoints(Lnet/minecraft/entity/player/EntityPlayer;)I"))
	private int onDeathUpdate_getExperiencePoints(EntityLivingBase _this, EntityPlayer attackingPlayer)
	{
		int ret = this.getExpToDrop();
		setExpToDrop(0);
		return ret;
	}

	@Overwrite
	protected void damageEntity(final DamageSource damagesource, float damage)
	{
		damageEntity_CB(damagesource, damage);
	}

	protected boolean damageEntity_CB(final DamageSource damagesource, float damage)
	{
		EntityLivingBase entity = (EntityLivingBase) (Object) this;
		if(entity.isEntityInvulnerable())
			return false;

		damage = ForgeHooks.onLivingHurt(entity, damagesource, damage);
		if(damage <= 0)
			return true;

		EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(((EntityLivingBase) (Object) this), damagesource, damage);
		if(event.isCancelled())
			return false;

		damage = (float) event.getFinalDamage();
		// Apply damage to helmet
		if((damagesource == DamageSource.anvil || damagesource == DamageSource.fallingBlock) && this.getEquipmentInSlot(4) != null)
		{
			this.getEquipmentInSlot(4).damageItem((int) (event.getDamage() * 4.0F + ThreadLocalRandom.current().nextFloat() * event.getDamage() * 2.0F), (EntityLivingBase) (Object) this);
		}

		final boolean human = entity instanceof EntityPlayer;

		// Apply damage to armor
		if(!damagesource.isUnblockable())
		{
			float armorDamage = (float) (event.getDamage() + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.HARD_HAT));
			if(human)
			{
				EntityPlayer player = (EntityPlayer) (Object) this;
				armorDamage = ArmorProperties.ApplyArmor(player, player.inventory.armorInventory, damagesource, armorDamage);
			}
			else
			{
				this.damageArmor(armorDamage);
			}
		}

		float absorptionModifier = (float) -event.getDamage(DamageModifier.ABSORPTION);
		this.setAbsorptionAmount(Math.max(this.getAbsorptionAmount() - absorptionModifier, 0.0F));
		if(damage != 0.0F)
		{
			if(human)
			{
				((EntityPlayer) (Object) this).addExhaustion(damagesource.getHungerDamage());
			}
			// CraftBukkit end
			float f2 = this.getHealth();
			this.setHealth(f2 - damage);
			this.func_110142_aN().func_94547_a(damagesource, f2, damage);
			// CraftBukkit start
			if(human)
			{
				return true;
			}
			// CraftBukkit end
			this.setAbsorptionAmount(this.getAbsorptionAmount() - damage);
		}
		return true;
	}

	@Overwrite
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		if(ForgeHooks.onLivingAttack((EntityLivingBase) (Object) this, p_70097_1_, p_70097_2_)) return false;
		if(this.isEntityInvulnerable())
		{
			return false;
		}
		else if(this.worldObj.isRemote)
		{
			return false;
		}
		else
		{
			this.entityAge = 0;

			if(this.getHealth() <= 0.0F)
			{
				return false;
			}
			else if(p_70097_1_.isFireDamage() && this.isPotionActive(Potion.fireResistance))
			{
				return false;
			}
			else
			{
				// CraftBukkit - Moved into damageEntity_CB(DamageSource, float)
//				if ((p_70097_1_ == DamageSource.anvil || p_70097_1_ == DamageSource.fallingBlock) && this.getEquipmentInSlot(4) != null)
//				{
//					this.getEquipmentInSlot(4).damageItem((int)(p_70097_2_ * 4.0F + this.rand.nextFloat() * p_70097_2_ * 2.0F), (EntityLivingBase) (Object) this);
//					p_70097_2_ *= 0.75F;
//				}

				this.limbSwingAmount = 1.5F;
				boolean flag = true;

				if((float) this.hurtResistantTime > (float) this.maxHurtResistantTime / 2.0F)
				{
					if(p_70097_2_ <= this.lastDamage)
					{
						return false;
					}

					// CraftBukkit start
					if(!this.damageEntity_CB(p_70097_1_, p_70097_2_ - this.lastDamage))
						return false;
					// CraftBukkit end
					this.lastDamage = p_70097_2_;
					flag = false;
				}
				else
				{
					// CraftBukkit start
					float previousHealth = this.getHealth();
					if(!this.damageEntity_CB(p_70097_1_, p_70097_2_))
					{
						return false;
					}
					this.lastDamage = p_70097_2_;
					this.prevHealth = previousHealth;
					this.hurtResistantTime = this.maxHurtResistantTime;
					// CraftBukkit end
					this.hurtTime = this.maxHurtTime = 10;
				}

				this.attackedAtYaw = 0.0F;
				Entity entity = p_70097_1_.getEntity();

				if(entity != null)
				{
					if(entity instanceof EntityLivingBase)
					{
						this.setRevengeTarget((EntityLivingBase) entity);
					}

					if(entity instanceof EntityPlayer)
					{
						this.recentlyHit = 100;
						this.attackingPlayer = (EntityPlayer) entity;
					}
					else if(entity instanceof net.minecraft.entity.passive.EntityTameable)
					{
						net.minecraft.entity.passive.EntityTameable entitywolf = (net.minecraft.entity.passive.EntityTameable) entity;

						if(entitywolf.isTamed())
						{
							this.recentlyHit = 100;
							this.attackingPlayer = null;
						}
					}
				}

				if(flag)
				{
					this.worldObj.setEntityState((Entity) (Object) this, (byte) 2);

					if(p_70097_1_ != DamageSource.drown)
					{
						this.setBeenAttacked();
					}

					if(entity != null)
					{
						double d1 = entity.posX - this.posX;
						double d0;

						for(d0 = entity.posZ - this.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D)
						{
							d1 = (Math.random() - Math.random()) * 0.01D;
						}

						this.attackedAtYaw = (float) (Math.atan2(d0, d1) * 180.0D / Math.PI) - this.rotationYaw;
						this.knockBack(entity, p_70097_2_, d1, d0);
					}
					else
					{
						this.attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
					}
				}

				String s;

				if(this.getHealth() <= 0.0F)
				{
					s = this.getDeathSound();

					if(flag && s != null)
					{
						this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
					}

					this.onDeath(p_70097_1_);
				}
				else
				{
					s = this.getHurtSound();

					if(flag && s != null)
					{
						this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
					}
				}

				return true;
			}
		}
	}

	@Overwrite
	protected float applyPotionDamageCalculations(DamageSource p_70672_1_, float p_70672_2_)
	{
		if(p_70672_1_.isDamageAbsolute())
		{
			return p_70672_2_;
		}
		else
		{

			int i;
			int j;
			float f1;

			// CraftBukkit - Moved to damageEntity_CB(DamageSource, float)
//			if (this.isPotionActive(Potion.resistance) && p_70672_1_ != DamageSource.outOfWorld)
//			{
//				i = (this.getActivePotionEffect(Potion.resistance).getAmplifier() + 1) * 5;
//				j = 25 - i;
//				f1 = p_70672_2_ * (float)j;
//				p_70672_2_ = f1 / 25.0F;
//			}

			if(p_70672_2_ <= 0.0F)
			{
				return 0.0F;
			}
			else
			{
				i = EnchantmentHelper.getEnchantmentModifierDamage(this.getLastActiveItems(), p_70672_1_);

				if(i > 20)
				{
					i = 20;
				}

				if(i > 0 && i <= 20)
				{
					j = 25 - i;
					f1 = p_70672_2_ * (float) j;
					p_70672_2_ = f1 / 25.0F;
				}

				return p_70672_2_;
			}
		}
	}
}
