package org.bukkit.craftbukkit.event;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Statistic.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftStatistic;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftDamageSource;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.meta.BookMeta;
import org.ultramine.mods.bukkit.interfaces.IMixinEntityDamageSourceIndirect;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLiving;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntityLivingBase;
import org.ultramine.mods.bukkit.interfaces.entity.player.IMixinPlayerMP;
import org.ultramine.mods.bukkit.interfaces.inventory.IInventoryTransactionProvider;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinContainer;
import org.ultramine.mods.bukkit.interfaces.network.play.client.IMixinC0DPacketCloseWindow;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;
import org.ultramine.mods.bukkit.util.ArmorPropertiesUM;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Cauldron start
// Cauldron end


public class CraftEventFactory
{
	public static final net.minecraft.util.DamageSource MELTING = CraftDamageSource.copyOf(net.minecraft.util.DamageSource.onFire);
	public static final net.minecraft.util.DamageSource POISON = CraftDamageSource.copyOf(net.minecraft.util.DamageSource.magic);
	public static org.bukkit.block.Block blockDamage; // For use in EntityDamageByBlockEvent
	public static Entity entityDamage; // For use in EntityDamageByEntityEvent

	// helper methods
	private static boolean canBuild(CraftWorld world, Player player, int x, int z)
	{
		return true;
//		net.minecraft.world.WorldServer worldServer = world.getHandle();
//		int spawnSize = Bukkit.getServer().getSpawnRadius();
//
//		if (world.getHandle().provider.dimensionId != 0) return true;
//		if (spawnSize <= 0) return true;
//		if (((CraftServer) Bukkit.getServer()).getHandle().func_152603_m().func_152690_d()) return true;
//		if (player.isOp()) return true;
//
//		net.minecraft.util.ChunkCoordinates chunkcoordinates = worldServer.getSpawnPoint();
//
//		int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.posX), Math.abs(z - chunkcoordinates.posZ));
//		return distanceFromSpawn > spawnSize;
	}

	public static <T extends Event> T callEvent(T event)
	{
		Bukkit.getServer().getPluginManager().callEvent(event);
		return event;
	}

	/**
	 * Block place methods
	 */
	// Cauldron start
	public static BlockMultiPlaceEvent callBlockMultiPlaceEvent(net.minecraft.world.World world, net.minecraft.entity.player.EntityPlayer who, List<BlockState> blockStates, int clickedX, int clickedY, int clickedZ)
	{
		CraftWorld craftWorld = ((IMixinWorld) world).getWorld();
		CraftServer craftServer = ((IMixinWorld) world).getServer();

		Player player = (who == null) ? null : (Player) ((IMixinEntity) who).getBukkitEntity();

		Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
		CraftItemStack is = CraftItemStack.asCraftMirror(who == null ? null : who.inventory.getCurrentItem());

		boolean canBuild = true;
		for(int i = 0; i < blockStates.size(); i++)
		{
			if(!canBuild(craftWorld, player, blockStates.get(i).getX(), blockStates.get(i).getZ()))
			{
				canBuild = false;
				break;
			}
		}

		BlockMultiPlaceEvent event = new BlockMultiPlaceEvent(blockStates, blockClicked, is, player, canBuild);
		craftServer.getPluginManager().callEvent(event);

		return event;
	}
	// Cauldron end

	public static BlockPlaceEvent callBlockPlaceEvent(net.minecraft.world.World world, net.minecraft.entity.player.EntityPlayer who, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ)
	{
		CraftWorld craftWorld = ((IMixinWorld) world).getWorld();
		CraftServer craftServer = ((IMixinWorld) world).getServer();

		Player player = (who == null) ? null : (Player) ((IMixinEntity) who).getBukkitEntity();

		Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
		Block placedBlock = replacedBlockState.getBlock();
		CraftItemStack is = CraftItemStack.asCraftMirror(who == null ? null : who.inventory.getCurrentItem());

		boolean canBuild = canBuild(craftWorld, player, placedBlock.getX(), placedBlock.getZ());

		BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, blockClicked, is, player, canBuild);
		craftServer.getPluginManager().callEvent(event);

