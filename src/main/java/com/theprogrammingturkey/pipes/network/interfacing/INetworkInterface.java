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

	public void tick();

	public void addInterfacedBlock(World world, BlockPos block, EnumFacing facing, InterfaceFilter filter);

	public void updateInterfacedBlock(World world, BlockPos block, EnumFacing facing, InterfaceFilter filter);

	public void removeInterfacedBlock(World world, BlockPos block, EnumFacing facing);

	public void updateFilter(BlockPos pos, InterfaceFilter filter);

	public InterfaceFilter getFilterFromPipe(BlockPos pos, EnumFacing facing);

	public void processTransfers();

	public void merge(INetworkInterface netInterface);

	default Long getKeyHash(BlockPos pos, EnumFacing facing)
	{
		/*
		 * Essentially I'm using the upper 3 bits of the Y coordinate value. Based on my maths and
		 * info found in BlockPos, the Y_SHIFT should be 12 allowing for values of 0-4096, but since
		 * the y coord should never go that high, I'm using the upper 3 bits to store the facing
		 * value (0-5) leaving 9 bits left for the y before it overflows (0-512), it's close, but I
		 * think it'll work. Maybe there's a better way, but idk.
		 */
		return pos.toLong() | ((long) facing.getIndex() & FACING_MASK) << FACING_BIT_SHIFT;
	}
}
