package com.theprogrammingturkey.pipes.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.BlockPos;

public class PipeNetwork
{
	private boolean isActive = true;
	private int networkID;
	private List<Long> containedBlockPos = new ArrayList<Long>();

	public PipeNetwork(int networkID)
	{
		this.networkID = networkID;
	}

	public void addBlockPosToNetwork(BlockPos pos)
	{
		containedBlockPos.add(pos.toLong());
	}

	public void removeBlockPosFromNetwork(BlockPos pos)
	{
		containedBlockPos.remove(pos.toLong());
	}

	public boolean isPosInNetwork(BlockPos pos)
	{
		return containedBlockPos.contains(pos.toLong());
	}

	public int getNetworkID()
	{
		return this.networkID;
	}

	public List<Long> getContainedBlockPos()
	{
		return containedBlockPos;
	}

	public void mergeWithNetwork(PipeNetwork toMerge)
	{
		this.containedBlockPos.addAll(containedBlockPos);
	}

	public void deleteNetwork()
	{
		isActive = false;
		containedBlockPos.clear();
	}

	public boolean isActive()
	{
		return isActive;
	}
}