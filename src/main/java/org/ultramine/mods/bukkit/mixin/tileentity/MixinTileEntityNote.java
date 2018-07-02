package org.ultramine.mods.bukkit.mixin.tileentity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.NotePlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityNote.class)
public class MixinTileEntityNote
{
	/**
	 * @author	AtomicInteger
	 */
	@Redirect(method = "triggerNote", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEvent(IIILnet/minecraft/block/Block;II)V"))
	public void addBlockEventRedirect(World world, int x, int y, int z, Block block, int instrument, int note)
	{
		NotePlayEvent event = CraftEventFactory.callNotePlayEvent(world, x, y, z, (byte) instrument, (byte) note);
		if (!event.isCancelled())
			world.addBlockEvent(x, y, z, block, event.getInstrument().getType(), event.getNote().getId());
	}
}