package org.bukkit.craftbukkit;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.IPBanEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Set;

public class CraftIpBanList implements org.bukkit.BanList
{
	private static final Logger log = LogManager.getLogger();
	private final BanList list;

	public CraftIpBanList(BanList list)
	{
		this.list = list;
	}

	@Override
	public org.bukkit.BanEntry getBanEntry(String target)
	{
		Validate.notNull(target, "Target cannot be null");

		IPBanEntry entry = (IPBanEntry) list.func_152683_b(target);
		if(entry == null)
		{
			return null;
		}

		return new CraftIpBanEntry(target, entry, list);
	}

	@Override
	public org.bukkit.BanEntry addBan(String target, String reason, Date expires, String source)
	{
		Validate.notNull(target, "Ban target cannot be null");

		IPBanEntry entry = new IPBanEntry(target, new Date(),
				StringUtils.isBlank(source) ? null : source, expires,
				StringUtils.isBlank(reason) ? null : reason);

		list.func_152687_a(entry);

		try
		{
			list.func_152678_f();
		} catch(IOException ex)
		{
			log.error("Failed to save banned-ips.json, " + ex.getMessage());
		}

		return new CraftIpBanEntry(target, entry, list);
	}

	@Override
	public Set<org.bukkit.BanEntry> getBanEntries()
	{
		ImmutableSet.Builder<org.bukkit.BanEntry> builder = ImmutableSet.builder();
		for(String target : list.func_152685_a())
		{
			builder.add(new CraftIpBanEntry(target, (IPBanEntry) list.func_152683_b(target), list));
		}

		return builder.build();
	}

	@Override
	public boolean isBanned(String target)
	{
		Validate.notNull(target, "Target cannot be null");

		return list.func_152708_a(InetSocketAddress.createUnresolved(target, 0));
	}

	@Override
	public void pardon(String target)
	{
		Validate.notNull(target, "Target cannot be null");

		list.func_152684_c(target);
	}
}
