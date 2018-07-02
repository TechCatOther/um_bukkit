package org.ultramine.mods.bukkit.mixin.entity.monster;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.monster.IMixinEntityCreeper;

@Mixin(net.minecraft.entity.monster.EntityCreeper.class)
public abstract class MixinEntityCreeper extends EntityMob implements IMixinEntityCreeper
{
	public MixinEntityCreeper(World w)
	{
		super(w);
	}

	@Shadow
	public abstract boolean getPowered();

	@Override
	public void setPowered(boolean powered)
	{
		if(!powered)
		{
			this.dataWatcher.updateObject(17, Byte.valueOf((byte) 0));
		}
		else
		{
			this.dataWatcher.updateObject(17, Byte.valueOf((byte) 1));
		}
	}

	@Overwrite
	public void onStruckByLightning(EntityLightningBolt ligtning)
	{
		boolean lastPowered = getPowered();
		super.onStruckByLightning(ligtning);

		if(ligtning != null)
		{
			setPowered(lastPowered);

			// CraftBukkit start
			if(CraftEventFactory.callCreeperPowerEvent(this, ligtning, org.bukkit.event.entity.CreeperPowerEvent.PowerCause.LIGHTNING).isCancelled())
			{
				return;
			}
		}

		this.setPowered(true);
	}
}
