package com.theprogrammingturkey.pipes.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class Util
{
	private static final long MASK = (1L << 32) - 1L;

	public static long chunkToLong(BlockPos pos)
	{
		return chunkToLong(pos.getX() >> 4, pos.getZ() >> 4);
	}

	public static long chunkToLong(Chunk c)
	{
		return chunkToLong(c.x, c.z);
	}

	public static long chunkToLong(int x, int z)
	{
		return ((long) x & MASK) << 32 | ((long) z & MASK) << 0;
	}
}
