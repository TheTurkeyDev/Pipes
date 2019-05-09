package com.theprogrammingturkey.pipes.network;

import java.util.ArrayList;
import java.util.List;

import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;
import com.theprogrammingturkey.pipes.network.interfacing.INetworkInterface;
import com.theprogrammingturkey.pipes.network.interfacing.ItemInterface;

import net.minecraft.util.math.BlockPos;

public class PipeNetwork
{
	private boolean isActive = true;
	private int networkID;
	private List<Long> containedBlockPos = new ArrayList<>();

	private NetworkType type;

	private INetworkInterface netInterface;

	public PipeNetwork(int networkID, NetworkType type)
	{
		this.networkID = networkID;
		this.type = type;
		this.netInterface = PipeNetwork.getNewInterface(type);
	}

	public void tick()
	{
		netInterface.processTransfers();
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

	public INetworkInterface getNetworkInterface()
	{
		return this.netInterface;
	}

	private static INetworkInterface getNewInterface(NetworkType type)
	{
		switch(type)
		{
			case ITEM:
				return new ItemInterface();
			default:
				return new ItemInterface();
		}
	}
}