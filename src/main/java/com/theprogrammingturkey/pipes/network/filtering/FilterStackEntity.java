package com.theprogrammingturkey.pipes.network.filtering;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public class FilterStackEntity implements IFilterStack
{
	private Entity ent;

	public FilterStackEntity(Entity ent)
	{
		this.ent = ent;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		return null;
	}

	@Override
	public boolean isEqual(IFilterStack ifs)
	{
		if(!(ifs instanceof FilterStackEntity))
			return false;

		FilterStackEntity fse = (FilterStackEntity) ifs;

		return ent.getClass().equals(fse.ent.getClass());
	}

	public Entity getAsEntity()
	{
		return ent;
	}

}
