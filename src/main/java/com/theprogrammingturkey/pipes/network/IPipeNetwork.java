package com.theprogrammingturkey.pipes.network;

import java.util.List;

import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPipeNetwork
{

	public void tick();

	public boolean isActive();

	public int getNetworkID();

	public NetworkType getNetworkType();

	public void deleteNetwork();

	public void addBlockPosToNetwork(BlockPos pos);

	public void removeBlockPosFromNetwork(BlockPos pos);

	public List<Long> getcontainedBlockPos();

	public void mergeWithNetwork(IPipeNetwork toMerge);

	public void addInterfacedBlock(World world, BlockPos pos, EnumFacing facing, InterfaceFilter filter);

	public void updateInterfacedBlock(World world, BlockPos pos, EnumFacing facing, InterfaceFilter filter);

	public void removeInterfacedBlock(World world, BlockPos pos, EnumFacing facing);

	public InterfaceFilter getFilterFromPipe(BlockPos pos, EnumFacing facing);

	public void updateFilter(BlockPos pos, InterfaceFilter filter);
}
