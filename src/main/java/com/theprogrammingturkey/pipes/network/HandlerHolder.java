package com.theprogrammingturkey.pipes.network;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HandlerHolder<T>
{
	public World world;
	public BlockPos pos;
	public T handler;
	public EnumFacing facing;
	public InterfaceFilter filter;
	public boolean isTE;
	public int teHash;
	public BlockPos handlerPos;

	public HandlerHolder(T handler, World world, BlockPos pos, EnumFacing facing, InterfaceFilter filter, boolean isTE, int teHash)
	{
		this.handler = handler;
		this.world = world;
		this.pos = pos;
		this.facing = facing;
		this.filter = filter;
		this.isTE = isTE;
		this.teHash = teHash;
		this.handlerPos = pos.offset(facing.getOpposite());
	}
}
