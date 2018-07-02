package org.bukkit.craftbukkit.util;

import java.util.Arrays;

public class Java15Compat
{
	public static <T> T[] Arrays_copyOf(T[] original, int newLength)
	{
		return Arrays.copyOf(original, newLength);
	}

	public static long[] Arrays_copyOf(long[] original, int newLength)
	{
		return Arrays.copyOf(original, newLength);
	}

	private static long[] Arrays_copyOfRange(long[] original, int start, int end)
	{
		return Arrays.copyOfRange(original, start, end);
	}
}
