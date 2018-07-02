package org.ultramine.mods.bukkit.injected.internal;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.account.Account;
import org.ultramine.core.economy.account.PlayerAccount;
import org.ultramine.core.economy.exception.AccountTypeNotSupportedException;
import org.ultramine.core.economy.holdings.Holdings;
import org.ultramine.core.economy.holdings.HoldingsFactory;
import org.ultramine.core.economy.service.EconomyRegistry;
import org.ultramine.core.permissions.Permissions;
import org.ultramine.core.service.ServiceBytecodeAdapter;
import org.ultramine.core.service.ServiceManager;
import org.ultramine.mods.bukkit.injected.internal.economy.VaultEconomyPlayerHoldings;
import org.ultramine.mods.bukkit.injected.internal.permissions.c2b.BukkitPermissionsServiceLoader;

import javax.annotation.Nonnull;

public class InjectedUltramineCorePlugin extends JavaPlugin
{
	// @InjectService is not working in plugin environment
	private static final ServiceManager services = (ServiceManager) ServiceBytecodeAdapter.provideService(ServiceManager.class);
	private static final EconomyRegistry economyRegistry = (EconomyRegistry) ServiceBytecodeAdapter.provideService(EconomyRegistry.class);

	@Override
	public void onEnable()
	{
		if(getServer().getPluginManager().getPlugin("Vault") != null)
			setupVault();
	}

	private void setupVault()
	{
		setupVaultPermissions();
		if(!setupVaultEconomy())
			getLogger().info("Vault economy was not set up");
	}

	private void setupVaultPermissions()
	{
		services.register(Permissions.class, new BukkitPermissionsServiceLoader(getServer()), 100);
	}

	private boolean setupVaultEconomy()
	{
		RegisteredServiceProvider<Economy> regEconomy = getServer().getServicesManager().getRegistration(Economy.class);
		if(regEconomy == null)
			return false;
		final Economy economy = regEconomy.getProvider();
		if(economy == null)
			return false;

		int fractionalDigits = economy.fractionalDigits();
		if(fractionalDigits == -1)
			fractionalDigits = 8;

		economyRegistry.registerCurrency("bukkit", economy.currencyNameSingular(), economy.currencyNamePlural(), "$", fractionalDigits, 100, new HoldingsFactory()
		{
			@Nonnull
			@Override
			public Holdings createHoldings(@Nonnull Account account, @Nonnull Currency currency) throws AccountTypeNotSupportedException
			{
				if(!(account instanceof PlayerAccount))
					throw new AccountTypeNotSupportedException(account, currency);
				PlayerAccount playerAccount = (PlayerAccount) account;
				return new VaultEconomyPlayerHoldings(economy, playerAccount, getServer().getOfflinePlayer(playerAccount.getProfile().getId()), currency);
			}
		});

		return true;
	}
}
