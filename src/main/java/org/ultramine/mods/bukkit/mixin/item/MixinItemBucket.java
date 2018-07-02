package org.ultramine.mods.bukkit.mixin.item;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemBucket.class)
public abstract class MixinItemBucket extends Item
{
	@Shadow private Block isFull;

	@Shadow protected abstract ItemStack func_150910_a(ItemStack p_150910_1_, EntityPlayer p_150910_2_, Item p_150910_3_);

	@Shadow public abstract boolean tryPlaceContainedLiquid(World p_77875_1_, int p_77875_2_, int p_77875_3_, int p_77875_4_);

	/**
	 * @author AtomicInteger
	 */
	@Overwrite
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		boolean flag = this.isFull == Blocks.air;
		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, flag);
		if (movingobjectposition == null)
		{
			return itemStack;
		}
		else
		{
			FillBucketEvent event = new FillBucketEvent(player, itemStack, world, movingobjectposition);
			if (MinecraftForge.EVENT_BUS.post(event))
			{
				return itemStack;
			}
			else if (event.getResult() == Result.ALLOW)
			{
				if (player.capabilities.isCreativeMode)
				{
					return itemStack;
				}
				else if (--itemStack.stackSize <= 0)
				{
					return event.result;
				}
				else
				{
					if (!player.inventory.addItemStackToInventory(event.result))
						player.dropPlayerItemWithRandomChoice(event.result, false);
					return itemStack;
				}
			}
			else
			{
				if (movingobjectposition.typeOfHit == MovingObjectType.BLOCK)
				{
					int i = movingobjectposition.blockX;
					int j = movingobjectposition.blockY;
					int k = movingobjectposition.blockZ;
					if (!world.canMineBlock(player, i, j, k))
						return itemStack;
					if (flag)
					{
						if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack))
							return itemStack;
						Material material = world.getBlock(i, j, k).getMaterial();
						int l = world.getBlockMetadata(i, j, k);
						if (material == Material.water && l == 0)
						{
							PlayerBucketFillEvent fillEvent = CraftEventFactory.callPlayerBucketFillEvent(player, i, j, k, -1, itemStack, Items.water_bucket);
							if (fillEvent.isCancelled())
								return itemStack;
							world.setBlockToAir(i, j, k);
							return this.func_150910_a(itemStack, player, Items.water_bucket, fillEvent.getItemStack());
						}
						if (material == Material.lava && l == 0)
						{
							PlayerBucketFillEvent fillEvent = CraftEventFactory.callPlayerBucketFillEvent(player, i, j, k, -1, itemStack, Items.lava_bucket);
							if (fillEvent.isCancelled())
								return itemStack;
							world.setBlockToAir(i, j, k);
							return this.func_150910_a(itemStack, player, Items.lava_bucket, fillEvent.getItemStack());
						}
					}
					else
					{
						if (this.isFull == Blocks.air)
						{
							PlayerBucketEmptyEvent cbEvent = CraftEventFactory.callPlayerBucketEmptyEvent(player, i, j, k, movingobjectposition.sideHit, itemStack);
							if (cbEvent.isCancelled())
								return itemStack;
							return CraftItemStack.asNMSCopy(cbEvent.getItemStack());
						}
						int clickedX = i, clickedY = j, clickedZ = k;
						if (movingobjectposition.sideHit == 0)
							--j;
						if (movingobjectposition.sideHit == 1)
							++j;
						if (movingobjectposition.sideHit == 2)
							--k;
						if (movingobjectposition.sideHit == 3)
							++k;
						if (movingobjectposition.sideHit == 4)
							--i;
						if (movingobjectposition.sideHit == 5)
							++i;
						if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack))
							return itemStack;
						PlayerBucketEmptyEvent cbEvent = CraftEventFactory.callPlayerBucketEmptyEvent(player, clickedX, clickedY, clickedZ, movingobjectposition.sideHit, itemStack);
						if (cbEvent.isCancelled())
							return itemStack;
						if (this.tryPlaceContainedLiquid(world, i, j, k) && !player.capabilities.isCreativeMode)
							return CraftItemStack.asNMSCopy(cbEvent.getItemStack());
					}
				}
				return itemStack;
			}
		}
	}

	private ItemStack func_150910_a(ItemStack itemstack, EntityPlayer entityplayer, Item item, org.bukkit.inventory.ItemStack result)
	{
		if (entityplayer.capabilities.isCreativeMode)
		{
			return itemstack;
		}
		else if (--itemstack.stackSize <= 0)
		{
			return CraftItemStack.asNMSCopy(result);
		}
		else
		{
			if (!entityplayer.inventory.addItemStackToInventory(CraftItemStack.asNMSCopy(result)))
				entityplayer.dropPlayerItemWithRandomChoice(CraftItemStack.asNMSCopy(result), false);
			return itemstack;
		}
	}
}
