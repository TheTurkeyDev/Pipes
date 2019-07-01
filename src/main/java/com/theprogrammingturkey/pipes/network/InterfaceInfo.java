package com.theprogrammingturkey.pipes.network;

import com.theprogrammingturkey.pipes.network.filtering.InterfaceFilter;

import net.minecraft.util.EnumFacing;

public class InterfaceInfo<T>
{
	public T inv;
	public InterfaceFilter filter;
	public EnumFacing facing;
	public int teHash;

	public InterfaceInfo(T inv, InterfaceFilter filter, EnumFacing facing, int teHash)
	{
		this.inv = inv;
		this.filter = filter;
		this.facing = facing;
		this.teHash = teHash;
	}
}
