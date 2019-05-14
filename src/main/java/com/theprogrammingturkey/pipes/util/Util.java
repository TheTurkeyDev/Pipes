package com.theprogrammingturkey.pipes.util;

import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraft.block.Block;

public class Util
{
	public static boolean areBlockAndTypeEqual(NetworkType type, Block block)
	{
		if(type == NetworkType.ITEM)
			return block.equals(RegistryHelper.ITEM_PIPE);
		else if(type == NetworkType.FLUID)
			return block.equals(RegistryHelper.FLUID_PIPE) || block.equals(RegistryHelper.FLUID_PUMP);
		return false;
	}
}
