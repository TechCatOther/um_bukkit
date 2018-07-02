package org.ultramine.mods.bukkit.mixin.entity.player;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.entity.player.IMixinPlayerMP;

@Mixin(net.minecraft.entity.player.EntityPlayerMP.class)
public abstract class MixinPlayerMP extends EntityPlayer implements IMixinPlayerMP
{
	@Shadow
	private String translator;
	@Shadow
	private int lastExperience;
	@Shadow
	private int field_147101_bU;
	@Shadow public int currentWindowId;

	@Shadow public NetHandlerPlayServer playerNetServerHandler;
	private static final Logger logger = LogManager.getLogger();

	// CraftBukkit start
	public String displayName;
	public String listName;
	public org.bukkit.Location compassTarget;
	public int newExp = 0;
	public int newLevel = 0;
	public int newTotalExp = 0;
	public boolean keepLevel = false;
	public double maxHealthCache;
	// CraftBukkit end
	// Spigot start
	public boolean collidesWithEntities = true;

	public MixinPlayerMP(World p_i45324_1_, GameProfile p_i45324_2_)
	{
		super(p_i45324_1_, p_i45324_2_);
	}

	@Override
	public String getTranslator()
	{
		return translator;
	}

	@Override
	public int getLastExperience()
	{
		return lastExperience;
	}

	@Override
	public void setLastExperience(int lastExperience)
	{
		this.lastExperience = lastExperience;
	}

	@Override
	public int getField_147101_bU()
	{
		return field_147101_bU;
	}

	@Override
	public void setField_147101_bU(int field_147101_bU)
	{
		this.field_147101_bU = field_147101_bU;
	}

	@Override
	public String getBukkitDisplayName()
	{
		return displayName;
	}

	@Override
	public void setBukkitDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String getBukkitListName()
	{
		return listName;
	}

	@Override
	public void setBukkitListName(String listName)
	{
		this.listName = listName;
	}

	@Override
	public org.bukkit.Location getCompassTarget()
	{
		return compassTarget;
	}

	@Override
	public void setCompassTarget(org.bukkit.Location compassTarget)
	{
		this.compassTarget = compassTarget;
	}

	@Override
	public int getNewExp()
	{
		return newExp;
	}

	@Override
	public void setNewExp(int newExp)
	{
		this.newExp = newExp;
	}

	@Override
	public int getNewLevel()
	{
		return newLevel;
	}

	@Override
	public void setNewLevel(int newLevel)
	{
		this.newLevel = newLevel;
	}

	@Override
	public int getNewTotalExp()
	{
		return newTotalExp;
	}

	@Override
	public void setNewTotalExp(int newTotalExp)
	{
		this.newTotalExp = newTotalExp;
	}

	@Override
	public boolean isKeepLevel()
	{
		return keepLevel;
	}

	@Override
	public void setKeepLevel(boolean keepLevel)
	{
		this.keepLevel = keepLevel;
	}

	@Override
	public double getMaxHealthCache()
	{
		return maxHealthCache;
	}

	@Override
	public void setMaxHealthCache(double maxHealthCache)
	{
		this.maxHealthCache = maxHealthCache;
	}

	@Override
	public boolean isCollidesWithEntities()
	{
		return collidesWithEntities;
	}

	@Override
	public void setCollidesWithEntities(boolean collidesWithEntities)
	{
		this.collidesWithEntities = collidesWithEntities;
	}

	public int nextContainerCounter()
	{
		this.currentWindowId = this.currentWindowId % 100 + 1;
		return this.currentWindowId;
	}

	@Inject(method = "readEntityFromNBT", at = @At("RETURN"))
	public void onReadEntityFromNBT(NBTTagCompound nbt, CallbackInfo ci)
	{
		((CraftPlayer) getBukkitEntity()).readExtraData(nbt);
	}

	@Inject(method = "writeEntityToNBT", at = @At("RETURN"))
	public void onWriteEntityToNBT(NBTTagCompound nbt, CallbackInfo ci)
	{
		((CraftPlayer) getBukkitEntity()).setExtraData(nbt);
	}

