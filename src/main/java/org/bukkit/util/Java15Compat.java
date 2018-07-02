package org.bukkit.util;

import java.util.Arrays;

public class Java15Compat
{
	public static <T> T[] Arrays_copyOfRange(T[] original, int start, int end)
	{
		return Arrays.copyOfRange(original, start, end);
	}
}