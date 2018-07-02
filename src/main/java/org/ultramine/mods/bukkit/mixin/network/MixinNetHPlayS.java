package org.ultramine.mods.bukkit.mixin.network;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IntHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinContainer;
import org.ultramine.mods.bukkit.interfaces.network.IMixinNetHPS;
import org.ultramine.mods.bukkit.interfaces.network.ITimestampedPacket;
import org.ultramine.mods.bukkit.util.GenericFutureCloseChannel;

import java.util.ArrayList;
import java.util.HashSet;

@Mixin(net.minecraft.network.NetHandlerPlayServer.class)
public abstract class MixinNetHPlayS implements IMixinNetHPS
{
	@Shadow public EntityPlayerMP playerEntity;
	@Shadow private @Final MinecraftServer serverController;
	@Shadow public @Final NetworkManager netManager;

	@Shadow public abstract void sendPacket(final Packet p_147359_1_);
	@Shadow public abstract void setPlayerLocation(double x, double y, double z, float yaw, float pitch);

	// For the PacketPlayOutBlockPlace hack :(
	Long lastPacket;

	// Store the last block right clicked and what type it was
	private Item lastMaterial;

	// Cauldron - rename getPlayer -> getPlayerB() to disambiguate with FML's getPlayer() method of the same name (below)
	// Plugins calling this method will be remapped appropriately, but CraftBukkit code should be updated
	@Override
	public CraftPlayer getPlayerB()
	{
		return (this.playerEntity == null) ? null : (CraftPlayer) ((IMixinEntity) playerEntity).getBukkitEntity();
	}

	// CraftBukkit start - Add "isDisconnected" method
	public final boolean isDisconnected()
	{
		return !this.netManager.channel().config().isAutoRead();
	}

	// CraftBukkit end

	// Cauldron start
	public CraftServer getCraftServer()
	{
		return (CraftServer) Bukkit.getServer();
	}
	// Cauldron end

	@Inject(method = "processPlayerBlockPlacement", cancellable = true, at = @At("HEAD"))
	private void onProcessPlayerBlockPlacement(C08PacketPlayerBlockPlacement pct, CallbackInfo ci)
	{
		if(this.playerEntity.isDead)
		{
			ci.cancel();
			return;
		}

		// This is a horrible hack needed because the client sends 2 packets on 'right mouse click'
		// aimed at a block. We shouldn't need to get the second packet if the data is handled
		// but we cannot know what the client will do, so we might still get it
		//
		// If the time between packets is small enough, and the 'signature' similar, we discard the
		// second one. This sadly has to remain until Mojang makes their packets saner. :(
		//  -- Grum
		if(pct.func_149568_f() == 255)
		{
			if(pct.func_149574_g() != null && pct.func_149574_g().getItem() == this.lastMaterial && this.lastPacket != null
					&& ((ITimestampedPacket) pct).getTimestamp() - this.lastPacket < 100)
			{
				this.lastPacket = null;
				ci.cancel();
				return;
			}
		}
		else
		{
			this.lastMaterial = pct.func_149574_g() == null ? null : pct.func_149574_g().getItem();
			this.lastPacket = ((ITimestampedPacket) pct).getTimestamp();
		}
	}

	@Inject(method = "processHeldItemChange", cancellable = true,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C09PacketHeldItemChange;func_149614_c()I"))
	public void onProcessHeldItemChange(C09PacketHeldItemChange p_147355_1_, CallbackInfo ci)
	{
		if(this.playerEntity.isDead)
		{
			ci.cancel();
			return;
		}

		PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getPlayerB(), this.playerEntity.inventory.currentItem, p_147355_1_.func_149614_c());
		Bukkit.getServer().getPluginManager().callEvent(event);

		if(event.isCancelled())
		{
			this.sendPacket(new S09PacketHeldItemChange(this.playerEntity.inventory.currentItem));
			this.playerEntity.func_143004_u();
			ci.cancel();
			return;
		}
	}

	@Override
	public void teleport(Location dest)
	{
		setPlayerLocation(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch());
	}