	@Overwrite
	public void displayGUIEnchantment(int p_71002_1_, int p_71002_2_, int p_71002_3_, String p_71002_4_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerEnchantment(this.inventory, this.worldObj, p_71002_1_, p_71002_2_, p_71002_3_));
		if (container == null)
			return;
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 4, p_71002_4_ == null ? "" : p_71002_4_, 9, p_71002_4_ != null));
		this.openContainer = container;
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void displayGUIWorkbench(int p_71058_1_, int p_71058_2_, int p_71058_3_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerWorkbench(this.inventory, this.worldObj, p_71058_1_, p_71058_2_, p_71058_3_));
		if (container == null)
			return;
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 1, "Crafting", 9, true));
		this.openContainer = container;
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void displayGUIAnvil(int p_82244_1_, int p_82244_2_, int p_82244_3_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerRepair(this.inventory, this.worldObj, p_82244_1_, p_82244_2_, p_82244_3_, this));
		if (container == null)
			return;
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 8, "Repairing", 9, true));
		this.openContainer = container;
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void displayGUIChest(IInventory p_71007_1_)
	{
		if (this.openContainer != this.inventoryContainer)
			this.closeScreen();
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerChest(this.inventory, p_71007_1_));
		if (container == null)
		{
			p_71007_1_.closeInventory(); // Cauldron - prevent chest from being stuck in open state on clients
			return;
		}
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 0, p_71007_1_.getInventoryName(), p_71007_1_.getSizeInventory(), p_71007_1_.hasCustomInventoryName()));
		this.openContainer = container;
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void func_146093_a(TileEntityHopper p_146093_1_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerHopper(this.inventory, p_146093_1_));
		if (container == null)
		{
			p_146093_1_.closeInventory(); // Cauldron - prevent chest from being stuck in open state on clients
			return;
		}
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 9, p_146093_1_.getInventoryName(), p_146093_1_.getSizeInventory(), p_146093_1_.hasCustomInventoryName()));
		this.openContainer = container;
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void displayGUIHopperMinecart(EntityMinecartHopper p_96125_1_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerHopper(this.inventory, p_96125_1_));
		if (container == null)
		{
			p_96125_1_.closeInventory(); // Cauldron - prevent chest from being stuck in open state on clients
			return;
		}
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 9, p_96125_1_.getInventoryName(), p_96125_1_.getSizeInventory(), p_96125_1_.hasCustomInventoryName()));
		this.openContainer = container; // CraftBukkit - Use container we passed to event
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void func_146101_a(TileEntityFurnace p_146101_1_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerFurnace(this.inventory, p_146101_1_));
		if (container == null)
		{
			p_146101_1_.closeInventory(); // Cauldron - prevent chests from being stuck in open state on clients
			return;
		}
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 2, p_146101_1_.getInventoryName(), p_146101_1_.getSizeInventory(), p_146101_1_.hasCustomInventoryName()));
		this.openContainer = container; // CraftBukkit - Use container we passed to event
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void func_146102_a(TileEntityDispenser p_146102_1_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerDispenser(this.inventory, p_146102_1_));
		if (container == null)
		{
			p_146102_1_.closeInventory(); // Cauldron - prevent chests from being stuck in open state on clients
			return;
		}
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, p_146102_1_ instanceof TileEntityDropper ? 10 : 3, p_146102_1_.getInventoryName(), p_146102_1_.getSizeInventory(), p_146102_1_.hasCustomInventoryName()));
		this.openContainer = container; // CraftBukkit - Use container we passed to event
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void func_146098_a(TileEntityBrewingStand p_146098_1_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerBrewingStand(this.inventory, p_146098_1_));
		if (container == null)
		{
			p_146098_1_.closeInventory(); // Cauldron - prevent chests from being stuck in open state on clients
			return;
		}
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 5, p_146098_1_.getInventoryName(), p_146098_1_.getSizeInventory(), p_146098_1_.hasCustomInventoryName()));
		this.openContainer = container; // CraftBukkit - Use container we passed to event
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void func_146104_a(TileEntityBeacon p_146104_1_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerBeacon(this.inventory, p_146104_1_));
		if (container == null)
		{
			p_146104_1_.closeInventory(); // Cauldron - prevent chests from being stuck in open state on clients
			return;
		}
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 7, p_146104_1_.getInventoryName(), p_146104_1_.getSizeInventory(), p_146104_1_.hasCustomInventoryName()));
		this.openContainer = container;
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Overwrite
	public void displayGUIMerchant(IMerchant p_71030_1_, String p_71030_2_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerMerchant(this.inventory, p_71030_1_, this.worldObj));
		if (container == null)
			return;
		this.nextContainerCounter();
		this.openContainer = container;
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
		InventoryMerchant inventorymerchant = ((ContainerMerchant) this.openContainer).getMerchantInventory();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 6, p_71030_2_ == null ? "" : p_71030_2_, inventorymerchant.getSizeInventory(), p_71030_2_ != null));
		MerchantRecipeList merchantrecipelist = p_71030_1_.getRecipes(this);
		if (merchantrecipelist != null)
		{
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			try
			{
				packetbuffer.writeInt(this.currentWindowId);
				merchantrecipelist.func_151391_a(packetbuffer);
				this.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload("MC|TrList", packetbuffer));
			} catch (Exception var10)
			{
				logger.error("Couldn't send trade list", var10);
			} finally
			{
				packetbuffer.release();
			}
		}
	}

	@Overwrite
	public void displayGUIHorse(EntityHorse p_110298_1_, IInventory p_110298_2_)
	{
		Container container = CraftEventFactory.callInventoryOpenEvent((EntityPlayerMP) (Object) this, new ContainerHorseInventory(this.inventory, p_110298_2_, p_110298_1_));
		if (container == null)
		{
			p_110298_2_.closeInventory(); // Cauldron - prevent chests from being stuck in open state on clients
			return;
		}
		if (this.openContainer != this.inventoryContainer)
			this.closeScreen();
		this.nextContainerCounter();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 11, p_110298_2_.getInventoryName(), p_110298_2_.getSizeInventory(), p_110298_2_.hasCustomInventoryName(), p_110298_1_.getEntityId()));
		this.openContainer = container;
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters((EntityPlayerMP) (Object) this);
	}

	@Inject(method = "closeScreen", at = @At(value = "HEAD"))
	public void closeScreenInject(CallbackInfo ci)
	{
		CraftEventFactory.handleInventoryCloseEvent(this);
	}

	@Override
	public void closeScreenSilent()
	{
		this.playerNetServerHandler.sendPacket(new S2EPacketCloseWindow(this.openContainer.windowId));
		this.openContainer.onContainerClosed(this);
		this.openContainer = this.inventoryContainer;
	}
}
