package com.theprogrammingturkey.pipes.blocks.properties;

import net.minecraft.util.IStringSerializable;

public enum EnumAttachType implements IStringSerializable
{
	NONE, PIPE, INVENTORY, DISABLED;

	@Override
	public String getName()
	{
		return this.name().toLowerCase();
	}

	public boolean isSegment()
	{
		return this == PIPE || this == INVENTORY;
	}
}
