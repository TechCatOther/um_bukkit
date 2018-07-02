package org.ultramine.mods.bukkit.mixin.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityFurnace;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.ultramine.mods.bukkit.interfaces.entity.player.IMixinPlayer;
import org.ultramine.mods.bukkit.interfaces.inventory.IInventoryTransactionProvider;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinContainer;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.minecraft.inventory.Container.func_94525_a;
import static net.minecraft.inventory.Container.func_94527_a;
import static net.minecraft.inventory.Container.func_94528_d;
import static net.minecraft.inventory.Container.func_94529_b;
import static net.minecraft.inventory.Container.func_94532_c;

@Mixin(Container.class)
public abstract class MixinContainer implements IMixinContainer
{
	@Shadow public List<Slot> inventorySlots;
	@Shadow protected List<ICrafting> crafters;

	private boolean isOpened = false;
	private boolean isClosedByEventCancelling = false;

	private InventoryView bukkitView;
	private boolean isBukkitViewCreated;

	@Override
	@Nullable
	public InventoryView getBukkitView()
	{
		if(!isBukkitViewCreated)
		{
			isBukkitViewCreated = true;
			bukkitView = computeBukkitView();
			return bukkitView;
		}

		return bukkitView; // nullable here
	}

	@Override
	public void setBukkitView(InventoryView bukkitView)
	{
		this.bukkitView = bukkitView;
		isBukkitViewCreated = true;
	}

