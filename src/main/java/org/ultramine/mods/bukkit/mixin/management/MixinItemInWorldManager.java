package org.ultramine.mods.bukkit.mixin.management;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.event.ForgeEventFactory;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinContainer;

@Mixin(net.minecraft.server.management.ItemInWorldManager.class)
public abstract class MixinItemInWorldManager
{
	@Shadow
	public World theWorld;
	@Shadow
	public EntityPlayerMP thisPlayerMP;
	@Shadow
	private WorldSettings.GameType gameType;
	@Shadow
	private boolean isDestroyingBlock;
	@Shadow
	private int initialDamage;
	@Shadow
	private int partiallyDestroyedBlockX;
	@Shadow
	private int partiallyDestroyedBlockY;
	@Shadow
	private int partiallyDestroyedBlockZ;
	@Shadow
	private int curblockDamage;
	@Shadow
	private boolean receivedFinishDiggingPacket;
	@Shadow
	private int posX;
	@Shadow
	private int posY;
	@Shadow
	private int posZ;
	@Shadow
	private int initialBlockDamage;
	@Shadow
	private int durabilityRemainingOnBlock;

	@Shadow
	public abstract boolean isCreative();

	@Shadow
	public abstract boolean tryUseItem(EntityPlayer p_73085_1_, World p_73085_2_, ItemStack p_73085_3_);

	@Shadow
	public abstract boolean tryHarvestBlock(int p_73084_1_, int p_73084_2_, int p_73084_3_);