		return event;
	}

	/**
	 * Bucket methods
	 */
	public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(net.minecraft.entity.player.EntityPlayer who, int clickedX, int clickedY, int clickedZ, int clickedFace, net.minecraft.item.ItemStack itemInHand)
	{
		return (PlayerBucketEmptyEvent) getPlayerBucketEvent(false, who, clickedX, clickedY, clickedZ, clickedFace, itemInHand, net.minecraft.init.Items.bucket);
	}

	public static PlayerBucketFillEvent callPlayerBucketFillEvent(net.minecraft.entity.player.EntityPlayer who, int clickedX, int clickedY, int clickedZ, int clickedFace, net.minecraft.item.ItemStack itemInHand, net.minecraft.item.Item bucket)
	{
		return (PlayerBucketFillEvent) getPlayerBucketEvent(true, who, clickedX, clickedY, clickedZ, clickedFace, itemInHand, bucket);
	}

	private static PlayerEvent getPlayerBucketEvent(boolean isFilling, net.minecraft.entity.player.EntityPlayer who, int clickedX, int clickedY, int clickedZ, int clickedFace, net.minecraft.item.ItemStack itemstack, net.minecraft.item.Item item)
	{
		Player player = (who == null) ? null : (Player) ((IMixinEntity) who).getBukkitEntity();
		CraftItemStack itemInHand = CraftItemStack.asNewCraftStack(item);
		Material bucket = CraftMagicNumbers.getMaterial(itemstack.getItem());

		CraftWorld craftWorld = (CraftWorld) player.getWorld();
		CraftServer craftServer = (CraftServer) player.getServer();

		Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
		BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);

		PlayerEvent event = null;
		if(isFilling)
		{
			event = new PlayerBucketFillEvent(player, blockClicked, blockFace, bucket, itemInHand);
			((PlayerBucketFillEvent) event).setCancelled(!canBuild(craftWorld, player, clickedX, clickedZ));
		}
		else
		{
			event = new PlayerBucketEmptyEvent(player, blockClicked, blockFace, bucket, itemInHand);
			((PlayerBucketEmptyEvent) event).setCancelled(!canBuild(craftWorld, player, clickedX, clickedZ));
		}

		craftServer.getPluginManager().callEvent(event);

		return event;
	}

	/**
	 * Player Interact event
	 */
	public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.entity.player.EntityPlayer who, Action action, net.minecraft.item.ItemStack itemstack)
	{
		if(action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR)
		{
			throw new IllegalArgumentException();
		}
		return callPlayerInteractEvent(who, action, 0, 256, 0, 0, itemstack);
	}

	public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.entity.player.EntityPlayer who, Action action, int clickedX, int clickedY, int clickedZ, int clickedFace, net.minecraft.item.ItemStack itemstack)
	{
		Player player = (who == null) ? null : (Player) ((IMixinEntity) who).getBukkitEntity();
		CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

		CraftWorld craftWorld = (CraftWorld) player.getWorld();
		CraftServer craftServer = (CraftServer) player.getServer();

		Block blockClicked = clickedY > 255 ? null : craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
		BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);

		if(clickedY > 255)
		{
			switch(action)
			{
			case LEFT_CLICK_BLOCK:
				action = Action.LEFT_CLICK_AIR;
				break;
			case RIGHT_CLICK_BLOCK:
				action = Action.RIGHT_CLICK_AIR;
				break;
			}
		}

		if(itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)
		{
			itemInHand = null;
		}

		PlayerInteractEvent event = new PlayerInteractEvent(player, action, itemInHand, blockClicked, blockFace);
		craftServer.getPluginManager().callEvent(event);

		return event;
	}

	/**
	 * EntityShootBowEvent
	 */
	public static EntityShootBowEvent callEntityShootBowEvent(net.minecraft.entity.EntityLivingBase who, net.minecraft.item.ItemStack itemstack, net.minecraft.entity.projectile.EntityArrow entityArrow, float force)
	{
		LivingEntity shooter = (LivingEntity) ((IMixinEntity) who).getBukkitEntity();
		CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);
		Arrow arrow = (Arrow) ((IMixinEntity) entityArrow).getBukkitEntity();

		if(itemInHand != null && (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0))
		{
			itemInHand = null;
		}

		EntityShootBowEvent event = new EntityShootBowEvent(shooter, itemInHand, arrow, force);
		Bukkit.getPluginManager().callEvent(event);

		return event;
	}

	/**
	 * BlockDamageEvent
	 */
	public static BlockDamageEvent callBlockDamageEvent(net.minecraft.entity.player.EntityPlayer who, int x, int y, int z, net.minecraft.item.ItemStack itemstack, boolean instaBreak)
	{
		Player player = (who == null) ? null : (Player) ((IMixinEntity) who).getBukkitEntity();
		CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

		CraftWorld craftWorld = (CraftWorld) player.getWorld();
		CraftServer craftServer = (CraftServer) player.getServer();

		Block blockClicked = craftWorld.getBlockAt(x, y, z);

		BlockDamageEvent event = new BlockDamageEvent(player, blockClicked, itemInHand, instaBreak);
		craftServer.getPluginManager().callEvent(event);

		return event;
	}

	/**
	 * CreatureSpawnEvent
	 */
	public static CreatureSpawnEvent callCreatureSpawnEvent(net.minecraft.entity.EntityLivingBase entityliving, SpawnReason spawnReason)
	{
		LivingEntity entity = (LivingEntity) ((IMixinEntity) entityliving).getBukkitEntity();
		CraftServer craftServer = (CraftServer) entity.getServer();

		CreatureSpawnEvent event = new CreatureSpawnEvent(entity, spawnReason);
		craftServer.getPluginManager().callEvent(event);
		return event;
	}

	/**
	 * EntityTameEvent
	 */
	public static EntityTameEvent callEntityTameEvent(net.minecraft.entity.EntityLiving entity, net.minecraft.entity.player.EntityPlayer tamer)
	{
		org.bukkit.entity.Entity bukkitEntity = ((IMixinEntity) entity).getBukkitEntity();
		org.bukkit.entity.AnimalTamer bukkitTamer = (org.bukkit.entity.AnimalTamer) (tamer != null ? ((IMixinEntity) tamer).getBukkitEntity() : null);
		CraftServer craftServer = (CraftServer) bukkitEntity.getServer();

		((IMixinEntityLiving) entity).setPersistenceRequired(true);

		EntityTameEvent event = new EntityTameEvent((LivingEntity) bukkitEntity, bukkitTamer);
		craftServer.getPluginManager().callEvent(event);
		return event;
	}

	/**
	 * ItemSpawnEvent
	 */
	public static ItemSpawnEvent callItemSpawnEvent(net.minecraft.entity.item.EntityItem entityitem)
	{
		org.bukkit.entity.Item entity = (org.bukkit.entity.Item) ((IMixinEntity) entityitem).getBukkitEntity();
		CraftServer craftServer = (CraftServer) entity.getServer();

		ItemSpawnEvent event = new ItemSpawnEvent(entity, entity.getLocation());

		craftServer.getPluginManager().callEvent(event);
		return event;
	}

	/**
	 * ItemDespawnEvent
	 */
	public static ItemDespawnEvent callItemDespawnEvent(net.minecraft.entity.item.EntityItem entityitem)
	{
		org.bukkit.entity.Item entity = (org.bukkit.entity.Item) ((IMixinEntity) entityitem).getBukkitEntity();

		ItemDespawnEvent event = new ItemDespawnEvent(entity, entity.getLocation());

		entity.getServer().getPluginManager().callEvent(event);
		return event;
	}

	/**
	 * PotionSplashEvent
	 */
	public static PotionSplashEvent callPotionSplashEvent(net.minecraft.entity.projectile.EntityPotion potion, Map<LivingEntity, Double> affectedEntities)
	{
		ThrownPotion thrownPotion = (ThrownPotion) ((IMixinEntity) potion).getBukkitEntity();

		PotionSplashEvent event = new PotionSplashEvent(thrownPotion, affectedEntities);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

	/**
	 * BlockFadeEvent
	 */
	public static BlockFadeEvent callBlockFadeEvent(Block block, net.minecraft.block.Block type)
	{
		BlockState state = block.getState();
		state.setTypeId(net.minecraft.block.Block.getIdFromBlock(type));

		BlockFadeEvent event = new BlockFadeEvent(block, state);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

	public static void handleBlockSpreadEvent(Block block, Block source, net.minecraft.block.Block type, int data)
	{
		BlockState state = block.getState();
		state.setTypeId(net.minecraft.block.Block.getIdFromBlock(type));
		state.setRawData((byte) data);

		BlockSpreadEvent event = new BlockSpreadEvent(block, source, state);
		Bukkit.getPluginManager().callEvent(event);

		if(!event.isCancelled())
		{
			state.update(true);
		}
	}

	public static EntityDeathEvent callEntityDeathEvent(net.minecraft.entity.EntityLivingBase victim)
	{
		return callEntityDeathEvent(victim, new ArrayList<org.bukkit.inventory.ItemStack>(0));
	}

	public static EntityDeathEvent callEntityDeathEvent(net.minecraft.entity.EntityLivingBase victim, List<org.bukkit.inventory.ItemStack> drops)
	{
		CraftLivingEntity entity = (CraftLivingEntity) ((IMixinEntity) victim).getBukkitEntity();
		EntityDeathEvent event = new EntityDeathEvent(entity, drops, ((IMixinEntityLivingBase) victim).getExpReward());
		//org.bukkit.World world = entity.getWorld();
		Bukkit.getServer().getPluginManager().callEvent(event);

		((IMixinEntityLivingBase) victim).setExpToDrop(event.getDroppedExp());
		// Cauldron start - handle any drop changes from plugins
		victim.capturedDrops.clear();
		for(org.bukkit.inventory.ItemStack stack : event.getDrops())
		{
			net.minecraft.entity.item.EntityItem entityitem = new net.minecraft.entity.item.EntityItem(victim.worldObj, entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), CraftItemStack.asNMSCopy(stack));
			if(entityitem != null)
			{
				victim.capturedDrops.add((EntityItem) entityitem);
			}
		}
		// Cauldron end

		return event;
	}

	public static PlayerDeathEvent callPlayerDeathEvent(net.minecraft.entity.player.EntityPlayerMP victim, List<org.bukkit.inventory.ItemStack> drops, String deathMessage, boolean keepInventory)
	{
		CraftPlayer entity = (CraftPlayer) ((IMixinEntity) victim).getBukkitEntity();
		PlayerDeathEvent event = new PlayerDeathEvent(entity, drops, ((IMixinEntityLivingBase) victim).getExpReward(), 0, deathMessage);
		event.setKeepInventory(keepInventory);
		event.setKeepLevel(keepInventory);
		//org.bukkit.World world = entity.getWorld();
		Bukkit.getServer().getPluginManager().callEvent(event);

		IMixinPlayerMP mixinVictim = (IMixinPlayerMP) victim;
		mixinVictim.setKeepLevel(event.getKeepLevel());
		mixinVictim.setNewLevel(event.getNewLevel());
		mixinVictim.setNewTotalExp(event.getNewTotalExp());
		mixinVictim.setExpToDrop(event.getDroppedExp());
		mixinVictim.setNewExp(event.getNewExp());
		if(event.getKeepInventory()) return event;
		victim.capturedDrops.clear(); // Cauldron - we must clear pre-capture to avoid duplicates

		for(org.bukkit.inventory.ItemStack stack : event.getDrops())
		{
			if(stack == null || stack.getType() == Material.AIR) continue;

			// Cauldron start - add support for Forge's PlayerDropsEvent
			//world.dropItemNaturally(entity.getLocation(), stack); // handle world drop in EntityPlayerMP
			if(victim.captureDrops)
			{
				net.minecraft.entity.item.EntityItem entityitem = new net.minecraft.entity.item.EntityItem(victim.worldObj, entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), CraftItemStack.asNMSCopy(stack));
				if(entityitem != null)
				{
					victim.capturedDrops.add((EntityItem) entityitem);
				}
			}
			// Cauldron end
		}

		return event;
	}

	/**
	 * Server methods
	 */
	public static ServerListPingEvent callServerListPingEvent(Server craftServer, InetAddress address, String motd, int numPlayers, int maxPlayers)
	{
		ServerListPingEvent event = new ServerListPingEvent(address, motd, numPlayers, maxPlayers);
		craftServer.getPluginManager().callEvent(event);
		return event;
	}

	private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions)
	{
		if(source.isExplosion())
		{
			DamageCause damageCause;
			Entity damager = entityDamage;
			entityDamage = null;
			EntityDamageEvent event;
			if(damager == null)
			{
				event = new EntityDamageByBlockEvent(null, ((IMixinEntity) entity).getBukkitEntity(), DamageCause.BLOCK_EXPLOSION, modifiers, modifierFunctions);
			}
			else if(entity instanceof EntityDragon && ((EntityDragon) entity).healingEnderCrystal == damager)
			{
				event = new EntityDamageEvent(((IMixinEntity) entity).getBukkitEntity(), DamageCause.ENTITY_EXPLOSION, modifiers, modifierFunctions);
			}
			else
			{
				if(damager instanceof org.bukkit.entity.TNTPrimed)
				{
					damageCause = DamageCause.BLOCK_EXPLOSION;
				}
				else
				{
					damageCause = DamageCause.ENTITY_EXPLOSION;
				}
				event = new EntityDamageByEntityEvent(((IMixinEntity) damager).getBukkitEntity(), ((IMixinEntity) entity).getBukkitEntity(), damageCause, modifiers, modifierFunctions);
			}

			callEvent(event);

			if(!event.isCancelled())
			{
				event.getEntity().setLastDamageCause(event);
			}
			return event;
		}
		else if(source instanceof EntityDamageSource)
		{
			Entity damager = source.getEntity();
			DamageCause cause = DamageCause.ENTITY_ATTACK;

			if(source instanceof net.minecraft.util.EntityDamageSourceIndirect)
			{
				damager = ((IMixinEntityDamageSourceIndirect) source).getProximateDamageSource();
				// Cauldron start - vanilla compatibility
				if(damager != null)
				{
					if(((IMixinEntity) damager).getBukkitEntity() instanceof ThrownPotion)
					{
						cause = DamageCause.MAGIC;
					}
					else if(((IMixinEntity) damager).getBukkitEntity() instanceof Projectile)
					{
						cause = DamageCause.PROJECTILE;
					}
				}
				// Cauldron end
			}
			else if("thorns".equals(source.damageType))
			{
				cause = DamageCause.THORNS;
			}

			return callEntityDamageEvent(damager, entity, cause, modifiers, modifierFunctions);
		}
		else if(source == DamageSource.outOfWorld)
		{
			EntityDamageEvent event = callEvent(new EntityDamageByBlockEvent(null, ((IMixinEntity) entity).getBukkitEntity(), DamageCause.VOID, modifiers, modifierFunctions));
			if(!event.isCancelled())
			{
				event.getEntity().setLastDamageCause(event);
			}
			return event;
		}
		else if(source == DamageSource.lava)
		{
			EntityDamageEvent event = callEvent(new EntityDamageByBlockEvent(null, ((IMixinEntity) entity).getBukkitEntity(), DamageCause.LAVA, modifiers, modifierFunctions));
			if(!event.isCancelled())
			{
				event.getEntity().setLastDamageCause(event);
			}
			return event;
		}
		else if(blockDamage != null)
		{
			DamageCause cause = null;
			Block damager = blockDamage;
			blockDamage = null;
			if(source == DamageSource.cactus)
			{
				cause = DamageCause.CONTACT;
				// Cauldron start - add more causes for mods
			}
			else if(source == DamageSource.fall)
			{
				cause = DamageCause.FALL;
			}
			else if(source == DamageSource.anvil || source == DamageSource.fallingBlock)
			{
				cause = DamageCause.FALLING_BLOCK;
			}
			else if(source == DamageSource.inFire)
			{
				cause = DamageCause.FIRE;
			}
			else if(source == DamageSource.onFire)
			{
				cause = DamageCause.FIRE_TICK;
			}
			else if(source == DamageSource.lava)
			{
				cause = DamageCause.LAVA;
			}
			else if(damager instanceof LightningStrike)
			{
				cause = DamageCause.LIGHTNING;
			}
			else if(source == DamageSource.magic)
			{
				cause = DamageCause.MAGIC;
			}
			else if(source == MELTING)
			{
				cause = DamageCause.MELTING;
			}
			else if(source == POISON)
			{
				cause = DamageCause.POISON;
			}
			else if(source == DamageSource.generic)
			{
				cause = DamageCause.CUSTOM;
			}
			else
			{
				//throw new RuntimeException("Unhandled entity damage");
				cause = DamageCause.CUSTOM;
			}
			// Cauldron end
			EntityDamageEvent event = callEvent(new EntityDamageByBlockEvent(damager, ((IMixinEntity) entity).getBukkitEntity(), cause, modifiers, modifierFunctions));
			if(!event.isCancelled())
			{
				event.getEntity().setLastDamageCause(event);
			}
			return event;
		}
		else if(entityDamage != null)
		{
			DamageCause cause = null;
			CraftEntity damager = ((IMixinEntity) entityDamage).getBukkitEntity();
			entityDamage = null;
			if(source == DamageSource.anvil || source == DamageSource.fallingBlock)
			{
				cause = DamageCause.FALLING_BLOCK;
			}
			else if(damager instanceof LightningStrike)
			{
				cause = DamageCause.LIGHTNING;
			}
			else if(source == DamageSource.fall)
			{
				cause = DamageCause.FALL;
				// Cauldron start - add more causes for mods
			}
			else if(source == DamageSource.cactus)
			{
				cause = DamageCause.CONTACT;
			}
			else if(source == DamageSource.inFire)
			{
				cause = DamageCause.FIRE;
			}
			else if(source == DamageSource.onFire)
			{
				cause = DamageCause.FIRE_TICK;
			}
			else if(source == DamageSource.lava)
			{
				cause = DamageCause.LAVA;
			}
			else if(source == DamageSource.magic)
			{
				cause = DamageCause.MAGIC;
			}
			else if(source == MELTING)
			{
				cause = DamageCause.MELTING;
			}
			else if(source == POISON)
			{
				cause = DamageCause.POISON;
			}
			else if(source == DamageSource.generic)
			{
				cause = DamageCause.CUSTOM;
			}
			else
			{
				//throw new RuntimeException("Unhandled entity damage");
				cause = DamageCause.CUSTOM;
			}
			// Cauldron end
			EntityDamageEvent event = callEvent(new EntityDamageByEntityEvent(damager, ((IMixinEntity) entity).getBukkitEntity(), cause, modifiers, modifierFunctions));
			if(!event.isCancelled())
			{
				event.getEntity().setLastDamageCause(event);
			}
			return event;
		}

		DamageCause cause = null;
		if(source == DamageSource.inFire)
		{
			cause = DamageCause.FIRE;
		}
		else if(source == DamageSource.starve)
		{
			cause = DamageCause.STARVATION;
		}
		else if(source == DamageSource.wither)
		{
			cause = DamageCause.WITHER;
		}
		else if(source == DamageSource.inWall)
		{
			cause = DamageCause.SUFFOCATION;
		}
		else if(source == DamageSource.drown)
		{
			cause = DamageCause.DROWNING;
		}
		else if(source == DamageSource.onFire)
		{
			cause = DamageCause.FIRE_TICK;
		}
		else if(source == MELTING)
		{
			cause = DamageCause.MELTING;
		}
		else if(source == POISON)
		{
			cause = DamageCause.POISON;
		}
		else if(source == DamageSource.magic)
		{
			cause = DamageCause.MAGIC;
		}
		else if(source == DamageSource.fall)
		{
			cause = DamageCause.FALL;
			// Cauldron start - add more causes for mods
		}
		else if(source == DamageSource.cactus)
		{
			cause = DamageCause.CONTACT;
		}
		else if(source == DamageSource.lava)
		{
			cause = DamageCause.LAVA;
		}
		else if(source == DamageSource.generic || cause == null)
		{
			return new EntityDamageEvent(((IMixinEntity) entity).getBukkitEntity(), DamageCause.CUSTOM, modifiers, modifierFunctions); // use custom
		}

		return callEntityDamageEvent(null, entity, cause, modifiers, modifierFunctions);
		//throw new RuntimeException("Unhandled entity damage");
		// Cauldron end
	}

	private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions)
	{
		EntityDamageEvent event;
		if(damager != null)
		{
			event = new EntityDamageByEntityEvent(((IMixinEntity) damager).getBukkitEntity(), ((IMixinEntity) damagee).getBukkitEntity(), cause, modifiers, modifierFunctions);
		}
		else
		{
			event = new EntityDamageEvent(((IMixinEntity) damagee).getBukkitEntity(), cause, modifiers, modifierFunctions);
		}

		callEvent(event);

		if(!event.isCancelled())
		{
			event.getEntity().setLastDamageCause(event);
		}

		return event;
	}

	private static final Function<? super Double, Double> ZERO = Functions.constant(-0.0);

	public static EntityDamageEvent handleLivingEntityDamageEvent(Entity damagee, DamageSource source, double rawDamage, double hardHatModifier, double blockingModifier, double armorModifier, double resistanceModifier, double magicModifier, double absorptionModifier, Function<Double, Double> hardHat, Function<Double, Double> blocking, Function<Double, Double> armor, Function<Double, Double> resistance, Function<Double, Double> magic, Function<Double, Double> absorption)
	{
		Map<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
		Map<DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap<DamageModifier, Function<? super Double, Double>>(DamageModifier.class);
		modifiers.put(DamageModifier.BASE, rawDamage);
		modifierFunctions.put(DamageModifier.BASE, ZERO);
		if(source == DamageSource.fallingBlock || source == DamageSource.anvil)
		{
			modifiers.put(DamageModifier.HARD_HAT, hardHatModifier);
			modifierFunctions.put(DamageModifier.HARD_HAT, hardHat);
		}
		if(damagee instanceof EntityPlayer)
		{
			modifiers.put(DamageModifier.BLOCKING, blockingModifier);
			modifierFunctions.put(DamageModifier.BLOCKING, blocking);
		}
		modifiers.put(DamageModifier.ARMOR, armorModifier);
		modifierFunctions.put(DamageModifier.ARMOR, armor);
		modifiers.put(DamageModifier.RESISTANCE, resistanceModifier);
		modifierFunctions.put(DamageModifier.RESISTANCE, resistance);
		modifiers.put(DamageModifier.MAGIC, magicModifier);
		modifierFunctions.put(DamageModifier.MAGIC, magic);
		modifiers.put(DamageModifier.ABSORPTION, absorptionModifier);
		modifierFunctions.put(DamageModifier.ABSORPTION, absorption);
		return handleEntityDamageEvent(damagee, source, modifiers, modifierFunctions);
	}

	public static EntityDamageEvent handleLivingEntityDamageEvent(final EntityLivingBase entity, final DamageSource damagesource, final float originalDamage)
	{
		final boolean human = entity instanceof EntityPlayer;
		Function<Double, Double> hardHat = new Function<Double, Double>()
		{
			@Override
			public Double apply(Double f)
			{
				if((damagesource == DamageSource.anvil || damagesource == DamageSource.fallingBlock) && entity.getEquipmentInSlot(4) != null)
				{
					return -(f - (f * 0.75F));
				}
				return -0.0;
			}
		};

		float damage = originalDamage;

		float hardHatModifier = hardHat.apply((double) damage).floatValue();
		damage += hardHatModifier;

		Function<Double, Double> blocking = new Function<Double, Double>()
		{
			@Override
			public Double apply(Double f)
			{
				if(human)
				{
					if(!damagesource.isUnblockable() && ((EntityPlayer) entity).isBlocking() && f > 0.0F)
					{
						return -(f - ((1.0F + f) * 0.5F));
					}
				}
				return -0.0;
			}
		};
		float blockingModifier = blocking.apply((double) damage).floatValue();
		damage += blockingModifier;

		Function<Double, Double> armor = new Function<Double, Double>()
		{
			@Override
			public Double apply(Double f)
			{
				// Cauldron start - apply forge armor hook
				if(human)
				{
					return -(f - ArmorPropertiesUM.ApplyArmor(entity, ((EntityPlayer) entity).inventory.armorInventory,
							damagesource, f.floatValue(), false));
				}
				// Cauldron end
				return -(f - ((IMixinEntityLivingBase) entity).applyArmorCalculationsP(damagesource, f.floatValue()));
			}
		};
		float armorModifier = armor.apply((double) damage).floatValue();
		damage += armorModifier;

		Function<Double, Double> resistance = new Function<Double, Double>()
		{
			@Override
			public Double apply(Double f)
			{
				if(!damagesource.isDamageAbsolute() && entity.isPotionActive(Potion.resistance) && damagesource != DamageSource.outOfWorld)
				{
					int i = (entity.getActivePotionEffect(Potion.resistance).getAmplifier() + 1) * 5;
					int j = 25 - i;
					float f1 = f.floatValue() * (float) j;
					return -(f - (f1 / 25.0F));
				}
				return -0.0;
			}
		};
		float resistanceModifier = resistance.apply((double) damage).floatValue();
		damage += resistanceModifier;

		Function<Double, Double> magic = new Function<Double, Double>()
		{
			@Override
			public Double apply(Double f)
			{
				return -(f - ((IMixinEntityLivingBase) entity).applyPotionDamageCalculationsP(damagesource, f.floatValue()));
			}
		};
		float magicModifier = magic.apply((double) damage).floatValue();
		damage += magicModifier;

		Function<Double, Double> absorption = new Function<Double, Double>()
		{
			@Override
			public Double apply(Double f)
			{
				return -(Math.max(f - Math.max(f - entity.getAbsorptionAmount(), 0.0F), 0.0F));
			}
		};
		float absorptionModifier = absorption.apply((double) damage).floatValue();

		return CraftEventFactory.handleLivingEntityDamageEvent(entity, damagesource, originalDamage, hardHatModifier, blockingModifier,
				armorModifier, resistanceModifier, magicModifier, absorptionModifier, hardHat, blocking, armor, resistance, magic, absorption);
	}

	// Non-Living Entities such as EntityEnderCrystal, EntityItemFrame, and EntityFireball need to call this
	public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage)
	{
		if(entity instanceof EntityEnderCrystal && !(source instanceof EntityDamageSource))
		{
			return false;
		}

		final EnumMap<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
		final EnumMap<DamageModifier, Function<? super Double, Double>> functions = new EnumMap(DamageModifier.class);

		modifiers.put(DamageModifier.BASE, damage);
		functions.put(DamageModifier.BASE, ZERO);

		final EntityDamageEvent event = handleEntityDamageEvent(entity, source, modifiers, functions);
		if(event == null)
		{
			return false;
		}
		return event.isCancelled() || (event.getDamage() == 0 && !(entity instanceof EntityItemFrame)); // Cauldron - fix frame removal
	}

	public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel)
	{
		PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(player, oldLevel, newLevel);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

	public static PlayerExpChangeEvent callPlayerExpChangeEvent(net.minecraft.entity.player.EntityPlayer entity, int expAmount)
	{
		Player player = (Player) ((IMixinEntity) entity).getBukkitEntity();
		PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

	public static void handleBlockGrowEvent(net.minecraft.world.World world, int x, int y, int z, net.minecraft.block.Block type, int data)
	{
		Block block = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z);
		CraftBlockState state = (CraftBlockState) block.getState();
		state.setTypeId(net.minecraft.block.Block.getIdFromBlock(type));
		state.setRawData((byte) data);

		BlockGrowEvent event = new BlockGrowEvent(block, state);
		Bukkit.getPluginManager().callEvent(event);

		if(!event.isCancelled())
		{
			state.update(true);
		}
	}

	public static FoodLevelChangeEvent callFoodLevelChangeEvent(net.minecraft.entity.player.EntityPlayer entity, int level)
	{
		FoodLevelChangeEvent event = new FoodLevelChangeEvent((HumanEntity) ((IMixinEntity) entity).getBukkitEntity(), level);
		((IMixinEntity) entity).getBukkitEntity().getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static PigZapEvent callPigZapEvent(net.minecraft.entity.Entity pig, net.minecraft.entity.Entity lightning, net.minecraft.entity.Entity pigzombie)
	{
		PigZapEvent event = new PigZapEvent((Pig) ((IMixinEntity) pig).getBukkitEntity(), (LightningStrike) ((IMixinEntity) lightning).getBukkitEntity(), (PigZombie) ((IMixinEntity) pigzombie).getBukkitEntity());
		((IMixinEntity) pig).getBukkitEntity().getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static HorseJumpEvent callHorseJumpEvent(net.minecraft.entity.Entity horse, float power)
	{
		HorseJumpEvent event = new HorseJumpEvent((Horse) ((IMixinEntity) horse).getBukkitEntity(), power);
		((IMixinEntity) horse).getBukkitEntity().getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static EntityChangeBlockEvent callEntityChangeBlockEvent(org.bukkit.entity.Entity entity, Block block, Material material)
	{
		return callEntityChangeBlockEvent(entity, block, material, 0);
	}

	public static EntityChangeBlockEvent callEntityChangeBlockEvent(net.minecraft.entity.Entity entity, Block block, Material material)
	{
		return callEntityChangeBlockEvent(((IMixinEntity) entity).getBukkitEntity(), block, material, 0);
	}

	public static EntityChangeBlockEvent callEntityChangeBlockEvent(net.minecraft.entity.Entity entity, Block block, Material material, boolean cancelled)
	{
		return callEntityChangeBlockEvent(((IMixinEntity) entity).getBukkitEntity(), block, material, 0, cancelled);
	}

	public static EntityChangeBlockEvent callEntityChangeBlockEvent(net.minecraft.entity.Entity entity, int x, int y, int z, net.minecraft.block.Block type, int data)
	{
		Block block = ((IMixinWorld) entity.worldObj).getWorld().getBlockAt(x, y, z);
		Material material = CraftMagicNumbers.getMaterial(type);

		return callEntityChangeBlockEvent(((IMixinEntity) entity).getBukkitEntity(), block, material, data);
	}

	public static EntityChangeBlockEvent callEntityChangeBlockEvent(org.bukkit.entity.Entity entity, Block block, Material material, int data)
	{
		return callEntityChangeBlockEvent(entity, block, material, data, false);
	}

	public static EntityChangeBlockEvent callEntityChangeBlockEvent(org.bukkit.entity.Entity entity, Block block, Material material, int data, boolean cancelled)
	{
		EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity, block, material, (byte) data);
		event.setCancelled(cancelled);
		entity.getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static CreeperPowerEvent callCreeperPowerEvent(net.minecraft.entity.Entity creeper, net.minecraft.entity.Entity lightning, CreeperPowerEvent.PowerCause cause)
	{
		CreeperPowerEvent event = new CreeperPowerEvent((Creeper) ((IMixinEntity) creeper).getBukkitEntity(), (LightningStrike) ((IMixinEntity) lightning).getBukkitEntity(), cause);
		((IMixinEntity) creeper).getBukkitEntity().getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static EntityTargetEvent callEntityTargetEvent(net.minecraft.entity.Entity entity, net.minecraft.entity.Entity target, EntityTargetEvent.TargetReason reason)
	{
		EntityTargetEvent event = new EntityTargetEvent(((IMixinEntity) entity).getBukkitEntity(), target == null ? null : ((IMixinEntity) target).getBukkitEntity(), reason);
		((IMixinEntity) entity).getBukkitEntity().getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static EntityTargetLivingEntityEvent callEntityTargetLivingEvent(net.minecraft.entity.Entity entity, net.minecraft.entity.EntityLivingBase target, EntityTargetEvent.TargetReason reason)
	{
		EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(((IMixinEntity) entity).getBukkitEntity(), (LivingEntity) ((IMixinEntity) target).getBukkitEntity(), reason);
		((IMixinEntity) entity).getBukkitEntity().getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static EntityBreakDoorEvent callEntityBreakDoorEvent(net.minecraft.entity.Entity entity, int x, int y, int z)
	{
		org.bukkit.entity.Entity entity1 = ((IMixinEntity) entity).getBukkitEntity();
		Block block = entity1.getWorld().getBlockAt(x, y, z);

		EntityBreakDoorEvent event = new EntityBreakDoorEvent((LivingEntity) entity1, block);
		entity1.getServer().getPluginManager().callEvent(event);

		return event;
	}

	//TODO

	// Cauldron start - allow inventory force close to be toggled
	public static net.minecraft.inventory.Container callInventoryOpenEvent(net.minecraft.entity.player.EntityPlayerMP player, net.minecraft.inventory.Container container) {
		return callInventoryOpenEvent(player, container, true);
	}

	public static net.minecraft.inventory.Container callInventoryOpenEvent(net.minecraft.entity.player.EntityPlayerMP player, net.minecraft.inventory.Container container, boolean closeInv) {
		IMixinContainer mixinContainer = (IMixinContainer) container;
		if (mixinContainer.isOpened() || mixinContainer.isClosedByEventCancelling())
			return container;
		if (player.openContainer != player.inventoryContainer && closeInv) { // fire INVENTORY_CLOSE if one already open
	// Cauldron end
			C0DPacketCloseWindow packetCloseWindow = new C0DPacketCloseWindow();
			((IMixinC0DPacketCloseWindow) packetCloseWindow).setWindowId(player.openContainer.windowId);
			player.playerNetServerHandler.processCloseWindow(packetCloseWindow);
		}
		if (player.openContainer  == container)
			player.openContainer = player.inventoryContainer;

		CraftServer server = ((IMixinWorld) player.worldObj).getServer();
		CraftPlayer craftPlayer = (CraftPlayer) ((IMixinEntity) player).getBukkitEntity();
		// Cauldron start - vanilla compatibility
		((IMixinContainer) player.openContainer).transferTo(container, craftPlayer);
		// Cauldron end
		mixinContainer.setOpened(true);
		InventoryOpenEvent event = new InventoryOpenEvent(mixinContainer.getBukkitView());
		if (mixinContainer.getBukkitView() != null) server.getPluginManager().callEvent(event); // Cauldron - allow vanilla mods to bypass

		if (event.isCancelled()) {
			mixinContainer.transferTo(player.openContainer, craftPlayer);
			mixinContainer.setOpened(false);
			mixinContainer.setClosedByEventCancelling(true);
			// Cauldron start - handle close for modded containers
			if (!closeInv) { // fire INVENTORY_CLOSE if one already open
				player.openContainer = container; // make sure the right container is processed
				((IMixinPlayerMP) player).closeScreenSilent();
				player.openContainer = player.inventoryContainer;
			}
			// Cauldron end
			return null;
		}

		return container;
	}

//	public static net.minecraft.item.ItemStack callPreCraftEvent(net.minecraft.inventory.InventoryCrafting matrix, net.minecraft.item.ItemStack result, InventoryView lastCraftView, boolean isRepair) {
//		CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, matrix.resultInventory);
//		inventory.setResult(CraftItemStack.asCraftMirror(result));
//
//		PrepareItemCraftEvent event = new PrepareItemCraftEvent(inventory, lastCraftView, isRepair);
//		Bukkit.getPluginManager().callEvent(event);
//
//		org.bukkit.inventory.ItemStack bitem = event.getInventory().getResult();
//
//		return CraftItemStack.asNMSCopy(bitem);
//	}

	public static ProjectileLaunchEvent callProjectileLaunchEvent(net.minecraft.entity.Entity entity)
	{
		Projectile bukkitEntity = (Projectile) ((IMixinEntity) entity).getBukkitEntity();
		ProjectileLaunchEvent event = new ProjectileLaunchEvent(bukkitEntity);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

	public static ProjectileHitEvent callProjectileHitEvent(net.minecraft.entity.Entity entity)
	{
		ProjectileHitEvent event = new ProjectileHitEvent((Projectile) ((IMixinEntity) entity).getBukkitEntity());
		((IMixinWorld) entity.worldObj).getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static ExpBottleEvent callExpBottleEvent(net.minecraft.entity.Entity entity, int exp)
	{
		ThrownExpBottle bottle = (ThrownExpBottle) ((IMixinEntity) entity).getBukkitEntity();
		ExpBottleEvent event = new ExpBottleEvent(bottle, exp);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

	public static BlockRedstoneEvent callRedstoneChange(net.minecraft.world.World world, int x, int y, int z, int oldCurrent, int newCurrent)
	{
		BlockRedstoneEvent event = new BlockRedstoneEvent(((IMixinWorld) world).getWorld().getBlockAt(x, y, z), oldCurrent, newCurrent);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static NotePlayEvent callNotePlayEvent(net.minecraft.world.World world, int x, int y, int z, byte instrument, byte note)
	{
		NotePlayEvent event = new NotePlayEvent(((IMixinWorld) world).getWorld().getBlockAt(x, y, z), org.bukkit.Instrument.getByType(instrument), new org.bukkit.Note(note));
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static void callPlayerItemBreakEvent(net.minecraft.entity.player.EntityPlayer human, net.minecraft.item.ItemStack brokenItem)
	{
		CraftItemStack item = CraftItemStack.asCraftMirror(brokenItem);
		PlayerItemBreakEvent event = new PlayerItemBreakEvent((Player) ((IMixinEntity) human).getBukkitEntity(), item);
		Bukkit.getPluginManager().callEvent(event);
	}

	public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.world.World world, int x, int y, int z, int igniterX, int igniterY, int igniterZ)
	{
		org.bukkit.World bukkitWorld = ((IMixinWorld) world).getWorld();
		Block igniter = bukkitWorld.getBlockAt(igniterX, igniterY, igniterZ);
		IgniteCause cause;
		switch(igniter.getType())
		{
		case LAVA:
		case STATIONARY_LAVA:
			cause = IgniteCause.LAVA;
			break;
		case DISPENSER:
			cause = IgniteCause.FLINT_AND_STEEL;
			break;
		case FIRE: // Fire or any other unknown block counts as SPREAD.
		default:
			cause = IgniteCause.SPREAD;
		}

		BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), cause, igniter);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.world.World world, int x, int y, int z, net.minecraft.entity.Entity igniter)
	{
		org.bukkit.World bukkitWorld = ((IMixinWorld) world).getWorld();
		org.bukkit.entity.Entity bukkitIgniter = ((IMixinEntity) igniter).getBukkitEntity();
		IgniteCause cause;
		switch(bukkitIgniter.getType())
		{
		case ENDER_CRYSTAL:
			cause = IgniteCause.ENDER_CRYSTAL;
			break;
		case LIGHTNING:
			cause = IgniteCause.LIGHTNING;
			break;
		case SMALL_FIREBALL:
		case FIREBALL:
			cause = IgniteCause.FIREBALL;
			break;
		default:
			cause = IgniteCause.FLINT_AND_STEEL;
		}

		BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), cause, bukkitIgniter);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.world.World world, int x, int y, int z, net.minecraft.world.Explosion explosion)
	{
		org.bukkit.World bukkitWorld = ((IMixinWorld) world).getWorld();
		org.bukkit.entity.Entity igniter = explosion.exploder == null ? null : ((IMixinEntity) explosion.exploder).getBukkitEntity();

		BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), IgniteCause.EXPLOSION, igniter);
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.world.World world, int x, int y, int z, IgniteCause cause, net.minecraft.entity.Entity igniter)
	{
		BlockIgniteEvent event = new BlockIgniteEvent(((IMixinWorld) world).getWorld().getBlockAt(x, y, z), cause, ((IMixinEntity) igniter).getBukkitEntity());
		((IMixinWorld) world).getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static void handleInventoryCloseEvent(net.minecraft.entity.player.EntityPlayer human)
	{
		InventoryView view = ((IMixinContainer) human.openContainer).getBukkitView();
		if(view != null)
		{
			if (human.openContainer == human.inventoryContainer)
			{
				IInventoryTransactionProvider topInventoryProvider = (IInventoryTransactionProvider) ((CraftInventory) view.getTopInventory()).getInventory();
				IInventoryTransactionProvider bottomInventoryProvider = (IInventoryTransactionProvider) ((CraftInventory) view.getBottomInventory()).getInventory();
				if (topInventoryProvider.getViewers().isEmpty())
				{
					topInventoryProvider.onOpen((CraftHumanEntity) ((IMixinEntity) human).getBukkitEntity());
					bottomInventoryProvider.onOpen((CraftHumanEntity) ((IMixinEntity) human).getBukkitEntity());
				}
			}
			((IMixinWorld) human.worldObj).getServer().getPluginManager().callEvent(new InventoryCloseEvent(view)); // Cauldron - allow vanilla mods to bypass
		}
		((IMixinContainer) human.openContainer).transferTo(human.inventoryContainer, (CraftHumanEntity) ((IMixinEntity) human).getBukkitEntity());
		((IMixinContainer) human.openContainer).setOpened(false);
	}

	public static void handleEditBookEvent(net.minecraft.entity.player.EntityPlayerMP player, net.minecraft.item.ItemStack newBookItem)
	{
		int itemInHandIndex = player.inventory.currentItem;

		PlayerEditBookEvent editBookEvent = new PlayerEditBookEvent((Player) ((IMixinEntity) player).getBukkitEntity(), player.inventory.currentItem, (BookMeta) CraftItemStack.getItemMeta(player.inventory.getCurrentItem()), (BookMeta) CraftItemStack.getItemMeta(newBookItem), newBookItem.getItem() == net.minecraft.init.Items.written_book);
		((IMixinWorld) player.worldObj).getServer().getPluginManager().callEvent(editBookEvent);
		net.minecraft.item.ItemStack itemInHand = player.inventory.getStackInSlot(itemInHandIndex);

		// If they've got the same item in their hand, it'll need to be updated.
		if(itemInHand != null && itemInHand.getItem() == net.minecraft.init.Items.writable_book)
		{
			if(!editBookEvent.isCancelled())
			{
				CraftItemStack.setItemMeta(itemInHand, editBookEvent.getNewBookMeta());
				if(editBookEvent.isSigning())
				{
					itemInHand.func_150996_a(net.minecraft.init.Items.written_book);
				}
			}

			// Client will have updated its idea of the book item; we need to overwrite that
			net.minecraft.inventory.Slot slot = player.openContainer.getSlotFromInventory((net.minecraft.inventory.IInventory) player.inventory, itemInHandIndex);
			player.playerNetServerHandler.sendPacket(new net.minecraft.network.play.server.S2FPacketSetSlot(player.openContainer.windowId, slot.slotNumber, itemInHand));
		}
	}

	public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(net.minecraft.entity.EntityLiving entity, net.minecraft.entity.player.EntityPlayer player)
	{
		PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(((IMixinEntity) entity).getBukkitEntity(), (Player) ((IMixinEntity) player).getBukkitEntity());
		((IMixinWorld) entity.worldObj).getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static PlayerLeashEntityEvent callPlayerLeashEntityEvent(net.minecraft.entity.EntityLiving entity, net.minecraft.entity.Entity leashHolder, net.minecraft.entity.player.EntityPlayer player)
	{
		PlayerLeashEntityEvent event = new PlayerLeashEntityEvent(((IMixinEntity) entity).getBukkitEntity(), ((IMixinEntity) leashHolder).getBukkitEntity(), (Player) ((IMixinEntity) player).getBukkitEntity());
		((IMixinWorld) entity.worldObj).getServer().getPluginManager().callEvent(event);
		return event;
	}

	public static Cancellable handleStatisticsIncrease(EntityPlayer entityHuman, net.minecraft.stats.StatBase statistic, int current, int incrementation)
	{
		Player player = (Player) ((IMixinEntity) entityHuman).getBukkitEntity();
		Event event;
		if(statistic instanceof net.minecraft.stats.Achievement)
		{
			if(current != 0)
			{
				return null;
			}
			event = new PlayerAchievementAwardedEvent(player, CraftStatistic.getBukkitAchievement((net.minecraft.stats.Achievement) statistic));
		}
		else
		{
			org.bukkit.Statistic stat = CraftStatistic.getBukkitStatistic(statistic);
			if(stat == null)
			{
				return null;
			}
			switch(stat)
			{
			case FALL_ONE_CM:
			case BOAT_ONE_CM:
			case CLIMB_ONE_CM:
			case DIVE_ONE_CM:
			case FLY_ONE_CM:
			case HORSE_ONE_CM:
			case MINECART_ONE_CM:
			case PIG_ONE_CM:
			case PLAY_ONE_TICK:
			case SWIM_ONE_CM:
			case WALK_ONE_CM:
				// Do not process event for these - too spammy
				return null;
			default:
			}
			if(stat.getType() == Type.UNTYPED)
			{
				event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation);
			}
			else if(stat.getType() == Type.ENTITY)
			{
				EntityType entityType = CraftStatistic.getEntityTypeFromStatistic(statistic);
				event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, entityType);
			}
			else
			{
				Material material = CraftStatistic.getMaterialFromStatistic(statistic);
				event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, material);
			}
		}
		((IMixinWorld) entityHuman.worldObj).getServer().getPluginManager().callEvent(event);
		return (Cancellable) event;
	}

	// Cauldron start
	public static BlockBreakEvent callBlockBreakEvent(net.minecraft.world.World world, int x, int y, int z, net.minecraft.block.Block block, int blockMetadata, net.minecraft.entity.player.EntityPlayerMP player)
	{
		org.bukkit.block.Block bukkitBlock = ((IMixinWorld) world).getWorld().getBlockAt(x, y, z);
		org.bukkit.event.block.BlockBreakEvent blockBreakEvent = new org.bukkit.event.block.BlockBreakEvent(bukkitBlock, (Player) ((IMixinEntity) player).getBukkitEntity());
		EntityPlayerMP playermp = (EntityPlayerMP) player;
		if(!(playermp instanceof FakePlayer))
		{
			if(!(playermp.theItemInWorldManager.getGameType().isAdventure() && !playermp.isCurrentToolAdventureModeExempt(x, y, z)) && !(playermp.theItemInWorldManager.getGameType().isCreative() && playermp.getHeldItem() != null && playermp.getHeldItem().getItem() instanceof ItemSword))
			{
				int exp = 0;
				if(!(block == null || !player.canHarvestBlock(block) || // Handle empty block or player unable to break block scenario
						block.canSilkHarvest(world, player, x, y, z, blockMetadata) && EnchantmentHelper.getSilkTouchModifier(player))) // If the block is being silk harvested, the exp dropped is 0
				{
					int meta = block.getDamageValue(world, x, y, z);
					int bonusLevel = EnchantmentHelper.getFortuneModifier(player);
					exp = block.getExpDrop(world, meta, bonusLevel);
				}
				blockBreakEvent.setExpToDrop(exp);
			}
			else blockBreakEvent.setCancelled(true);
		}

		((IMixinWorld) world).getServer().getPluginManager().callEvent(blockBreakEvent);
		return blockBreakEvent;
	}
	// Cauldron end
}