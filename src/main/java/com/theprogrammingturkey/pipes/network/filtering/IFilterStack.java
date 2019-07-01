package com.theprogrammingturkey.pipes.network.filtering;

import net.minecraft.nbt.NBTTagCompound;

public interface IFilterStack
{

	public NBTTagCompound serializeNBT();

	public boolean isEqual(IFilterStack ifs);
}