	@Inject(method = "tryHarvestBlock", cancellable = true,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"))
	private void onTryHarvestBlock(int p_73084_1_, int p_73084_2_, int p_73084_3_, CallbackInfoReturnable<Boolean> ci)
	{
		Block block = this.theWorld.getBlock(p_73084_1_, p_73084_2_, p_73084_3_);

		if(block == Blocks.air)
		{
			ci.setReturnValue(false);
			return;    // CraftBukkit - A plugin set block to air without cancelling
		}
	}

	@Overwrite
	public void onBlockClicked(int p_73074_1_, int p_73074_2_, int p_73074_3_, int p_73074_4_)
	{
		// CraftBukkit start
		org.bukkit.event.player.PlayerInteractEvent cbEvent = CraftEventFactory.callPlayerInteractEvent(this.thisPlayerMP, Action.LEFT_CLICK_BLOCK, p_73074_1_, p_73074_2_, p_73074_3_, p_73074_4_, this.thisPlayerMP.inventory.getCurrentItem());

		if(!this.gameType.isAdventure() || this.thisPlayerMP.isCurrentToolAdventureModeExempt(p_73074_1_, p_73074_2_, p_73074_3_))
		{
			net.minecraftforge.event.entity.player.PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(this.thisPlayerMP, net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, p_73074_1_, p_73074_2_, p_73074_3_, p_73074_4_, theWorld); // Forge

			if(cbEvent.isCancelled() || event.isCanceled())
			{
				// Let the client know the block still exists
				this.thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(p_73074_1_, p_73074_2_, p_73074_3_, this.theWorld));
				// Update any tile entity data for this block
				TileEntity tileentity = this.theWorld.getTileEntity(p_73074_1_, p_73074_2_, p_73074_3_);

				if(tileentity != null)
				{
					Packet packet = tileentity.getDescriptionPacket();
					if(packet != null)
						this.thisPlayerMP.playerNetServerHandler.sendPacket(packet);
				}

				return;
			}

			// CraftBukkit end
			if(this.isCreative())
			{
				if(!this.theWorld.extinguishFire((EntityPlayer) null, p_73074_1_, p_73074_2_, p_73074_3_, p_73074_4_))
				{
					this.tryHarvestBlock(p_73074_1_, p_73074_2_, p_73074_3_);
				}
			}
			else
			{
				this.initialDamage = this.curblockDamage;
				float f = 1.0F;
				Block block = this.theWorld.getBlock(p_73074_1_, p_73074_2_, p_73074_3_);

				// CraftBukkit start - Swings at air do *NOT* exist.
				if(cbEvent.useInteractedBlock() == org.bukkit.event.Event.Result.DENY || event.useBlock == cpw.mods.fml.common.eventhandler.Event.Result.DENY)   // Cauldron
				{
					// If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
					if(block == Blocks.wooden_door)
					{
						// For some reason *BOTH* the bottom/top part have to be marked updated.
						boolean bottom = (this.theWorld.getBlockMetadata(p_73074_1_, p_73074_2_, p_73074_3_) & 8) == 0;
						((EntityPlayerMP) this.thisPlayerMP).playerNetServerHandler.sendPacket(new S23PacketBlockChange(p_73074_1_, p_73074_2_, p_73074_3_, this.theWorld));
						((EntityPlayerMP) this.thisPlayerMP).playerNetServerHandler.sendPacket(new S23PacketBlockChange(p_73074_1_, p_73074_2_ + (bottom ? 1 : -1), p_73074_3_, this.theWorld));
					}
					else if(block == Blocks.trapdoor)
					{
						((EntityPlayerMP) this.thisPlayerMP).playerNetServerHandler.sendPacket(new S23PacketBlockChange(p_73074_1_, p_73074_2_, p_73074_3_, this.theWorld));
					}
				}
				else if(!block.isAir(theWorld, p_73074_1_, p_73074_2_, p_73074_3_))
				{
					block.onBlockClicked(this.theWorld, p_73074_1_, p_73074_2_, p_73074_3_, this.thisPlayerMP);
					f = block.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, p_73074_1_, p_73074_2_, p_73074_3_);
					// Allow fire punching to be blocked
					this.theWorld.extinguishFire((EntityPlayer) null, p_73074_1_, p_73074_2_, p_73074_3_, p_73074_4_);
				}
				if(cbEvent.useItemInHand() == org.bukkit.event.Event.Result.DENY || event.useItem == cpw.mods.fml.common.eventhandler.Event.Result.DENY)   // Forge
				{
					// If we 'insta destroyed' then the client needs to be informed.
					if(f > 1.0f)
					{
						((EntityPlayerMP) this.thisPlayerMP).playerNetServerHandler.sendPacket(new S23PacketBlockChange(p_73074_1_, p_73074_2_, p_73074_3_, this.theWorld));
					}

					return;
				}

				org.bukkit.event.block.BlockDamageEvent blockEvent = CraftEventFactory.callBlockDamageEvent(this.thisPlayerMP, p_73074_1_, p_73074_2_, p_73074_3_, this.thisPlayerMP.inventory.getCurrentItem(), f >= 1.0f);

				if(blockEvent.isCancelled())
				{
					// Let the client know the block still exists
					((EntityPlayerMP) this.thisPlayerMP).playerNetServerHandler.sendPacket(new S23PacketBlockChange(p_73074_1_, p_73074_2_, p_73074_3_, this.theWorld));
					return;
				}

				if(blockEvent.getInstaBreak())
				{
					f = 2.0f;
				}

				// CraftBukkit end

				if(!block.isAir(theWorld, p_73074_1_, p_73074_2_, p_73074_3_) && f >= 1.0F)
				{
					this.tryHarvestBlock(p_73074_1_, p_73074_2_, p_73074_3_);
				}
				else
				{
					this.isDestroyingBlock = true;
					this.partiallyDestroyedBlockX = p_73074_1_;
					this.partiallyDestroyedBlockY = p_73074_2_;
					this.partiallyDestroyedBlockZ = p_73074_3_;
					int i1 = (int) (f * 10.0F);
					this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.getEntityId(), p_73074_1_, p_73074_2_, p_73074_3_, i1);
					this.durabilityRemainingOnBlock = i1;
				}
			}
		}
	}

	@Overwrite
	public boolean activateBlockOrUseItem(EntityPlayer p_73078_1_, World p_73078_2_, ItemStack p_73078_3_, int p_73078_4_, int p_73078_5_, int p_73078_6_, int p_73078_7_, float p_73078_8_, float p_73078_9_, float p_73078_10_)
	{
		// CraftBukkit start - Interact
		Block block = p_73078_2_.getBlock(p_73078_4_, p_73078_5_, p_73078_6_);
		boolean isAir = block.isAir(p_73078_2_, p_73078_4_, p_73078_5_, p_73078_6_); // Cauldron
		boolean denyResult = false, denyItem = false, denyBlock = false;

		if(!isAir)
		{
			org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(p_73078_1_, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, p_73078_4_, p_73078_5_, p_73078_6_, p_73078_7_, p_73078_3_);
			net.minecraftforge.event.entity.player.PlayerInteractEvent forgeEvent = ForgeEventFactory.onPlayerInteract(p_73078_1_, net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, p_73078_4_, p_73078_5_, p_73078_6_, p_73078_7_, p_73078_2_);
			// Cauldron start
			// if forge event is explicitly cancelled, return
			if(forgeEvent.isCanceled())
			{
				thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(p_73078_4_, p_73078_5_, p_73078_6_, theWorld));
				return false;
			}
			denyItem = event.useItemInHand() == org.bukkit.event.Event.Result.DENY || forgeEvent.useItem == cpw.mods.fml.common.eventhandler.Event.Result.DENY;
			denyBlock = event.useInteractedBlock() == org.bukkit.event.Event.Result.DENY || forgeEvent.useBlock == cpw.mods.fml.common.eventhandler.Event.Result.DENY;
			denyResult = denyItem || denyBlock;
			// if we have no explicit deny, check if item can be used
			if(!denyItem)
			{
				Item item = (p_73078_3_ != null ? p_73078_3_.getItem() : null);
				// try to use an item in hand before activating a block. Used for items such as IC2's wrench.
				if(item != null && item.onItemUseFirst(p_73078_3_, p_73078_1_, p_73078_2_, p_73078_4_, p_73078_5_, p_73078_6_, p_73078_7_, p_73078_8_, p_73078_9_, p_73078_10_))
				{
					if(p_73078_3_.stackSize <= 0) ForgeEventFactory.onPlayerDestroyItem(thisPlayerMP, p_73078_3_);
					return true;
				}
			}
			// Cauldron end
			if(denyBlock)
			{
				// If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
				if(block == Blocks.wooden_door)
				{
					boolean bottom = (p_73078_2_.getBlockMetadata(p_73078_4_, p_73078_5_, p_73078_6_) & 8) == 0;
					((EntityPlayerMP) p_73078_1_).playerNetServerHandler.sendPacket(new S23PacketBlockChange(p_73078_4_, p_73078_5_ + (bottom ? 1 : -1), p_73078_6_, p_73078_2_));
				}
			}
			else if(!p_73078_1_.isSneaking() || p_73078_3_ == null || p_73078_1_.getHeldItem().getItem().doesSneakBypassUse(p_73078_2_, p_73078_4_, p_73078_5_, p_73078_6_, p_73078_1_))
			{
				denyResult |= block.onBlockActivated(p_73078_2_, p_73078_4_, p_73078_5_, p_73078_6_, p_73078_1_, p_73078_7_, p_73078_8_, p_73078_9_, p_73078_10_);
				if (thisPlayerMP != null && !(thisPlayerMP.openContainer instanceof ContainerPlayer))
				{
					if (((IMixinContainer) thisPlayerMP.openContainer).getBukkitView() == null)
					{
						TileEntity te = thisPlayerMP.worldObj.getTileEntity(p_73078_4_, p_73078_5_, p_73078_6_);
						CraftEntity bukkitPlayer = ((IMixinEntity) thisPlayerMP).getBukkitEntity();
						if (te != null && te instanceof IInventory)
							((IMixinContainer) thisPlayerMP.openContainer).setBukkitView(new CraftInventoryView((HumanEntity) bukkitPlayer, new CraftInventory((IInventory) te), thisPlayerMP.openContainer));
						else
							((IMixinContainer) thisPlayerMP.openContainer).setBukkitView(new CraftInventoryView((HumanEntity) bukkitPlayer, Bukkit.createInventory((InventoryHolder) bukkitPlayer, InventoryType.CHEST), thisPlayerMP.openContainer));
					}
					thisPlayerMP.openContainer = CraftEventFactory.callInventoryOpenEvent(thisPlayerMP, thisPlayerMP.openContainer, false);
					if (thisPlayerMP.openContainer == null)
					{
						thisPlayerMP.openContainer = thisPlayerMP.inventoryContainer;
						return false;
					}
				}
			}

			if(p_73078_3_ != null && !denyResult && p_73078_3_.stackSize > 0)
			{
				int meta = p_73078_3_.getItemDamage();
				int size = p_73078_3_.stackSize;
				denyResult = p_73078_3_.tryPlaceItemIntoWorld(p_73078_1_, p_73078_2_, p_73078_4_, p_73078_5_, p_73078_6_, p_73078_7_, p_73078_8_, p_73078_9_, p_73078_10_);

				// The item count should not decrement in Creative mode.
				if(this.isCreative())
				{
					p_73078_3_.setItemDamage(meta);
					p_73078_3_.stackSize = size;
				}

				if(p_73078_3_.stackSize <= 0)
				{
					ForgeEventFactory.onPlayerDestroyItem(this.thisPlayerMP, p_73078_3_);
				}
			}

			// If we have 'true' and no explicit deny *or* an explicit allow -- run the item part of the hook
			if(p_73078_3_ != null && ((!denyResult && event.useItemInHand() != org.bukkit.event.Event.Result.DENY) || event.useItemInHand() == org.bukkit.event.Event.Result.ALLOW))
			{
				this.tryUseItem(p_73078_1_, p_73078_2_, p_73078_3_);
			}
		}

		return denyResult;
		// CraftBukkit end
	}
}