	private InventoryView computeBukkitView()
	{
		Container container = (Container) (Object) this;

		Set<IInventory> uniqueInventorySet = new HashSet<IInventory>();
		for(Object o : inventorySlots)
			uniqueInventorySet.add(((Slot) o).inventory);
		List<IInventory> invs = new ArrayList<IInventory>(uniqueInventorySet);

		InventoryPlayer playerInv = null;

		for(Iterator<IInventory> it = invs.iterator(); it.hasNext();)
		{
			IInventory inv = it.next();
			if(inv instanceof InventoryPlayer)
			{
				InventoryPlayer foundPlayerInv = (InventoryPlayer) inv;
				//noinspection SuspiciousMethodCalls
//				if(crafters.contains(foundPlayerInv.player))
				{
					playerInv = foundPlayerInv;
					it.remove();
					break;
				}
			}
		}

		if(playerInv == null)
			return null;

		Inventory craftInv = null;
		if(invs.size() == 1)
		{
			IInventory firstInv = invs.get(0);
			if(container instanceof ContainerEnchantment)
				craftInv = new org.bukkit.craftbukkit.inventory.CraftInventoryEnchanting(firstInv);
			else if (firstInv instanceof InventoryPlayer)
				craftInv = new org.bukkit.craftbukkit.inventory.CraftInventoryPlayer((CraftHumanEntity) ((IMixinPlayer) ((InventoryPlayer) firstInv).player).getBukkitEntity());
			else if (firstInv instanceof InventoryLargeChest)
				craftInv = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) firstInv);
			else if(firstInv instanceof TileEntityBeacon)
				craftInv = new org.bukkit.craftbukkit.inventory.CraftInventoryBeacon((TileEntityBeacon) firstInv);
			else if(firstInv instanceof TileEntityBrewingStand)
				craftInv = new org.bukkit.craftbukkit.inventory.CraftInventoryBrewer((TileEntityBrewingStand) firstInv);
			else if(firstInv instanceof TileEntityFurnace)
				craftInv = new org.bukkit.craftbukkit.inventory.CraftInventoryFurnace((TileEntityFurnace) firstInv);
		}
		else if(invs.size() == 2)
		{
			InventoryCraftResult result = findInstance(invs, InventoryCraftResult.class);
			if(result != null)
			{
				InventoryCrafting crafting = findInstance(invs, InventoryCrafting.class);
				if(crafting != null)
				{
					craftInv = new org.bukkit.craftbukkit.inventory.CraftInventoryCrafting(crafting, result);
				}
				else
				{
					InventoryBasic basic = findInstance(invs, InventoryBasic.class);
					if(basic != null && "Repair".equals(basic.getInventoryName()))
						craftInv = new org.bukkit.craftbukkit.inventory.CraftInventoryAnvil(basic, result);
				}
			}
		}

		CraftPlayer bukkitPlayer = (CraftPlayer) ((IMixinPlayer) playerInv.player).getBukkitEntity();

		if(craftInv == null)
		{
			if(invs.size() != 1)
				craftInv = Bukkit.getServer().createInventory(bukkitPlayer, InventoryType.CHEST);
			else
				craftInv = new CraftInventory(invs.get(0));
		}

		return new CraftInventoryView(bukkitPlayer, craftInv, container);
	}

	@Nullable
	private static <T> T findInstance(List<?> list, Class<T> type)
	{
		for(Object o : list)
			if(type.isInstance(o))
				return type.cast(o);
		return null;
	}

	@Override
	public void transferTo(Container other, CraftHumanEntity player)
	{
		InventoryView source = player.getOpenInventory(), destination = ((IMixinContainer) other).getBukkitView();
		if (source != null)
		{
			if (source.getTopInventory() instanceof CraftInventory)
			{
				IInventory topInventory = ((CraftInventory) source.getTopInventory()).getInventory();
				if (topInventory instanceof IInventoryTransactionProvider)
				{
					IInventoryTransactionProvider topInventoryProvider = (IInventoryTransactionProvider) topInventory;
					if (topInventoryProvider.getViewers().contains(player))
						topInventoryProvider.onClose(player);
				}
			}
			else
			{
				if (source.getTopInventory() instanceof IInventoryTransactionProvider)
				{
					IInventoryTransactionProvider topInventoryProvider = (IInventoryTransactionProvider) source.getTopInventory();
					if (topInventoryProvider.getViewers().contains(player))
						topInventoryProvider.onClose(player);
				}
			}
			if (source.getBottomInventory() instanceof CraftInventory)
			{
				IInventory bottomInventory = ((CraftInventory) source.getBottomInventory()).getInventory();
				if (bottomInventory instanceof IInventoryTransactionProvider)
				{
					IInventoryTransactionProvider bottomInventoryProvider = (IInventoryTransactionProvider) bottomInventory;
					if (bottomInventoryProvider.getViewers().contains(player))
						bottomInventoryProvider.onClose(player);
				}
			}
			else
			{
				if (source.getBottomInventory() instanceof IInventoryTransactionProvider)
				{
					IInventoryTransactionProvider bottomInventoryProvider = (IInventoryTransactionProvider) source.getBottomInventory();
					if (bottomInventoryProvider.getViewers().contains(player))
						bottomInventoryProvider.onClose(player);
				}
			}
		}
		if (destination != null)
		{
			if (destination.getTopInventory() instanceof CraftInventory)
			{
				IInventory topInventory = ((CraftInventory) destination.getTopInventory()).getInventory();
				if (topInventory instanceof IInventoryTransactionProvider)
				{
					IInventoryTransactionProvider topInventoryProvider = (IInventoryTransactionProvider) topInventory;
					if (!topInventoryProvider.getViewers().contains(player))
						topInventoryProvider.onOpen(player);
				}
			}
			else
			{
				if (destination.getTopInventory() instanceof IInventoryTransactionProvider)
				{
					IInventoryTransactionProvider topInventoryProvider = (IInventoryTransactionProvider) destination.getTopInventory();
					if (!topInventoryProvider.getViewers().contains(player))
						topInventoryProvider.onOpen(player);
				}
			}
			if (destination.getBottomInventory() instanceof CraftInventory)
			{
				IInventory bottomInventory = ((CraftInventory) destination.getBottomInventory()).getInventory();
				if (bottomInventory instanceof IInventoryTransactionProvider)
				{
					IInventoryTransactionProvider bottomInventoryProvider = (IInventoryTransactionProvider) bottomInventory;
					if (!bottomInventoryProvider.getViewers().contains(player))
						bottomInventoryProvider.onOpen(player);
				}
			}
			else
			{
				if (destination.getBottomInventory() instanceof IInventoryTransactionProvider)
				{
					IInventoryTransactionProvider bottomInventoryProvider = (IInventoryTransactionProvider) destination.getBottomInventory();
					if (!bottomInventoryProvider.getViewers().contains(player))
						bottomInventoryProvider.onOpen(player);
				}
			}
		}
	}

	@Final
	@Shadow private Set<Slot> field_94537_h;
	@Shadow private int field_94535_f = -1;
	@Shadow private int field_94536_g;
	@Shadow protected abstract void func_94533_d();
	@Shadow public abstract ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_);
	@Shadow protected abstract void retrySlotClick(int p_75133_1_, int p_75133_2_, boolean p_75133_3_, EntityPlayer p_75133_4_);
	@Shadow public abstract boolean canDragIntoSlot(Slot p_94531_1_);
	@Shadow public abstract boolean func_94530_a(ItemStack p_94530_1_, Slot p_94530_2_);
	@Shadow public abstract void detectAndSendChanges();

	@Overwrite
	public ItemStack slotClick(int index, int p_75144_2_, int p_75144_3_, EntityPlayer player)
	{
		ItemStack itemstack = null;
		InventoryPlayer inventoryplayer = player.inventory;
		int i1;
		ItemStack itemstack3;
		ItemStack itemstack4;
		int j1;
		if (p_75144_3_ == 5)
		{
			int l = this.field_94536_g;
			this.field_94536_g = func_94532_c(p_75144_2_);
			if ((l != 1 || this.field_94536_g != 2) && l != this.field_94536_g)
			{
				this.func_94533_d();
			}
			else if (inventoryplayer.getItemStack() == null)
			{
				this.func_94533_d();
			}
			else if (this.field_94536_g == 0)
			{
				this.field_94535_f = func_94529_b(p_75144_2_);
				if (func_94528_d(this.field_94535_f))
				{
					this.field_94536_g = 1;
					this.field_94537_h.clear();
				} else
				{
					this.func_94533_d();
				}
			}
			else if (this.field_94536_g == 1)
			{
				Slot slot = this.inventorySlots.get(index);
				if (slot != null && func_94527_a(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > this.field_94537_h.size() && this.canDragIntoSlot(slot))
					this.field_94537_h.add(slot);
			}
			else if (this.field_94536_g == 2)
			{
				if (!this.field_94537_h.isEmpty())
				{
					itemstack3 = inventoryplayer.getItemStack().copy();
					i1 = inventoryplayer.getItemStack().stackSize;
					Map<Integer, ItemStack> draggedSlots = new HashMap<Integer, ItemStack>(); // CraftBukkit - Store slots from drag in map (raw slot id -> new stack)
					for (Object aField_94537_h : this.field_94537_h)
					{
						Slot slot1 = (Slot) aField_94537_h;
						if (slot1 != null && func_94527_a(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= this.field_94537_h.size() && this.canDragIntoSlot(slot1))
						{
							itemstack4 = itemstack3.copy();
							j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
							func_94525_a(this.field_94537_h, this.field_94535_f, itemstack4, j1);
							if (itemstack4.stackSize > itemstack4.getMaxStackSize())
							{
								itemstack4.stackSize = itemstack4.getMaxStackSize();
							}

							if (itemstack4.stackSize > slot1.getSlotStackLimit())
							{
								itemstack4.stackSize = slot1.getSlotStackLimit();
							}

							i1 -= itemstack4.stackSize - j1;
							draggedSlots.put(slot1.slotNumber, itemstack4); // CraftBukkit - Put in map instead of setting
						}
					}

					// CraftBukkit start - InventoryDragEvent
					InventoryView view = getBukkitView();
					org.bukkit.inventory.ItemStack newcursor = CraftItemStack.asCraftMirror(itemstack3);
					newcursor.setAmount(i1);
					Map<Integer, org.bukkit.inventory.ItemStack> eventmap = new HashMap<Integer, org.bukkit.inventory.ItemStack>();
					for (Map.Entry<Integer, ItemStack> ditem : draggedSlots.entrySet())
					{
						eventmap.put(ditem.getKey(), CraftItemStack.asBukkitCopy(ditem.getValue()));
					}

					// It's essential that we set the cursor to the new value here to prevent item duplication if a plugin closes the inventory.
					ItemStack oldCursor = inventoryplayer.getItemStack();
					inventoryplayer.setItemStack(CraftItemStack.asNMSCopy(newcursor));
					InventoryDragEvent event = new InventoryDragEvent(view, (newcursor.getType() != org.bukkit.Material.AIR ? newcursor : null), CraftItemStack.asBukkitCopy(oldCursor), this.field_94535_f == i1, eventmap); // Should be dragButton
					Bukkit.getPluginManager().callEvent(event);
					// Whether or not a change was made to the inventory that requires an update.
					boolean needsUpdate = event.getResult() != Result.DEFAULT;

					if (event.getResult() != Result.DENY)
					{
						for (Map.Entry<Integer, ItemStack> dslot : draggedSlots.entrySet())
							if (view != null)
								view.setItem(dslot.getKey(), CraftItemStack.asBukkitCopy(dslot.getValue()));
						// The only time the carried item will be set to null is if the inventory is closed by the server.
						// If the inventory is closed by the server, then the cursor items are dropped.  This is why we change the cursor early.
						if (inventoryplayer.getItemStack() != null)
						{
							inventoryplayer.setItemStack(CraftItemStack.asNMSCopy(event.getCursor()));
							needsUpdate = true;
						}
					}
					else
					{
						inventoryplayer.setItemStack(oldCursor);
					}
					if (needsUpdate && player instanceof EntityPlayerMP)
						((EntityPlayerMP) player).sendContainerToPlayer((Container) (Object) this);
					// CraftBukkit end
				}
				this.func_94533_d();
			}
			else
			{
				this.func_94533_d();
			}
		}
		else if (this.field_94536_g != 0)
		{
			this.func_94533_d();
		}
		else
		{
			Slot slot2;
			int l1;
			ItemStack itemstack5;
			if ((p_75144_3_ == 0 || p_75144_3_ == 1) && (p_75144_2_ == 0 || p_75144_2_ == 1))
			{
				if (index == -999)
				{
					if (inventoryplayer.getItemStack() != null)
					{
						if (p_75144_2_ == 0)
						{
							player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
							inventoryplayer.setItemStack(null);
						}

						if (p_75144_2_ == 1)
						{
							player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), true);
							if (inventoryplayer.getItemStack().stackSize == 0)
								inventoryplayer.setItemStack(null);
						}
					}
				}
				else if (p_75144_3_ == 1)
				{
					if (index < 0)
						return null;
					slot2 = this.inventorySlots.get(index);
					if (slot2 != null && slot2.canTakeStack(player))
					{
						itemstack3 = this.transferStackInSlot(player, index);
						if (itemstack3 != null)
						{
							Item item = itemstack3.getItem();
							itemstack = itemstack3.copy();
							if (slot2.getStack() != null && slot2.getStack().getItem() == item)
								this.retrySlotClick(index, p_75144_2_, true, player);
						}
					}
				}
				else
				{
					if (index < 0)
					{
						return null;
					}

					slot2 = this.inventorySlots.get(index);
					if (slot2 != null)
					{
						itemstack3 = slot2.getStack();
						itemstack4 = inventoryplayer.getItemStack();
						if (itemstack3 != null)
						{
							itemstack = itemstack3.copy();
						}

						if (itemstack3 == null)
						{
							if (itemstack4 != null && slot2.isItemValid(itemstack4))
							{
								l1 = p_75144_2_ == 0 ? itemstack4.stackSize : 1;
								if (l1 > slot2.getSlotStackLimit())
								{
									l1 = slot2.getSlotStackLimit();
								}

								if (itemstack4.stackSize >= l1)
								{
									slot2.putStack(itemstack4.splitStack(l1));
								}

								if (itemstack4.stackSize == 0)
								{
									inventoryplayer.setItemStack(null);
								}
							}
						}
						else if (slot2.canTakeStack(player))
						{
							if (itemstack4 == null)
							{
								l1 = p_75144_2_ == 0 ? itemstack3.stackSize : (itemstack3.stackSize + 1) / 2;
								itemstack5 = slot2.decrStackSize(l1);
								inventoryplayer.setItemStack(itemstack5);
								if (itemstack3.stackSize == 0)
								{
									slot2.putStack(null);
								}

								slot2.onPickupFromSlot(player, inventoryplayer.getItemStack());
							}
							else if (slot2.isItemValid(itemstack4))
							{
								if (itemstack3.getItem() == itemstack4.getItem() && itemstack3.getItemDamage() == itemstack4.getItemDamage() && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
								{
									l1 = p_75144_2_ == 0 ? itemstack4.stackSize : 1;
									if (l1 > slot2.getSlotStackLimit() - itemstack3.stackSize)
									{
										l1 = slot2.getSlotStackLimit() - itemstack3.stackSize;
									}

									if (l1 > itemstack4.getMaxStackSize() - itemstack3.stackSize)
									{
										l1 = itemstack4.getMaxStackSize() - itemstack3.stackSize;
									}

									itemstack4.splitStack(l1);
									if (itemstack4.stackSize == 0)
									{
										inventoryplayer.setItemStack(null);
									}

									itemstack3.stackSize += l1;
								}
								else if (itemstack4.stackSize <= slot2.getSlotStackLimit())
								{
									slot2.putStack(itemstack4);
									inventoryplayer.setItemStack(itemstack3);
								}
							}
							else if (itemstack3.getItem() == itemstack4.getItem() && itemstack4.getMaxStackSize() > 1 && (!itemstack3.getHasSubtypes() || itemstack3.getItemDamage() == itemstack4.getItemDamage()) && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
							{
								l1 = itemstack3.stackSize;
								if (l1 > 0 && l1 + itemstack4.stackSize <= itemstack4.getMaxStackSize())
								{
									itemstack4.stackSize += l1;
									itemstack3 = slot2.decrStackSize(l1);
									if (itemstack3.stackSize == 0)
									{
										slot2.putStack(null);
									}

									slot2.onPickupFromSlot(player, inventoryplayer.getItemStack());
								}
							}
						}

						slot2.onSlotChanged();
					}
				}
			}
			else if (p_75144_3_ == 2 && p_75144_2_ >= 0 && p_75144_2_ < 9)
			{
				slot2 = this.inventorySlots.get(index);
				if (slot2.canTakeStack(player))
				{
					itemstack3 = inventoryplayer.getStackInSlot(p_75144_2_);
					boolean flag = itemstack3 == null || slot2.inventory == inventoryplayer && slot2.isItemValid(itemstack3);
					l1 = -1;
					if (!flag)
					{
						l1 = inventoryplayer.getFirstEmptyStack();
						flag |= l1 > -1;
					}

					if (slot2.getHasStack() && flag)
					{
						itemstack5 = slot2.getStack();
						inventoryplayer.setInventorySlotContents(p_75144_2_, itemstack5.copy());
						if ((slot2.inventory != inventoryplayer || !slot2.isItemValid(itemstack3)) && itemstack3 != null)
						{
							if (l1 > -1)
							{
								inventoryplayer.addItemStackToInventory(itemstack3);
								slot2.decrStackSize(itemstack5.stackSize);
								slot2.putStack(null);
								slot2.onPickupFromSlot(player, itemstack5);
							}
						}
						else
						{
							slot2.decrStackSize(itemstack5.stackSize);
							slot2.putStack(itemstack3);
							slot2.onPickupFromSlot(player, itemstack5);
						}
					}
					else if (!slot2.getHasStack() && itemstack3 != null && slot2.isItemValid(itemstack3))
					{
						inventoryplayer.setInventorySlotContents(p_75144_2_, null);
						slot2.putStack(itemstack3);
					}
				}
			}
			else if (p_75144_3_ == 3 && player.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && index >= 0)
			{
				slot2 = this.inventorySlots.get(index);
				if (slot2 != null && slot2.getHasStack())
				{
					itemstack3 = slot2.getStack().copy();
					itemstack3.stackSize = itemstack3.getMaxStackSize();
					inventoryplayer.setItemStack(itemstack3);
				}
			}
			else if (p_75144_3_ == 4 && inventoryplayer.getItemStack() == null && index >= 0)
			{
				slot2 = this.inventorySlots.get(index);
				if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(player))
				{
					itemstack3 = slot2.decrStackSize(p_75144_2_ == 0 ? 1 : slot2.getStack().stackSize);
					slot2.onPickupFromSlot(player, itemstack3);
					player.dropPlayerItemWithRandomChoice(itemstack3, true);
				}
			}
			else if (p_75144_3_ == 6 && index >= 0)
			{
				slot2 = this.inventorySlots.get(index);
				itemstack3 = inventoryplayer.getItemStack();
				if (itemstack3 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(player)))
				{
					i1 = p_75144_2_ == 0 ? 0 : this.inventorySlots.size() - 1;
					l1 = p_75144_2_ == 0 ? 1 : -1;

					for (int i2 = 0; i2 < 2; ++i2)
					{
						for (j1 = i1; j1 >= 0 && j1 < this.inventorySlots.size() && itemstack3.stackSize < itemstack3.getMaxStackSize(); j1 += l1)
						{
							Slot slot3 = this.inventorySlots.get(j1);
							if (slot3.getHasStack() && func_94527_a(slot3, itemstack3, true) && slot3.canTakeStack(player) && this.func_94530_a(itemstack3, slot3) && (i2 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize()))
							{
								int k1 = Math.min(itemstack3.getMaxStackSize() - itemstack3.stackSize, slot3.getStack().stackSize);
								ItemStack itemstack2 = slot3.decrStackSize(k1);
								itemstack3.stackSize += k1;
								if (itemstack2.stackSize <= 0)
									slot3.putStack(null);
								slot3.onPickupFromSlot(player, itemstack2);
							}
						}
					}
				}
				this.detectAndSendChanges();
			}
		}
		return itemstack;
	}

	@Override
	public void setOpened(boolean isOpened)
	{
		this.isOpened = isOpened;
	}

	@Override
	public boolean isOpened()
	{
		return this.isOpened;
	}

	@Override
	public boolean isClosedByEventCancelling()
	{
		return this.isClosedByEventCancelling;
	}

	@Override
	public void setClosedByEventCancelling(boolean closedByEventCancelling)
	{
		this.isClosedByEventCancelling = closedByEventCancelling;
	}
}
