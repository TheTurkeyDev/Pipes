package com.theprogrammingturkey.pipes.network.interfacing;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public interface INetworkInterface
{
	public static final long FACING_NUM_BITS = 3;
	//I think this is right..... see BlockPos.NUM_Y_BITS
	public static final long FACING_BIT_SHIFT = 60 - MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
	public static final long FACING_MASK = (1L << FACING_NUM_BITS) - 1L;
	
	public void updateInterfacedBlock(World world, BlockPos block, EnumFacing facing);

	public void removeInterfacedBlock(World world, BlockPos block, EnumFacing facing);
	
	public void updateFilter(BlockPos pos, InterfaceFilter filter);

	public void processTransfers();

	public void merge(INetworkInterface netInterface);
}
