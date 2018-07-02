package org.ultramine.mods.bukkit.mixin.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

@Mixin(EntityAIEatGrass.class)
public class MixinEntityAIEatGrass
{
	@Shadow private EntityLiving field_151500_b;
	@Shadow private World field_151501_c;
	@Shadow int field_151502_a;

	@Overwrite
	public void updateTask()
	{
		this.field_151502_a = Math.max(0, this.field_151502_a - 1);
		if (this.field_151502_a == 4)
		{
			int i = MathHelper.floor_double(this.field_151500_b.posX);
			int j = MathHelper.floor_double(this.field_151500_b.posY);
			int k = MathHelper.floor_double(this.field_151500_b.posZ);
			if (this.field_151501_c.getBlock(i, j, k) == Blocks.tallgrass)
			{
				if (!CraftEventFactory.callEntityChangeBlockEvent(this.field_151500_b, ((IMixinWorld) this.field_151500_b.worldObj).getWorld().getBlockAt(i, j, k), Material.AIR, !this.field_151501_c.getGameRules().getGameRuleBooleanValue("mobGriefing")).isCancelled())
					this.field_151501_c.func_147480_a(i, j, k, false);
				this.field_151500_b.eatGrassBonus();
			}
			else if (this.field_151501_c.getBlock(i, j - 1, k) == Blocks.grass)
			{
				if (!CraftEventFactory.callEntityChangeBlockEvent(this.field_151500_b, ((IMixinWorld) this.field_151500_b.worldObj).getWorld().getBlockAt(i, j - 1, k), Material.DIRT, !this.field_151501_c.getGameRules().getGameRuleBooleanValue("mobGriefing")).isCancelled())
				{
					this.field_151501_c.playAuxSFX(2001, i, j - 1, k, Block.getIdFromBlock(Blocks.grass));
					this.field_151501_c.setBlock(i, j - 1, k, Blocks.dirt, 0, 2);
				}
				this.field_151500_b.eatGrassBonus();
			}
		}
	}
}
