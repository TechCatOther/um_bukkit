package org.ultramine.mods.bukkit.mixin.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.world.IMixinChunk;

@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class MixinChunk implements IMixinChunk
{
	public CraftChunk bukkitChunk;

	public CraftChunk getBukkitChunk()
	{
		return bukkitChunk;
	}

	public void setBukkitChunk(CraftChunk bukkitChunk)
	{
		this.bukkitChunk = bukkitChunk;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onConstructed(World world, int cx, int cz, CallbackInfo ci)
	{
		if(!((Object) this instanceof EmptyChunk))
			bukkitChunk = new CraftChunk((Chunk) (Object) this);
	}
}
