package org.ultramine.mods.bukkit.mixin.entity.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityFallingBlock.class)
public abstract class MixinEntityFallingBlock extends Entity
{
	public MixinEntityFallingBlock(World p_i1582_1_)
	{
		super(p_i1582_1_);
	}

	@Shadow private Block field_145811_e;
	@Shadow public int field_145814_a;
	@Shadow public int field_145812_b;
	@Shadow public boolean field_145813_c;
	@Shadow private boolean field_145808_f;
	@Shadow public NBTTagCompound field_145810_d;

	@Overwrite
	public void onUpdate()
	{
		if (this.field_145811_e.getMaterial() == Material.air)
		{
			this.setDead();
		}
		else
		{
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			++this.field_145812_b;
			this.motionY -= 0.03999999910593033D;
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.9800000190734863D;
			this.motionY *= 0.9800000190734863D;
			this.motionZ *= 0.9800000190734863D;
			if (!this.worldObj.isRemote)
			{
				int i = MathHelper.floor_double(this.posX);
				int j = MathHelper.floor_double(this.posY);
				int k = MathHelper.floor_double(this.posZ);
				if (this.field_145812_b == 1)
				{
					if (this.worldObj.getBlock(i, j, k) != this.field_145811_e || this.worldObj.getBlockMetadata(i, j, k) != this.field_145814_a || CraftEventFactory.callEntityChangeBlockEvent(this, i, j, k, Blocks.air, 0).isCancelled())
					{
						this.setDead();
						return;
					}
					this.worldObj.setBlockToAir(i, j, k);
				}
				if (this.onGround)
				{
					this.motionX *= 0.699999988079071D;
					this.motionZ *= 0.699999988079071D;
					this.motionY *= -0.5D;
					if (this.worldObj.getBlock(i, j, k) != Blocks.piston_extension)
					{
						this.setDead();
						if (!this.field_145808_f && this.worldObj.canPlaceEntityOnSide(this.field_145811_e, i, j, k, true, 1, null, null) && !BlockFalling.func_149831_e(this.worldObj, i, j - 1, k))
						{
							if (CraftEventFactory.callEntityChangeBlockEvent(this, i, j, k, this.field_145811_e, this.field_145814_a).isCancelled())
								return;
							this.worldObj.setBlock(i, j, k, this.field_145811_e, this.field_145814_a, 3);
							if (this.field_145811_e instanceof BlockFalling)
								((BlockFalling) this.field_145811_e).func_149828_a(this.worldObj, i, j, k, this.field_145814_a);
							if (this.field_145810_d != null && this.field_145811_e instanceof ITileEntityProvider)
							{
								TileEntity tileentity = this.worldObj.getTileEntity(i, j, k);
								if (tileentity != null)
								{
									NBTTagCompound nbttagcompound = new NBTTagCompound();
									tileentity.writeToNBT(nbttagcompound);
									for (Object o : this.field_145810_d.func_150296_c())
									{
										String s = (String) o;
										NBTBase nbtbase = this.field_145810_d.getTag(s);
										if (!s.equals("x") && !s.equals("y") && !s.equals("z"))
											nbttagcompound.setTag(s, nbtbase.copy());
									}
									tileentity.readFromNBT(nbttagcompound);
									tileentity.markDirty();
								}
							}
						}
						else if (this.field_145813_c && !this.field_145808_f)
						{
							this.entityDropItem(new ItemStack(this.field_145811_e, 1, this.field_145811_e.damageDropped(this.field_145814_a)), 0.0F);
						}
					}
				}
				else if (this.field_145812_b > 100 && !this.worldObj.isRemote && (j < 1 || j > 256) || this.field_145812_b > 600)
				{
					if (this.field_145813_c)
						this.entityDropItem(new ItemStack(this.field_145811_e, 1, this.field_145811_e.damageDropped(this.field_145814_a)), 0.0F);
					this.setDead();
				}
			}
		}
	}
}
