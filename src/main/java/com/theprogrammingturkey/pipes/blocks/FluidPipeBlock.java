package com.theprogrammingturkey.pipes.blocks;

import com.theprogrammingturkey.pipes.network.PipeNetwork;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.util.RegistryHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class FluidPipeBlock extends BasePipeBlock
{
	public FluidPipeBlock()
	{
		super("item_pipe");
		this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, EnumAttachType.NONE).withProperty(EAST, EnumAttachType.NONE).withProperty(SOUTH, EnumAttachType.NONE).withProperty(WEST, EnumAttachType.NONE).withProperty(UP, EnumAttachType.NONE).withProperty(DOWN, EnumAttachType.NONE));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos origin)
	{
		for(EnumFacing side : EnumFacing.VALUES)
		{
			BlockPos offset = origin.offset(side);
			IBlockState neighbor = world.getBlockState(offset);
			if(neighbor.getBlock().equals(RegistryHelper.FLUID_PIPE))
				state = state.withProperty(FACING_MAPPING.get(side).direction, EnumAttachType.PIPE);
			else if(neighbor.getBlock().hasTileEntity(state) && world.getTileEntity(offset) instanceof IInventory)
				state = state.withProperty(FACING_MAPPING.get(side).direction, EnumAttachType.INVENTORY);
		}
		return state;
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
			for(EnumFacing side : EnumFacing.VALUES)
				network.getNetworkInterface().addInterfacedBlock(world, pos.offset(side), side.getOpposite());
		}
	}

	public void onNeighborChange(IBlockAccess access, BlockPos pos, BlockPos neighbor)
	{
		if(!(access instanceof World))
			return;
		World world = (World) access;
		if(world.isRemote)
			return;

		EnumFacing side = EnumFacing.getFacingFromVector(pos.getX() - neighbor.getX(), pos.getY() - neighbor.getY(), pos.getZ() - neighbor.getZ());
		PipeNetwork network = PipeNetworkManager.FLUID_NETWORK.getNetwork(pos);
		if(network != null)
			network.getNetworkInterface().updateInterfacedBlock(world, neighbor, side);
	}
}
