package com.theprogrammingturkey.pipes.network;

import java.util.List;

import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPipeNetwork
{

	public void tick();

	public boolean isActive();

	public int getNetworkID();

	public int getDimID();

	public NetworkType getNetworkType();

	public void update(World world);

	public boolean requiresUpdate();

	public void passiveUpdate(World world);

	public boolean requiresPassiveUpdate();

	public void deleteNetwork();

	public void addBlockPosToNetwork(BlockPos pos);

	public void removeBlockPosFromNetwork(BlockPos pos);

	public List<Long> getcontainedBlockPos();
	
	public boolean isPosInNetwork(BlockPos pos);

	public void mergeWithNetwork(IPipeNetwork toMerge);

	/**
	 * 
	 * @param world
	 *            Location of the pipe in the world
	 * @param facing
	 *            interfacing side on the interfaced block
	 * @param filter
	 */
	public void addInterfacedBlock(World world, BlockPos pos, EnumFacing facing, InterfaceFilter filter);

	public void updateInterfacedBlock(World world, BlockPos pos, EnumFacing facing, InterfaceFilter filter);

	public void removeInterfacedBlock(World world, BlockPos pos, EnumFacing facing);

	public InterfaceFilter getFilterFromPipe(BlockPos pos, EnumFacing facing);

	public void updateFilter(BlockPos pos, InterfaceFilter filter);

	public boolean isInChunk(int x, int z);

	public boolean shouldSave();

	public NBTTagCompound saveNetworkInchunk(int x, int z);

	public void loadNetworkInChunk(World world, int x, int z, NBTTagCompound nbt);
}
