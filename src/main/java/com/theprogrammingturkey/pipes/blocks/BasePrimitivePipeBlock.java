package com.theprogrammingturkey.pipes.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BasePrimitivePipeBlock extends BaseNetworkPipeBlock
{
	public BasePrimitivePipeBlock(String name, NetworkType type)
	{
		super(name, type);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		AxisAlignedBB box = BASE_AABB;
		for(EnumFacing side : EnumFacing.VALUES)
			if(state.getActualState(world, pos).getValue(FACING_MAPPING.get(side).direction).isSegment())
				box = box.union(FACING_MAPPING.get(side).boundingBox);
		return box;
	}

	@SuppressWarnings("deprecation")
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState)
	{
		addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
		for(EnumFacing side : EnumFacing.VALUES)
			if(state.getActualState(world, pos).getValue(FACING_MAPPING.get(side).direction).isSegment())
				addCollisionBoxToList(pos, entityBox, collidingBoxes, FACING_MAPPING.get(side).boundingBox);
	}
}
