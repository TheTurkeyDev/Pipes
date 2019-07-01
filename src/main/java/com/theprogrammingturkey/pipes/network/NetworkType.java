package com.theprogrammingturkey.pipes.network;

import com.theprogrammingturkey.pipes.RegistryHelper;

import net.minecraft.block.state.IBlockState;

public enum NetworkType
{
	ITEM(0), FLUID(1), ENERGY(2), ENTITY(3);

	private int id;

	NetworkType(int id)
	{
		this.id = id;
	}

	public int getID()
	{
		return this.id;
	}

	public static NetworkType getFromID(int id)
	{
		for(NetworkType type : NetworkType.values())
			if(type.getID() == id)
				return type;
		return ITEM;
	}

	public boolean areBlockAndTypeEqual(IBlockState state)
	{
		if(this == NetworkType.ITEM)
			return state.getBlock().equals(RegistryHelper.ITEM_PIPE);
		else if(this == NetworkType.FLUID)
			return state.getBlock().equals(RegistryHelper.FLUID_PIPE) || state.getBlock().equals(RegistryHelper.FLUID_PUMP);
		else if(this == NetworkType.ENERGY)
			return state.getBlock().equals(RegistryHelper.ENERGY_PIPE);
		else if(this == NetworkType.ENTITY)
			return state.getBlock().equals(RegistryHelper.ENTITY_PIPE);
		return false;
	}
}