package org.ultramine.mods.bukkit.injected.internal.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.ultramine.core.economy.account.Account;
import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.holdings.Holdings;
import org.ultramine.core.economy.account.PlayerAccount;
import org.ultramine.core.economy.exception.EconomyException;
import org.ultramine.core.economy.exception.InsufficientFundsException;
import org.ultramine.core.economy.exception.NegativeAmountException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.DoubleUnaryOperator;

public class VaultEconomyPlayerHoldings implements Holdings
{
	private final Economy economy;
	private final PlayerAccount account;
	private final OfflinePlayer player;
	private final Currency currency;

	public VaultEconomyPlayerHoldings(Economy economy, PlayerAccount account, OfflinePlayer player, Currency currency)
	{
		this.economy = economy;
		this.account = account;
		this.player = player;
		this.currency = currency;
	}

	@Nonnull
	@Override
	public Account getAccount()
	{
		return account;
	}

	@Nonnull
	@Override
	public Currency getCurrency()
	{
		return currency;
	}

	@Override
	public double getBalance()
	{
		ensureAccountExists();
		return economy.getBalance(player);
	}

	@Override
	public double setBalance(double newBalance, @Nullable String comment)
	{
		ensureAccountExists();
		double lastBalance = getBalance();
		double toAdd = newBalance - lastBalance;
		EconomyResponse resp = null;
		if(toAdd < 0)
			resp = economy.withdrawPlayer(player, -toAdd);
		else if(toAdd > 0)
			resp = economy.depositPlayer(player, toAdd);
		if(resp != null && !resp.transactionSuccess())
			throw new EconomyException(resp.errorMessage);
		return lastBalance;
	}

	@Override
	public double deposit(double amount, @Nullable String comment) throws NegativeAmountException
	{
		checkAmount(amount);
		ensureAccountExists();
		EconomyResponse resp = economy.depositPlayer(player, amount);
		if(!resp.transactionSuccess())
			throw new EconomyException(resp.errorMessage);
		return resp.balance;
	}

	@Override
	public double withdrawUnchecked(double amount, @Nullable String comment) throws NegativeAmountException
	{
		checkAmount(amount);
		ensureAccountExists();
		EconomyResponse resp = economy.withdrawPlayer(player, amount);
		if(!resp.transactionSuccess())
			throw new EconomyException(resp.errorMessage);
		return resp.balance;
	}

	@Override
	public double withdraw(double amount, @Nullable String comment) throws NegativeAmountException, InsufficientFundsException
	{
		checkAmount(amount);
		ensureAccountExists();
		double balance = getBalance();
		if(balance - amount < 0)
			throw new InsufficientFundsException(this, balance, amount);
		EconomyResponse resp = economy.withdrawPlayer(player, amount);
		if(!resp.transactionSuccess())
		{
			if("Insufficient funds".equals(resp.errorMessage))
				throw new InsufficientFundsException(this, balance, amount);
			throw new EconomyException(resp.errorMessage);
		}
		return resp.balance;
	}

	@Override
	public double computeBalance(DoubleUnaryOperator operation, @Nullable String comment)
	{
		ensureAccountExists();
		double newBalance = operation.applyAsDouble(getBalance());
		setBalance(newBalance);
		return newBalance;
	}

	protected static void checkAmount(double amount)
	{
		if(amount < 0.0d)
			throw new NegativeAmountException(amount);
	}

	protected void ensureAccountExists()
	{
		if(!economy.hasAccount(player))
			economy.createPlayerAccount(player);
	}
}
