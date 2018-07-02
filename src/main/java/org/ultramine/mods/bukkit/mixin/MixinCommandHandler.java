package org.ultramine.mods.bukkit.mixin;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.command.CraftRemoteConsoleCommandSender;
import org.bukkit.craftbukkit.entity.CraftMinecartCommand;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ultramine.commands.CommandRegistry;
import org.ultramine.mods.bukkit.interfaces.entity.IMixinEntity;
import org.ultramine.server.event.WorldEventProxy;
import org.ultramine.server.event.WorldUpdateObject;
import org.ultramine.server.event.WorldUpdateObjectType;

@Mixin(net.minecraft.command.CommandHandler.class)
public abstract class MixinCommandHandler
{
	@Shadow
	private static Logger logger;
	@Shadow(remap = false)
	private CommandRegistry registry;

	@Shadow
	public abstract int executeCommand(ICommandSender sender, String line);

	@Inject(method = "executeCommand", cancellable = true, at = @At("HEAD"))
	public void onExecuteCommand(ICommandSender sender, String line, CallbackInfoReturnable<Integer> ci)
	{
		line = line.trim();

		CraftServer server = (CraftServer) Bukkit.getServer();
		CommandSender bukkitSender;
		boolean tryConversation = false;
		if(sender instanceof EntityPlayerMP)
		{
			PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent((Player) ((IMixinEntity) sender).getBukkitEntity(), line, new LazyPlayerSet());
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(event.isCancelled())
				ci.setReturnValue(0);
			line = event.getMessage();
			bukkitSender = (Player) ((IMixinEntity) sender).getBukkitEntity();
		}
		else if(sender instanceof CommandBlockLogic)
		{
			WorldUpdateObject wuo = WorldEventProxy.getCurrent().getUpdateObject();
			if(wuo.getType() == WorldUpdateObjectType.TILEE_ENTITY)
				bukkitSender = new CraftBlockCommandSender((CommandBlockLogic) sender);
			else if(wuo.getType() == WorldUpdateObjectType.ENTITY)
				bukkitSender = new CraftMinecartCommand(server, (net.minecraft.entity.EntityMinecartCommandBlock) wuo.getEntity());
			else
				bukkitSender = server.getConsoleSender();
		}
		else if(sender instanceof RConConsoleSource)
		{
			bukkitSender = CraftRemoteConsoleCommandSender.getInstance();
			RemoteServerCommandEvent event = new RemoteServerCommandEvent(bukkitSender, line);
			server.getPluginManager().callEvent(event);
			line = event.getCommand();
			tryConversation = true;
		}
		else
		{
			ServerCommandEvent event = new ServerCommandEvent(server.getConsoleSender(), line);
			server.getPluginManager().callEvent(event);
			line = event.getCommand();
			bukkitSender = server.getConsoleSender();
			tryConversation = true;
		}

		String trimmedLine = line;
		if(line.startsWith("/"))
			trimmedLine = line.substring(1);

		if(tryConversation && server.tryConversation(bukkitSender, trimmedLine))
			ci.setReturnValue(1);
	}

	private int dispatchBukkit(ICommandSender sender, CommandSender bukkitSender, String line, boolean tryConversation)
	{
		CraftServer server = (CraftServer) Bukkit.getServer();

		int ret = 0;

		try
		{
			//TODO support multiple
			ret += (tryConversation && server.tryConversation(bukkitSender, line)) ? 1 : server.dispatchCommand(bukkitSender, line) ? 1 : 0;
		} catch(org.bukkit.command.CommandException ex)
		{
			if(bukkitSender instanceof Player)
			{
				bukkitSender.sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
				logger.error("An internal error occurred while attempting to perform this command", ex);
			}
			else if(bukkitSender instanceof CraftBlockCommandSender)
			{
				CommandBlockLogic listener = (CommandBlockLogic) sender;
				logger.warn(String.format("CommandBlock at (%d,%d,%d) failed to handle command", listener.getPlayerCoordinates().posX, listener.getPlayerCoordinates().posY, listener.getPlayerCoordinates().posZ), ex);
			}
			else if(bukkitSender instanceof CraftMinecartCommand)
			{
				CommandBlockLogic listener = (CommandBlockLogic) sender;
				logger.warn(String.format("MinecartCommandBlock at (%d,%d,%d) failed to handle command", listener.getPlayerCoordinates().posX, listener.getPlayerCoordinates().posY, listener.getPlayerCoordinates().posZ), ex);
			}
			else
			{
				logger.warn("Unknown sender failed to handle command", ex);
			}
		}

		return ret;
	}
}
