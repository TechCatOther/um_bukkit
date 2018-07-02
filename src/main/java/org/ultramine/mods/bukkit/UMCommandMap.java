package org.ultramine.mods.bukkit;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.ultramine.commands.IExtendedCommand;
import org.ultramine.mods.bukkit.util.BukkitUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UMCommandMap extends SimpleCommandMap
{
	private final Set<Command> registeredBukkitCommands = new HashSet<Command>();

	public UMCommandMap(Server server)
	{
		super(server);
	}

	@Override
	public void registerAll(String fallbackPrefix, List<Command> commands)
	{
		if(commands != null)
		{
			for(Command c : commands)
			{
				register(fallbackPrefix, c);
			}
		}
	}

	@Override
	public boolean register(String fallbackPrefix, Command command)
	{
		return register(command.getName(), fallbackPrefix, command);
	}

	@Override
	public boolean register(String label, String fallbackPrefix, Command command)
	{
		label = label.toLowerCase().trim();
		fallbackPrefix = fallbackPrefix.toLowerCase().trim();
		registeredBukkitCommands.add(command);
		command.getAliases().remove(label);
		BukkitCommand cmd = new BukkitCommand(fallbackPrefix, label, command);
		((CommandHandler) MinecraftServer.getServer().getCommandManager()).getRegistry().registerCommand(cmd);
		return true;
	}

	@Override
	public boolean dispatch(CommandSender sender, String cmdLine) throws CommandException
	{
		return MinecraftServer.getServer().getCommandManager().executeCommand(BukkitUtil.toVanillaCommandSender(sender), cmdLine) != 0;
	}

	@Override
	public void clearCommands()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Command getCommand(String name)
	{
		IExtendedCommand cmd = ((CommandHandler) MinecraftServer.getServer().getCommandManager()).getRegistry().get(name);
		return cmd == null || !(cmd instanceof BukkitCommand) ? null : ((BukkitCommand) cmd).getBukkitCommand();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> tabComplete(CommandSender sender, String cmdLine) throws IllegalArgumentException
	{
		return MinecraftServer.getServer().getPossibleCompletions(BukkitUtil.toVanillaCommandSender(sender), cmdLine);
	}

	@Override
	public void setFallbackCommands()
	{
		// TODO
	}

	protected void setDefaultCommands()
	{
		// TODO
	}

	@Override
	public Collection<Command> getCommands()
	{
		return registeredBukkitCommands;
	}

	@Override
	public void registerServerAliases()
	{
		// TODO
	}

	private static class BukkitCommand implements IExtendedCommand
	{
		private final String group;
		private final String name;
		private final Command bukkitCommand;

		private BukkitCommand(String group, String name, Command bukkitCommand)
		{
			this.group = group;
			this.name = name;
			this.bukkitCommand = bukkitCommand;
		}

		public Command getBukkitCommand()
		{
			return bukkitCommand;
		}

		@Override
		public String getCommandName()
		{
			return name;
		}

		@Override
		public String getCommandUsage(ICommandSender sender)
		{
			return bukkitCommand.getUsage() == null ? null : bukkitCommand.getUsage().replace("<command>", bukkitCommand.getLabel()).trim();
		}

		@Override
		public List getCommandAliases()
		{
			return bukkitCommand.getAliases();
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args)
		{
			bukkitCommand.execute(BukkitUtil.toBukkitCommandSender(sender), bukkitCommand.getLabel(), args);
		}

		@Override
		public boolean canCommandSenderUseCommand(ICommandSender sender)
		{
			return bukkitCommand.testPermissionSilent(BukkitUtil.toBukkitCommandSender(sender));
		}

		@Override
		public List addTabCompletionOptions(ICommandSender sender, String[] args)
		{
			return bukkitCommand.tabComplete(BukkitUtil.toBukkitCommandSender(sender), bukkitCommand.getLabel(), args);
		}

		@Override
		public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_)
		{
			return false;
		}

		@Override
		public int compareTo(Object o)
		{
			if (o instanceof IExtendedCommand)
			{
				int result = getGroup().compareTo(((IExtendedCommand) o).getGroup());
				if (result == 0)
					result = getCommandName().compareTo(((IExtendedCommand) o).getCommandName());

				return result;
			}
			return -1;
		}

		@Override
		public String getDescription()
		{
			return bukkitCommand.getDescription();
		}

		@Override
		public String getGroup()
		{
			return group;
		}
	}
}
