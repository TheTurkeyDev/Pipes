package com.theprogrammingturkey.pipes.util;

import com.theprogrammingturkey.pipes.RegistryHelper;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class Util
{
	private static final long MASK = (1L << 32) - 1L;

	public static boolean areBlockAndTypeEqual(NetworkType type, Block block)
	{
		if(type == NetworkType.ITEM)
			return block.equals(RegistryHelper.ITEM_PIPE);
		else if(type == NetworkType.FLUID)
			return block.equals(RegistryHelper.FLUID_PIPE) || block.equals(RegistryHelper.FLUID_PUMP);
		else if(type == NetworkType.ENERGY)
			return block.equals(RegistryHelper.ENERGY_PIPE);
		return false;
	}

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
