package org.ultramine.mods.bukkit.mixin.world;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.generator.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.mods.bukkit.interfaces.world.IMixinWorld;
import org.ultramine.server.event.WorldEventProxy;
import org.ultramine.server.event.WorldUpdateObject;
import org.ultramine.server.event.WorldUpdateObjectType;

@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements IMixinWorld
{
	@Shadow
	public WorldProvider provider;
	@Shadow(remap = false)
	public boolean restoringBlockSnapshots;

	@Shadow
	protected abstract IChunkProvider createChunkProvider();

	@Shadow
	public abstract WorldInfo getWorldInfo();

	@Shadow
	public abstract boolean setBlock(int x, int y, int z, Block type, int meta, int flags);

	@Shadow
	public abstract Block getBlock(int x, int y, int z);

	@Shadow
	public abstract boolean setBlockMetadataWithNotify(int x, int y, int z, int meta, int flags);

	@Shadow
	public abstract int getBlockMetadata(int x, int y, int z);

	@Shadow
	public abstract boolean checkNoEntityCollision(AxisAlignedBB p_72855_1_);

	@Shadow
	public abstract boolean checkNoEntityCollision(AxisAlignedBB p_72917_1_, Entity p_72917_2_);

	@Shadow
	public abstract boolean spawnEntityInWorld(Entity entity);

	private ChunkGenerator generator;
	private CraftWorld craftWorld;

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createChunkProvider()Lnet/minecraft/world/chunk/IChunkProvider;"))
	public IChunkProvider createChunkProviderReplacement(World world)
	{
		if(!((Object) this instanceof WorldServer) || getServer() == null)
			return createChunkProvider();
		int providerId = DimensionManager.getProviderType(provider.dimensionId);
		@SuppressWarnings("deprecation")
		Environment env = Environment.getEnvironment(providerId);
		env.getClass(); //NPE
		WorldInfo info = getWorldInfo();
		initBukkit(info == null ? null : getServer().getGenerator(info.getWorldName()), env);

		//

		IChunkLoader ichunkloader = world.getSaveHandler().getChunkLoader(provider);
		if(provider.getClass().toString().startsWith("net.minecraft."))
		{
			// CraftBukkit start
			org.bukkit.craftbukkit.generator.InternalChunkGenerator gen;

			if(this.generator != null)
			{
				gen = new org.bukkit.craftbukkit.generator.CustomChunkGenerator(world, world.getSeed(), this.generator);
			}
			else if(provider instanceof WorldProviderHell)
			{
				gen = new org.bukkit.craftbukkit.generator.NetherChunkGenerator(world, world.getSeed());
			}
			else if(provider instanceof WorldProviderEnd)
			{
				gen = new org.bukkit.craftbukkit.generator.SkyLandsChunkGenerator(world, world.getSeed());
			}
			else
			{
				gen = new org.bukkit.craftbukkit.generator.NormalChunkGenerator(world, world.getSeed());
			}
			((WorldServer) world).theChunkProviderServer = new ChunkProviderServer((WorldServer) world, ichunkloader, gen);
			// CraftBukkit end
		}
		else
		{
			((WorldServer) world).theChunkProviderServer = new ChunkProviderServer((WorldServer) world, ichunkloader, provider.createChunkGenerator());
		}

		return ((WorldServer) world).theChunkProviderServer;
	}

	private void initBukkit(ChunkGenerator generator, org.bukkit.World.Environment env)
	{
		this.generator = generator;
		this.craftWorld = new CraftWorld((WorldServer) (Object) this, generator, env);

		if(generator != null)
			getWorld().getPopulators().addAll(generator.getDefaultPopulators(getWorld()));
		getServer().addWorld(craftWorld);
	}

	@Overwrite
	@SuppressWarnings("deprecation")
	public boolean canPlaceEntityOnSide(Block p_147472_1_, int p_147472_2_, int p_147472_3_, int p_147472_4_, boolean p_147472_5_, int p_147472_6_, Entity p_147472_7_, ItemStack p_147472_8_)
	{
		Block block1 = this.getBlock(p_147472_2_, p_147472_3_, p_147472_4_);
		if(block1 == null) return false; // Cauldron
		AxisAlignedBB axisalignedbb = p_147472_5_ ? null : p_147472_1_.getCollisionBoundingBoxFromPool((World) (Object) this, p_147472_2_, p_147472_3_, p_147472_4_);
		// CraftBukkit start - store default return
		boolean defaultReturn = axisalignedbb != null && !this.checkNoEntityCollision(axisalignedbb, p_147472_7_) ? false
				: (block1.getMaterial() == Material.circuits && p_147472_1_ == Blocks.anvil ? true : block1.isReplaceable((World) (Object) this, p_147472_2_, p_147472_3_,
				p_147472_4_) && p_147472_1_.canReplace((World) (Object) this, p_147472_2_, p_147472_3_, p_147472_4_, p_147472_6_, p_147472_8_));
		BlockCanBuildEvent event = new BlockCanBuildEvent(this.getWorld().getBlockAt(p_147472_2_, p_147472_3_, p_147472_4_),
				CraftMagicNumbers.getId(p_147472_1_), defaultReturn);
		this.getServer().getPluginManager().callEvent(event);
		return event.isBuildable();
		// CraftBukkit end
	}

	// this method is used by ForgeMultipart and Immibis's Microblocks
	@Override
	public boolean canPlaceMultipart(Block block, int x, int y, int z)
	{
		BlockPlaceEvent placeEvent = null;
		WorldUpdateObject wuo = WorldEventProxy.getCurrent().getUpdateObject();
		if(wuo.getType() == WorldUpdateObjectType.PLAYER)
		{
			placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent((World) (Object) this, (EntityPlayer) wuo.getEntity(),
					org.bukkit.craftbukkit.block.CraftBlockState.getBlockState((World) (Object) this, x, y, z, 3), x, y, z);
		}

		if(placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild()))
		{
			return false;
		}

		return true;
	}

	@Override
	public CraftWorld getWorld()
	{
		return craftWorld;
	}

	@Override
	public CraftServer getServer()
	{
		return (CraftServer) Bukkit.getServer();
	}

	@Override
	public boolean addEntity(Entity entity, SpawnReason spawnReason)
	{
		if(entity == null || (this.restoringBlockSnapshots && entity instanceof EntityItem))
			return false;
		IMixinEntity mentity = (IMixinEntity) entity;
		org.bukkit.event.Cancellable event = null;
		// Cauldron start - workaround for handling CraftBukkit's SpawnReason with customspawners and block spawners
		String strSpawnReason = mentity.getSpawnReason();
		if(strSpawnReason != null)
		{
			if(strSpawnReason.equals("natural"))
			{
				spawnReason = SpawnReason.NATURAL;
			}
			else if(strSpawnReason.equals("spawner"))
			{
				spawnReason = SpawnReason.SPAWNER;
			}
		}
		// Cauldron end

		if(entity instanceof EntityLivingBase && !(entity instanceof EntityPlayerMP))
		{
			// Cauldron start - add custom entity support
//			boolean isAnimal = p_72838_1_ instanceof EntityAnimal || p_72838_1_ instanceof EntityWaterMob || p_72838_1_ instanceof EntityGolem
//					|| p_72838_1_.isCreatureType(EnumCreatureType.creature, false);
//			boolean isMonster = p_72838_1_ instanceof EntityMob || p_72838_1_ instanceof EntityGhast || p_72838_1_ instanceof EntitySlime
//					|| p_72838_1_.isCreatureType(EnumCreatureType.monster, false);
			// Cauldron end

			event = CraftEventFactory.callCreatureSpawnEvent((EntityLivingBase) entity, spawnReason);
		}
		else if(entity instanceof EntityItem)
		{
			event = CraftEventFactory.callItemSpawnEvent((EntityItem) entity);
		}
		else if(mentity.getBukkitEntity() instanceof org.bukkit.entity.Projectile)
		{
			// Not all projectiles extend EntityProjectile, so check for Bukkit interface instead
			event = CraftEventFactory.callProjectileLaunchEvent(entity);
		}

		if(event != null && (event.isCancelled() || entity.isDead))
		{
			entity.isDead = true;
			return false;
		}

		return real_spawnEntityInWorld(entity);
	}

	// must be public to be compatible with some mods using world proxy
	public abstract boolean real_spawnEntityInWorld(Entity entity);

	@Override
	public boolean setRawTypeId(int x, int y, int z, int typeId)
	{
		return this.setBlock(x, y, z, Block.getBlockById(typeId), 0, 4);
	}

	@Override
	public boolean setRawTypeIdAndData(int x, int y, int z, int typeId, int data)
	{
		return this.setBlock(x, y, z, Block.getBlockById(typeId), data, 4);
	}

	@Override
	public boolean setTypeId(int x, int y, int z, int typeId)
	{
		return this.setBlock(x, y, z, Block.getBlockById(typeId), 0, 3);
	}

	@Override
	public boolean setTypeIdAndData(int x, int y, int z, int typeId, int data)
	{
		return this.setBlock(x, y, z, Block.getBlockById(typeId), data, 3);
	}

	@Override
	public int getTypeId(int x, int y, int z)
	{
		return Block.getIdFromBlock(getBlock(x, y, z));
	}

	@Override
	public boolean setTypeAndData(int x, int y, int z, Block block, int data, int flag)
	{
		return this.setBlock(x, y, z, block, data, flag);
	}

	@Override
	public boolean setData(int x, int y, int z, int data, int flag)
	{
		return this.setBlockMetadataWithNotify(x, y, z, data, flag);
	}

	@Override
	public int getData(int x, int y, int z)
	{
		return this.getBlockMetadata(x, y, z);
	}

	@Override
	public Block getType(int x, int y, int z)
	{
		return this.getBlock(x, y, z);
	}
}
