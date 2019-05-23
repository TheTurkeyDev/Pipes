package com.theprogrammingturkey.pipes.blocks;

import com.theprogrammingturkey.pipes.RegistryHelper;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.energy.CapabilityEnergy;

public class EnergyPipeBlock extends BasePipeBlock
{
	public EnergyPipeBlock()
	{
		super("energy_pipe", NetworkType.ENERGY);
		this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, EnumAttachType.NONE).withProperty(EAST, EnumAttachType.NONE).withProperty(SOUTH, EnumAttachType.NONE).withProperty(WEST, EnumAttachType.NONE).withProperty(UP, EnumAttachType.NONE).withProperty(DOWN, EnumAttachType.NONE));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos origin)
	{
		for(EnumFacing side : EnumFacing.VALUES)
		{
			BlockPos offset = origin.offset(side);
			IBlockState neighbor = world.getBlockState(offset);
			if(neighbor.getBlock().equals(RegistryHelper.ENERGY_PIPE))
				state = state.withProperty(FACING_MAPPING.get(side).direction, EnumAttachType.PIPE);
			else if(neighbor.getBlock().hasTileEntity(state) && world.getTileEntity(offset).hasCapability(CapabilityEnergy.ENERGY, side.getOpposite()))
				state = state.withProperty(FACING_MAPPING.get(side).direction, EnumAttachType.INVENTORY);
		}
		return state;
	}
}
