package org.ultramine.mods.bukkit.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;

import java.util.ArrayList;
import java.util.Arrays;

public class ArmorPropertiesUM
{
	public static float ApplyArmor(EntityLivingBase entity, ItemStack[] inventory, DamageSource source, double damage, boolean applyDamage)
	{
		damage *= 25;
		ArrayList<ArmorProperties> dmgVals = new ArrayList<ArmorProperties>();
		for(int x = 0; x < inventory.length; x++)
		{
			ItemStack stack = inventory[x];
			if(stack == null)
			{
				continue;
			}
			ArmorProperties prop = null;
			if(stack.getItem() instanceof ISpecialArmor)
			{
				ISpecialArmor armor = (ISpecialArmor) stack.getItem();
				prop = armor.getProperties(entity, stack, source, damage / 25D, x).copy();
			}
			else if(stack.getItem() instanceof ItemArmor && !source.isUnblockable())
			{
				ItemArmor armor = (ItemArmor) stack.getItem();
				prop = new ArmorProperties(0, armor.damageReduceAmount / 25D, armor.getMaxDamage() + 1 - stack.getItemDamage());
			}
			if(prop != null)
			{
				prop.Slot = x;
				dmgVals.add(prop);
			}
		}
		if(dmgVals.size() > 0)
		{
			ArmorProperties[] props = dmgVals.toArray(new ArmorProperties[dmgVals.size()]);
			StandardizeList(props, damage);
			int level = props[0].Priority;
			double ratio = 0;
			for(ArmorProperties prop : props)
			{
				if(level != prop.Priority)
				{
					damage -= (damage * ratio);
					ratio = 0;
					level = prop.Priority;
				}
				ratio += prop.AbsorbRatio;

				double absorb = damage * prop.AbsorbRatio;
				if(absorb > 0)
				{
					ItemStack stack = inventory[prop.Slot];
					int itemDamage = (int) (absorb / 25D < 1 ? 1 : absorb / 25D);
					if(stack.getItem() instanceof ISpecialArmor)
					{
						if(applyDamage)
						{
							((ISpecialArmor) stack.getItem()).damageArmor(entity, stack, source, itemDamage, prop.Slot);
						}
					}
					else if(applyDamage)
					{
						stack.damageItem(itemDamage, entity);
					}
					if(stack.stackSize <= 0)
					{
						/*if (entity instanceof EntityPlayer)
						{
							stack.onItemDestroyedByUse((EntityPlayer)entity);
						}*/
						inventory[prop.Slot] = null;
					}
				}
			}
			damage -= (damage * ratio);
		}
		return (float) (damage / 25.0F);
	}

	private static void StandardizeList(ArmorProperties[] armor, double damage)
	{
		Arrays.sort(armor);

		int start = 0;
		double total = 0;
		int priority = armor[0].Priority;
		int pStart = 0;
		boolean pChange = false;
		boolean pFinished = false;

		for(int x = 0; x < armor.length; x++)
		{
			total += armor[x].AbsorbRatio;
			if(x == armor.length - 1 || armor[x].Priority != priority)
			{
				if(armor[x].Priority != priority)
				{
					total -= armor[x].AbsorbRatio;
					x--;
					pChange = true;
				}
				if(total > 1)
				{
					for(int y = start; y <= x; y++)
					{
						double newRatio = armor[y].AbsorbRatio / total;
						if(newRatio * damage > armor[y].AbsorbMax)
						{
							armor[y].AbsorbRatio = (double) armor[y].AbsorbMax / damage;
							total = 0;
							for(int z = pStart; z <= y; z++)
							{
								total += armor[z].AbsorbRatio;
							}
							start = y + 1;
							x = y;
							break;
						}
						else
						{
							armor[y].AbsorbRatio = newRatio;
							pFinished = true;
						}
					}
					if(pChange && pFinished)
					{
						damage -= (damage * total);
						total = 0;
						start = x + 1;
						priority = armor[start].Priority;
						pStart = start;
						pChange = false;
						pFinished = false;
						if(damage <= 0)
						{
							for(int y = x + 1; y < armor.length; y++)
							{
								armor[y].AbsorbRatio = 0;
							}
							break;
						}
					}
				}
				else
				{
					for(int y = start; y <= x; y++)
					{
						total -= armor[y].AbsorbRatio;
						if(damage * armor[y].AbsorbRatio > armor[y].AbsorbMax)
						{
							armor[y].AbsorbRatio = (double) armor[y].AbsorbMax / damage;
						}
						total += armor[y].AbsorbRatio;
					}
					damage -= (damage * total);
					total = 0;
					if(x != armor.length - 1)
					{
						start = x + 1;
						priority = armor[start].Priority;
						pStart = start;
						pChange = false;
						if(damage <= 0)
						{
							for(int y = x + 1; y < armor.length; y++)
							{
								armor[y].AbsorbRatio = 0;
							}
							break;
						}
					}
				}
			}
		}
	}
}
