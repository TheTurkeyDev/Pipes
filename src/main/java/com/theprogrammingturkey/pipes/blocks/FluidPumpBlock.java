package com.theprogrammingturkey.pipes.blocks;

import com.theprogrammingturkey.pipes.PipesCore;
import com.theprogrammingturkey.pipes.network.PipeNetwork;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.network.interfacing.InterfaceFilter;

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
	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);

	private String blockName;

	public FluidPumpBlock()
	{
		super(Material.GROUND);
		this.blockName = "fluid_pump";
		this.setHardness(0.5f);
		this.setTranslationKey(blockName);
		this.setCreativeTab(PipesCore.modTab);
		this.setRegistryName(PipesCore.MODID, this.blockName);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN));
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!world.isRemote)
		{
			PipeNetwork network = PipeNetworkManager.FLUID_NETWORK.getNetwork(pos);
			if(network != null)
				for(EnumFacing side : EnumFacing.VALUES)
					network.getNetworkInterface().removeInterfacedBlock(world, pos.offset(side), side.getOpposite());

			PipeNetworkManager.FLUID_NETWORK.removePipeFromNetwork(world, pos);
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if(!world.isRemote)
		{
			PipeNetwork network = PipeNetworkManager.FLUID_NETWORK.addPipeToNetwork(world, pos);
			EnumFacing side = state.getValue(FACING);
			network.getNetworkInterface().addInterfacedBlock(world, pos.offset(side), side.getOpposite(), getDefaultFilter(side));
		}
	}

	public void onNeighborChange(IBlockAccess access, BlockPos pos, BlockPos neighbor)
	{
		if(!(access instanceof World))
			return;
		World world = (World) access;
		if(world.isRemote)
			return;

		IBlockState state = world.getBlockState(pos);
		EnumFacing side = state.getValue(FACING);
		if(pos.offset(side).equals(neighbor))
		{
			PipeNetwork network = PipeNetworkManager.FLUID_NETWORK.getNetwork(pos);
			if(network != null)
				network.getNetworkInterface().updateInterfacedBlock(world, neighbor, side, getDefaultFilter(side));
		}
	}

	public String getBlockName()
	{
		return this.blockName;
	}

	public InterfaceFilter getDefaultFilter(EnumFacing side)
	{
		InterfaceFilter filter = new InterfaceFilter();
		filter.outputFilter.enabled = false;
		filter.facing = side;
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
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}
}
