package com.theprogrammingturkey.pipes.network.interfacing;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface INetworkInterface
{
	public void addInterfacedBlock(World world, BlockPos block, EnumFacing facing);

	public void removeInterfacedBlock(World world, BlockPos block, EnumFacing facing);
	
	public void updateFilter(BlockPos pos, InterfaceFilter filter);

	public void processTransfers();
}
