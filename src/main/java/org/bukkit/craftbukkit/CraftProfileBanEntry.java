package org.bukkit.craftbukkit;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.mods.bukkit.interfaces.management.IMixinBanEntry;

import java.io.IOException;
import java.util.Date;

public final class CraftProfileBanEntry implements org.bukkit.BanEntry
{
	private static final Logger log = LogManager.getLogger();
	private final UserListBans list;
	private final GameProfile profile;
	private Date created;
	private String source;
	private Date expiration;
	private String reason;

	public CraftProfileBanEntry(GameProfile profile, UserListBansEntry entry, UserListBans list)
	{
		this.list = list;
		this.profile = profile;
		this.created = ((IMixinBanEntry) entry).getCreated() != null ? new Date(((IMixinBanEntry) entry).getCreated().getTime()) : null;
		this.source = ((IMixinBanEntry) entry).getSource();
		this.expiration = entry.getBanEndDate() != null ? new Date(entry.getBanEndDate().getTime()) : null;
		this.reason = entry.getBanReason();
	}

	@Override
	public String getTarget()
	{
		return this.profile.getName();
	}

	@Override
	public Date getCreated()
	{
		return this.created == null ? null : (Date) this.created.clone();
	}

	@Override
	public void setCreated(Date created)
	{
		this.created = created;
	}

	@Override
	public String getSource()
	{
		return this.source;
	}

	@Override
	public void setSource(String source)
	{
		this.source = source;
	}

	@Override
	public Date getExpiration()
	{
		return this.expiration == null ? null : (Date) this.expiration.clone();
	}

	@Override
	public void setExpiration(Date expiration)
	{
		if(expiration != null && expiration.getTime() == new Date(0, 0, 0, 0, 0, 0).getTime())
		{
			expiration = null; // Forces "forever"
		}

		this.expiration = expiration;
	}

	@Override
	public String getReason()
	{
		return this.reason;
	}

	@Override
	public void setReason(String reason)
	{
		this.reason = reason;
	}

	@Override
	public void save()
	{
		UserListBansEntry entry = new UserListBansEntry(profile, this.created, this.source, this.expiration, this.reason);
		this.list.func_152687_a(entry);
		try
		{
			this.list.func_152678_f();
		} catch(IOException ex)
		{
			log.error("Failed to save banned-players.json, " + ex.getMessage());
		}
	}
}
