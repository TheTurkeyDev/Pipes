package com.theprogrammingturkey.pipes.blocks;

import com.theprogrammingturkey.pipes.PipesCore;
import com.theprogrammingturkey.pipes.RegistryHelper;
import com.theprogrammingturkey.pipes.blocks.BasePipeBlock.EnumAttachType;
import com.theprogrammingturkey.pipes.network.IPipeNetwork;
import com.theprogrammingturkey.pipes.network.NetworkType;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.network.filtering.InterfaceFilter;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class FluidPumpBlock extends Block
{
	public static final PropertyEnum<EnumFacing> PUMP_FACING = PropertyEnum.create("pump_facing", EnumFacing.class);
	public static final PropertyEnum<EnumAttachType> NORTH = PropertyEnum.create("north", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> EAST = PropertyEnum.create("east", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> SOUTH = PropertyEnum.create("south", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> WEST = PropertyEnum.create("west", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> UP = PropertyEnum.create("up", EnumAttachType.class);
	public static final PropertyEnum<EnumAttachType> DOWN = PropertyEnum.create("down", EnumAttachType.class);

	private String blockName;

	public FluidPumpBlock()
	{
		super(Material.GROUND);
		this.blockName = "fluid_pump";
		this.setHardness(0.5f);
		this.setTranslationKey(blockName);
		this.setCreativeTab(PipesCore.modTab);
		this.setRegistryName(PipesCore.MODID, this.blockName);
		this.setDefaultState(this.blockState.getBaseState().withProperty(PUMP_FACING, EnumFacing.DOWN));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos origin)
	{
		for(EnumFacing side : EnumFacing.VALUES)
		{
			BlockPos offset = origin.offset(side);
			IBlockState neighbor = world.getBlockState(offset);
			if(neighbor.getBlock().equals(RegistryHelper.FLUID_PIPE))
				state = state.withProperty(BasePipeBlock.FACING_MAPPING.get(side).direction, EnumAttachType.PIPE);
		}
		return state;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!world.isRemote)
		{
			IPipeNetwork network = PipeNetworkManager.getNetworkManagerForType(NetworkType.FLUID).getNetwork(pos, world.provider.getDimension());
			if(network != null)
				for(EnumFacing side : EnumFacing.VALUES)
					network.removeInterfacedBlock(world, pos, side.getOpposite());

			PipeNetworkManager.getNetworkManagerForType(NetworkType.FLUID).removePipeFromNetwork(world, pos);
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if(!world.isRemote)
		{
			IPipeNetwork network = PipeNetworkManager.getNetworkManagerForType(NetworkType.FLUID).addPipeToNetwork(world, pos);
			EnumFacing side = state.getValue(PUMP_FACING).getOpposite();
			network.addInterfacedBlock(world, pos, side, getDefaultFilter(side));
		}
	}

	public void observedNeighborChange(IBlockState observerState, World world, BlockPos pos, Block changedBlock, BlockPos neighbor)
	{
		if(world.isRemote)
			return;

		EnumFacing side = EnumFacing.getFacingFromVector(pos.getX() - neighbor.getX(), pos.getY() - neighbor.getY(), pos.getZ() - neighbor.getZ());
		IPipeNetwork network = PipeNetworkManager.getNetworkManagerForType(NetworkType.FLUID).getNetwork(pos, world.provider.getDimension());
		if(network != null)
			network.updateInterfacedBlock(world, pos, side, getDefaultFilter(side));
	}

	public String getBlockName()
	{
		return this.blockName;
	}

	public InterfaceFilter getDefaultFilter(EnumFacing side)
	{
		InterfaceFilter filter = new InterfaceFilter(side.getOpposite(), NetworkType.FLUID);
		filter.insertFilter.enabled = false;
		filter.extractFilter.enabled = true;
		return filter;
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
		return new BlockStateContainer(this, new IProperty[] { PUMP_FACING, NORTH, EAST, SOUTH, WEST, UP, DOWN });
	}
}