	@Inject(method = "processUpdateSign", cancellable = true,
			at = @At(value = "INVOKE", target = "Ljava/lang/System;arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V"))
	public void onProcessUpdateSign(C12PacketUpdateSign p_147343_1_, CallbackInfo ci)
	{
		int x = p_147343_1_.func_149588_c();
		int y = p_147343_1_.func_149586_d();
		int z = p_147343_1_.func_149585_e();
		TileEntitySign tileentitysign1 = (TileEntitySign) playerEntity.worldObj.getTileEntity(x, y, z);
		// CraftBukkit start
		Player player = getPlayerB();
		SignChangeEvent event = new SignChangeEvent(player.getWorld().getBlockAt(x, y, z), player, p_147343_1_.func_149589_f());
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled())
		{
			for (int l = 0; l < 4; ++l)
			{
				tileentitysign1.signText[l] = event.getLine(l);

				if (tileentitysign1.signText[l] == null)
				{
					tileentitysign1.signText[l] = "";
				}
			}

//			tileentitysign1.field_145916_j = false;
		}

		// System.arraycopy(p_147343_1_.func_149589_f(), 0, tileentitysign1.signText, 0, 4);
		// CraftBukkit end
		tileentitysign1.markDirty();
		playerEntity.worldObj.markBlockForUpdate(x, y, z);

