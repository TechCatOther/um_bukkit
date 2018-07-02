package org.bukkit.command.defaults;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class StopCommand extends VanillaCommand
{
	public StopCommand()
	{
		super("stop");
		this.description = "Stops the server with optional reason";
		this.usageMessage = "/stop [reason]";
		this.setPermission("bukkit.command.stop");
	}

	@Override
	public boolean execute(CommandSender sender, String currentAlias, String[] args)
	{
		if(!testPermission(sender)) return true;

		DedicatedServer.allowPlayerLogins = false; // KCauldron - immediately disable logins

		Command.broadcastCommandMessage(sender, "Stopping the server..");

		String reason = this.createString(args, 0);
		if(StringUtils.isNotEmpty(reason))
		{
			for(Player player : ImmutableList.copyOf(Bukkit.getOnlinePlayers()))
			{
				player.kickPlayer(reason);
			}
		}

		Bukkit.shutdown(); // KCauldron - shutdown server after all players kicked

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException
	{
		Validate.notNull(sender, "Sender cannot be null");
		Validate.notNull(args, "Arguments cannot be null");
		Validate.notNull(alias, "Alias cannot be null");

		return ImmutableList.of();
	}
}