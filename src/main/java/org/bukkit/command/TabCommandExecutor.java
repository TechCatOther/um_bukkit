package org.bukkit.command;

import java.util.List;

/**
 * Represents a class which can handle command tab completion and commands
 *
 * @see TabExecutor
 * @deprecated Remains for plugins that would have implemented it even without
 * functionality
 */
@Deprecated
public interface TabCommandExecutor extends CommandExecutor
{
	public List<String> onTabComplete();

}