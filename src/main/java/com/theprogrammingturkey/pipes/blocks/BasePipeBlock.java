package com.theprogrammingturkey.pipes.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.theprogrammingturkey.pipes.PipesCore;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BasePipeBlock extends Block
{
	public static final PropertyEnum<EnumAttachType> NORTH = PropertyEnum.create("north", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> EAST = PropertyEnum.create("east", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> SOUTH = PropertyEnum.create("south", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> WEST = PropertyEnum.create("west", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> UP = PropertyEnum.create("up", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> DOWN = PropertyEnum.create("down", EnumAttachType.class);

	public static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.4375, 0.4375, 0.4375, 0.5625, 0.5625, 0.5625);

	@SuppressWarnings("serial")
	public static final Map<EnumFacing, CustomFacingHolder> FACING_MAPPING = new HashMap<EnumFacing, CustomFacingHolder>()
	{
		{
			put(EnumFacing.NORTH, new CustomFacingHolder(NORTH, new AxisAlignedBB(0.4375, 0.4375, 0, 0.5625, 0.5625, 0.5)));
			put(EnumFacing.EAST, new CustomFacingHolder(EAST, new AxisAlignedBB(0.5, 0.4375, 0.4375, 1, 0.5625, 0.5625)));
			put(EnumFacing.SOUTH, new CustomFacingHolder(SOUTH, new AxisAlignedBB(0.4375, 0.4375, 0.5, 0.5625, 0.5625, 1)));
			put(EnumFacing.WEST, new CustomFacingHolder(WEST, new AxisAlignedBB(0, 0.4375, 0.4375, 0.5, 0.5625, 0.5625)));
			put(EnumFacing.UP, new CustomFacingHolder(UP, new AxisAlignedBB(0.4375, 0.5, 0.4375, 0.5625, 1, 0.5625)));
			put(EnumFacing.DOWN, new CustomFacingHolder(DOWN, new AxisAlignedBB(0.4375, 0, 0.4375, 0.5625, 0.5, 0.5625)));
		}
	};

	private String blockName = "pipes_unnamed";

	public BasePipeBlock(String name)
	{
		super(Material.GROUND);
		this.blockName = name;
		this.setHardness(0.5f);
		this.setTranslationKey(blockName);
		this.setCreativeTab(PipesCore.modTab);
		this.setRegistryName(PipesCore.MODID, this.blockName);
		this.setDefaultState(getDefaultState());
	}

	public String getBlockName()
	{
		return this.blockName;
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

	public int getMetaFromState(IBlockState state)
	{
		return 0;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		return false;
	}

	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { NORTH, EAST, SOUTH, WEST, UP, DOWN });
	}

	public static enum EnumAttachType implements IStringSerializable
	{
		NONE, PIPE, INVENTORY, DISABLED;

		@Override
		public String getName()
		{
			return this.name().toLowerCase();
		}

		public boolean isSegment()
		{
			return this == PIPE || this == INVENTORY;
		}
	}

	public static class CustomFacingHolder
	{
		public PropertyEnum<EnumAttachType> direction;
		public AxisAlignedBB boundingBox;

		public CustomFacingHolder(PropertyEnum<EnumAttachType> direction, AxisAlignedBB boundingBox)
		{
			this.direction = direction;
			this.boundingBox = boundingBox;
		}

	}
}