		ci.cancel();
	}

	@Inject(method = "processVanilla250Packet", at = @At("HEAD"))
	public void onProcessVanilla250Packet(C17PacketCustomPayload p_147349_1_, CallbackInfo ci)
	{
		// CraftBukkit start
		// Cauldron - bukkit registration moved to FML's ChannelRegistrationHandler
		if(p_147349_1_.func_149558_e() != null)
			Bukkit.getServer().getMessenger().dispatchIncomingMessage(getPlayerB(), p_147349_1_.func_149559_c(), p_147349_1_.func_149558_e());
		// CraftBukkit end
	}

	private final static HashSet<Integer> invalidItems = new HashSet<Integer>(java.util.Arrays.asList(8, 9, 10, 11, 26, 34,
			36, 43, 51, 52, 55, 59, 60, 62, 63, 64, 68, 71, 74, 75, 83, 90, 92, 93, 94, 104, 105, 115, 117, 118, 119,
			125, 127, 132, 140, 141, 142, 144));

	@Shadow private int field_147375_m;

	@Overwrite
	public void processCreativeInventoryAction(C10PacketCreativeInventoryAction creativeActionPacket)
	{
		if (this.playerEntity.theItemInWorldManager.isCreative())
		{
			boolean flag = creativeActionPacket.func_149627_c() < 0;
			ItemStack itemstack = creativeActionPacket.func_149625_d();
			boolean flag1 = creativeActionPacket.func_149627_c() >= 1 && creativeActionPacket.func_149627_c() < 36 + InventoryPlayer.getHotbarSize();
			boolean flag2 = itemstack == null || itemstack.getItem() != null && !invalidItems.contains(Item.getIdFromItem(itemstack.getItem()));
			boolean flag3 = itemstack == null || itemstack.getItemDamage() >= 0 && itemstack.stackSize <= 64 && itemstack.stackSize > 0;
			if (flag || (flag1 && !ItemStack.areItemStacksEqual(this.playerEntity.inventoryContainer.getSlot(creativeActionPacket.func_149627_c()).getStack(),
					creativeActionPacket.func_149625_d()))) // Insist on valid slot
			{
				org.bukkit.entity.HumanEntity player = (HumanEntity) ((IMixinEntity) this.playerEntity).getBukkitEntity();
				InventoryView inventory = new CraftInventoryView(player, player.getInventory(), this.playerEntity.inventoryContainer);
				org.bukkit.inventory.ItemStack item = CraftItemStack.asBukkitCopy(creativeActionPacket.func_149625_d()); // Should be packet107setcreativeslot.newitem
				SlotType type = SlotType.QUICKBAR;
				if (flag)
					type = SlotType.OUTSIDE;
				else if (creativeActionPacket.func_149627_c() < 36)
					if (creativeActionPacket.func_149627_c() >= 5 && creativeActionPacket.func_149627_c() < 9)
						type = SlotType.ARMOR;
					else
						type = SlotType.CONTAINER;
				InventoryCreativeEvent event = new InventoryCreativeEvent(inventory, type, flag ? -999 : creativeActionPacket.func_149627_c(), item);
				Bukkit.getServer().getPluginManager().callEvent(event);
				itemstack = CraftItemStack.asNMSCopy(event.getCursor());
				switch (event.getResult())
				{
					case ALLOW:
						// Plugin cleared the id / stacksize checks
						flag2 = flag3 = true;
						break;
					case DEFAULT:
						break;
					case DENY:
						// Reset the slot
						if (creativeActionPacket.func_149627_c() >= 0)
						{
							this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(this.playerEntity.inventoryContainer.windowId, creativeActionPacket
									.func_149627_c(), this.playerEntity.inventoryContainer.getSlot(creativeActionPacket.func_149627_c()).getStack()));
							this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, null));
						}
						return;
				}
			}
			if (flag1 && flag2 && flag3)
			{
				if (itemstack == null)
					this.playerEntity.inventoryContainer.putStackInSlot(creativeActionPacket.func_149627_c(), null);
				else
					this.playerEntity.inventoryContainer.putStackInSlot(creativeActionPacket.func_149627_c(), itemstack);
				this.playerEntity.inventoryContainer.setPlayerIsPresent(this.playerEntity, true);
			}
			else if (flag && flag2 && flag3 && this.field_147375_m < 200)
			{
				this.field_147375_m += 20;
				EntityItem entityitem = this.playerEntity.dropPlayerItemWithRandomChoice(itemstack, true);
				if (entityitem != null)
					entityitem.setAgeToCreativeDespawnTime();
			}
		}

	}

	@Inject(method = "processCloseWindow", at = @At(value = "INVOKE"))
	public void processCloseWindowInject(C0DPacketCloseWindow p_147356_1_, CallbackInfo ci)
	{
		if (this.playerEntity.isDead)
			return;
		if (((IMixinContainer) this.playerEntity.openContainer).getBukkitView() != null)
			CraftEventFactory.handleInventoryCloseEvent(this.playerEntity); // CraftBukkit
	}

	@Shadow private IntHashMap field_147372_n;

	@Overwrite
	public void processClickWindow(C0EPacketClickWindow p_147351_1_)
	{
		this.playerEntity.func_143004_u();
		if (this.playerEntity.openContainer.windowId == p_147351_1_.func_149548_c() && this.playerEntity.openContainer.isPlayerNotUsingContainer(this.playerEntity))
		{
			// CraftBukkit start - Call InventoryClickEvent
			if (p_147351_1_.func_149544_d() < -1 && p_147351_1_.func_149544_d() != -999)
				return;
			InventoryView inventory = ((IMixinContainer) this.playerEntity.openContainer).getBukkitView();
			SlotType type = CraftInventoryView.getSlotType(inventory, p_147351_1_.func_149544_d());
			InventoryClickEvent event;
			ClickType click = ClickType.UNKNOWN;
			InventoryAction action = InventoryAction.UNKNOWN;
			ItemStack itemstack = null;
			// Cauldron start - some containers such as NEI's Creative Container does not have a view at this point so we need to create one
			if (inventory == null)
			{
				inventory = new CraftInventoryView((HumanEntity) ((IMixinEntity) this.playerEntity).getBukkitEntity(), Bukkit.getServer().createInventory(
						(InventoryHolder) ((IMixinEntity) this.playerEntity).getBukkitEntity(), InventoryType.CHEST), this.playerEntity.openContainer);
//			                this.playerEntity.openContainer.bukkitView = inventory;
			}
			// Cauldron end
			if (p_147351_1_.func_149544_d() == -1)
			{
				type = SlotType.OUTSIDE; // override
				click = p_147351_1_.func_149543_e() == 0 ? ClickType.WINDOW_BORDER_LEFT : ClickType.WINDOW_BORDER_RIGHT;
				action = InventoryAction.NOTHING;
			}
			else if (p_147351_1_.func_149542_h() == 0)
			{
				if (p_147351_1_.func_149543_e() == 0 || p_147351_1_.func_149543_e() == 1)
				{
					click = p_147351_1_.func_149543_e() == 0 ? ClickType.LEFT : ClickType.RIGHT;
					action = InventoryAction.NOTHING; // Don't want to repeat ourselves
					if (p_147351_1_.func_149544_d() == -999)
					{
						if (playerEntity.inventory.getItemStack() != null)
							action = p_147351_1_.func_149543_e() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
					}
					else
					{
						Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());
						if (slot != null)
						{
							ItemStack clickedItem = slot.getStack();
							ItemStack cursor = playerEntity.inventory.getItemStack();
							if (clickedItem == null)
							{
								if (cursor != null)
									action = p_147351_1_.func_149543_e() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
							}
							else if (slot.canTakeStack(playerEntity)) // Should be Slot.isPlayerAllowed
							{
								if (cursor == null)
								{
									action = p_147351_1_.func_149543_e() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
								}
								else if (slot.isItemValid(cursor)) // Should be Slot.isItemAllowed
								{
									if (clickedItem.isItemEqual(cursor) && ItemStack.areItemStackTagsEqual(clickedItem, cursor))
									{
										int toPlace = p_147351_1_.func_149543_e() == 0 ? cursor.stackSize : 1;
										toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.stackSize);
										toPlace = Math.min(toPlace, slot.inventory.getInventoryStackLimit() - clickedItem.stackSize);
										if (toPlace == 1)
											action = InventoryAction.PLACE_ONE;
										else if (toPlace == cursor.stackSize)
											action = InventoryAction.PLACE_ALL;
										else if (toPlace < 0)
											action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE; // this happens with oversized stacks
										else if (toPlace != 0)
											action = InventoryAction.PLACE_SOME;
									}
									else if (cursor.stackSize <= slot.getSlotStackLimit()) // Should be Slot.getMaxStackSize()
									{
										action = InventoryAction.SWAP_WITH_CURSOR;
									}
								}
								else if (cursor.getItem() == clickedItem.getItem() && (!cursor.getHasSubtypes() || cursor.getItemDamage() == clickedItem.getItemDamage()) && ItemStack.areItemStackTagsEqual(cursor, clickedItem))
								{
									if (clickedItem.stackSize >= 0)
										if (clickedItem.stackSize + cursor.stackSize <= cursor.getMaxStackSize())
											// As of 1.5, this is result slots only
											action = InventoryAction.PICKUP_ALL;
								}
							}
						}
					}
				}
			}
			else if (p_147351_1_.func_149542_h() == 1)
			{
				if (p_147351_1_.func_149543_e() == 0)
					click = ClickType.SHIFT_LEFT;
				else if (p_147351_1_.func_149543_e() == 1)
					click = ClickType.SHIFT_RIGHT;
				if (p_147351_1_.func_149543_e() == 0 || p_147351_1_.func_149543_e() == 1)
				{
					if (p_147351_1_.func_149544_d() < 0)
					{
						action = InventoryAction.NOTHING;
					}
					else
					{
						Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());
						if (slot != null && slot.canTakeStack(this.playerEntity) && slot.getHasStack()) // Should be Slot.hasItem()
							action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
						else
							action = InventoryAction.NOTHING;
					}
				}
			}
			else if (p_147351_1_.func_149542_h() == 2)
			{
				if (p_147351_1_.func_149543_e() >= 0 && p_147351_1_.func_149543_e() < 9)
				{
					click = ClickType.NUMBER_KEY;
					Slot clickedSlot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());
					if (clickedSlot.canTakeStack(playerEntity))
					{
						ItemStack hotbar = this.playerEntity.inventory.getStackInSlot(p_147351_1_.func_149543_e());
						boolean canCleanSwap = hotbar == null || (clickedSlot.inventory == playerEntity.inventory && clickedSlot.isItemValid(hotbar)); // the slot will accept the hotbar item
						if (clickedSlot.getHasStack())
						{
							if (canCleanSwap)
							{
								action = InventoryAction.HOTBAR_SWAP;
							}
							else
							{
								int firstEmptySlot = playerEntity.inventory.getFirstEmptyStack(); // Should be Inventory.firstEmpty()
								if (firstEmptySlot > -1)
									action = InventoryAction.HOTBAR_MOVE_AND_READD;
								else
									action = InventoryAction.NOTHING; // This is not sane! Mojang: You should test for other slots of same type
							}
						}
						else if (!clickedSlot.getHasStack() && hotbar != null && clickedSlot.isItemValid(hotbar))
						{
							action = InventoryAction.HOTBAR_SWAP;
						}
						else
						{
							action = InventoryAction.NOTHING;
						}
					}
					else
					{
						action = InventoryAction.NOTHING;
					}
					// Special constructor for number key
					event = new InventoryClickEvent(inventory, type, p_147351_1_.func_149544_d(), click, action, p_147351_1_.func_149543_e());
				}
			}
			else if (p_147351_1_.func_149542_h() == 3)
			{
				if (p_147351_1_.func_149543_e() == 2)
				{
					click = ClickType.MIDDLE;
					if (p_147351_1_.func_149544_d() == -999)
					{
						action = InventoryAction.NOTHING;
					}
					else
					{
						Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());
						if (slot != null && slot.getHasStack() && playerEntity.capabilities.isCreativeMode && playerEntity.inventory.getItemStack() == null)
							action = InventoryAction.CLONE_STACK;
						else
							action = InventoryAction.NOTHING;
					}
				}
				else
				{
					click = ClickType.UNKNOWN;
					action = InventoryAction.UNKNOWN;
				}
			}
			else if (p_147351_1_.func_149542_h() == 4)
			{
				if (p_147351_1_.func_149544_d() >= 0)
				{
					if (p_147351_1_.func_149543_e() == 0)
					{
						click = ClickType.DROP;
						Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());
						if (slot != null && slot.getHasStack() && slot.canTakeStack(playerEntity) && slot.getStack() != null && slot.getStack().getItem() != Item.getItemFromBlock(Blocks.air))
							action = InventoryAction.DROP_ONE_SLOT;
						else
							action = InventoryAction.NOTHING;
					}
					else if (p_147351_1_.func_149543_e() == 1)
					{
						click = ClickType.CONTROL_DROP;
						Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());
						if (slot != null && slot.getHasStack() && slot.canTakeStack(playerEntity) && slot.getStack() != null && slot.getStack().getItem() != Item.getItemFromBlock(Blocks.air))
							action = InventoryAction.DROP_ALL_SLOT;
						else
							action = InventoryAction.NOTHING;
					}
				}
				else
				{
					// Sane default (because this happens when they are holding nothing. Don't ask why.)
					click = ClickType.LEFT;
					if (p_147351_1_.func_149543_e() == 1)
						click = ClickType.RIGHT;
					action = InventoryAction.NOTHING;
				}
			}
			else if (p_147351_1_.func_149542_h() == 5)
			{
				itemstack = this.playerEntity.openContainer.slotClick(p_147351_1_.func_149544_d(), p_147351_1_.func_149543_e(), 5, this.playerEntity);
			}
			else if (p_147351_1_.func_149542_h() == 6)
			{
				click = ClickType.DOUBLE_CLICK;
				action = InventoryAction.NOTHING;
				if (p_147351_1_.func_149544_d() >= 0 && this.playerEntity.inventory.getItemStack() != null)
				{
					ItemStack cursor = this.playerEntity.inventory.getItemStack();
					action = InventoryAction.NOTHING;
					// Quick check for if we have any of the item
					// Cauldron start - can't call getContents() on modded IInventory; CB-added method
						if (inventory.getTopInventory().contains(org.bukkit.Material.getMaterial(Item.getIdFromItem(cursor.getItem())))
								|| inventory.getBottomInventory().contains(org.bukkit.Material.getMaterial(Item.getIdFromItem(cursor.getItem()))))
						{
							action = InventoryAction.COLLECT_TO_CURSOR;
						}
					// Cauldron end
				}
			}
			// TODO check on updates
			if (p_147351_1_.func_149542_h() != 5)
			{
				if (click == ClickType.NUMBER_KEY)
					event = new InventoryClickEvent(inventory, type, p_147351_1_.func_149544_d(), click, action, p_147351_1_.func_149543_e());
				else
					event = new InventoryClickEvent(inventory, type, p_147351_1_.func_149544_d(), click, action);
				org.bukkit.inventory.Inventory top = inventory.getTopInventory();
				if (p_147351_1_.func_149544_d() == 0 && top instanceof CraftingInventory)
				{
					// Cauldron start - vanilla compatibility (mod recipes)
					org.bukkit.inventory.Recipe recipe = null;
					recipe = ((CraftingInventory) top).getRecipe();
					// Cauldron end

					if (recipe != null)
						if (click == ClickType.NUMBER_KEY)
							event = new CraftItemEvent(recipe, inventory, type, p_147351_1_.func_149544_d(), click, action, p_147351_1_.func_149543_e());
						else
							event = new CraftItemEvent(recipe, inventory, type, p_147351_1_.func_149544_d(), click, action);
				}
				Bukkit.getServer().getPluginManager().callEvent(event);
				switch (event.getResult())
				{
					case ALLOW:
					case DEFAULT:
						itemstack = this.playerEntity.openContainer.slotClick(p_147351_1_.func_149544_d(), p_147351_1_.func_149543_e(),
								p_147351_1_.func_149542_h(), this.playerEntity);
						break;
					case DENY:
						switch (action)
						{
							// Modified other slots
							case PICKUP_ALL:
							case MOVE_TO_OTHER_INVENTORY:
							case HOTBAR_MOVE_AND_READD:
							case HOTBAR_SWAP:
							case COLLECT_TO_CURSOR:
							case UNKNOWN:
								this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
								break;
							// Modified cursor and clicked
							case PICKUP_SOME:
							case PICKUP_HALF:
							case PICKUP_ONE:
							case PLACE_ALL:
							case PLACE_SOME:
							case PLACE_ONE:
							case SWAP_WITH_CURSOR:
								this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, this.playerEntity.inventory.getItemStack()));
								this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(this.playerEntity.openContainer.windowId, p_147351_1_
										.func_149544_d(), this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d()).getStack()));
								break;
							// Modified clicked only
							case DROP_ALL_SLOT:
							case DROP_ONE_SLOT:
								this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(this.playerEntity.openContainer.windowId, p_147351_1_
										.func_149544_d(), this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d()).getStack()));
								break;
							// Modified cursor only
							case DROP_ALL_CURSOR:
							case DROP_ONE_CURSOR:
							case CLONE_STACK:
								this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, this.playerEntity.inventory.getItemStack()));
								break;
							// Nothing
							case NOTHING:
								break;
						}
						return;
				}
			}
			if (ItemStack.areItemStacksEqual(p_147351_1_.func_149546_g(), itemstack))
			{
				this.playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(p_147351_1_.func_149548_c(), p_147351_1_.func_149547_f(), true));
				this.playerEntity.isChangingQuantityOnly = true;
				this.playerEntity.openContainer.detectAndSendChanges();
				this.playerEntity.updateHeldItem();
				this.playerEntity.isChangingQuantityOnly = false;
			}
			else
			{
				this.field_147372_n.addKey(this.playerEntity.openContainer.windowId, p_147351_1_.func_149547_f());
				this.playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(p_147351_1_.func_149548_c(), p_147351_1_.func_149547_f(), false));
				this.playerEntity.openContainer.setPlayerIsPresent(this.playerEntity, false);
				ArrayList<ItemStack> arraylist = new ArrayList<ItemStack>();
				for (int i = 0; i < this.playerEntity.openContainer.inventorySlots.size(); ++i)
					arraylist.add(((Slot) this.playerEntity.openContainer.inventorySlots.get(i)).getStack());
				this.playerEntity.sendContainerAndContentsToPlayer(this.playerEntity.openContainer, arraylist);
			}
		}
	}

	@Overwrite
	public void kickPlayerFromServer(String kickReason)
	{
		String leaveMessage = EnumChatFormatting.YELLOW + this.playerEntity.getCommandSenderName() + " left the game.";
		PlayerKickEvent event = new PlayerKickEvent(getPlayerB(), kickReason, leaveMessage);
		if (serverController.isServerRunning())
			Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;
		// TODO: Make PlayerKickEvent#getLeaveMessage() useful.
		final ChatComponentText chatcomponenttext = new ChatComponentText(event.getReason());
		this.netManager.scheduleOutboundPacket(new S40PacketDisconnect(chatcomponenttext), new GenericFutureCloseChannel(this.netManager, chatcomponenttext));
		this.netManager.disableAutoRead();
	}
}
