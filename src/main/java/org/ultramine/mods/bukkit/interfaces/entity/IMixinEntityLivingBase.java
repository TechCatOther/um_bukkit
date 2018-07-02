package org.ultramine.mods.bukkit.interfaces.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.HashMap;

public interface IMixinEntityLivingBase extends IMixinEntity
{
	void heal(float value, EntityRegainHealthEvent.RegainReason regainReason);

	int getExpReward();

	int getExpToDrop();

	void setExpToDrop(int expToDrop);

	int getMaxAirTicks();

	void setMaxAirTicks(int maxAirTicks);

	float getLastDamage();

	void setLastDamage(float lastDamage);

	int getRecentlyHit();

	void setRecentlyHit(int recentlyHit);

	EntityPlayer getAttackingPlayer();

	HashMap<Integer, PotionEffect> getActivePotionsMap();

	float applyArmorCalculationsP(DamageSource source, float damage);

	float applyPotionDamageCalculationsP(DamageSource source, float damage);
}
