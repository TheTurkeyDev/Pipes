package com.theprogrammingturkey.pipes.blocks;

import javax.annotation.Nullable;

import com.theprogrammingturkey.pipes.network.IPipeNetwork;
import com.theprogrammingturkey.pipes.network.InterfaceFilter;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.util.RegistryHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public class ItemPipeBlock extends BasePipeBlock
{
	public ItemPipeBlock()
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
			if(neighbor.getBlock().equals(RegistryHelper.ITEM_PIPE))
				state = state.withProperty(FACING_MAPPING.get(side).direction, EnumAttachType.PIPE);
			else if(neighbor.getBlock().hasTileEntity(state) && world.getTileEntity(offset).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()))
				state = state.withProperty(FACING_MAPPING.get(side).direction, EnumAttachType.INVENTORY);
		}
		return state;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!world.isRemote)
		{
			IPipeNetwork network = PipeNetworkManager.ITEM_NETWORK.getNetwork(pos);
			if(network != null)
				for(EnumFacing side : EnumFacing.VALUES)
					network.removeInterfacedBlock(world, pos, side.getOpposite());

			PipeNetworkManager.ITEM_NETWORK.removePipeFromNetwork(world, pos);
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if(!world.isRemote)
		{
			IPipeNetwork network = PipeNetworkManager.ITEM_NETWORK.addPipeToNetwork(world, pos);
			for(EnumFacing side : EnumFacing.VALUES)
				network.addInterfacedBlock(world, pos, side.getOpposite(), new InterfaceFilter(side.getOpposite()));
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
		IPipeNetwork network = PipeNetworkManager.ITEM_NETWORK.getNetwork(pos);
		if(network != null)
			network.updateInterfacedBlock(world, pos, side, new InterfaceFilter(side));
	}

	@Deprecated
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
	{
		//TODO: change this
		return this.rayTrace(pos, start, end, blockState.getBoundingBox(worldIn, pos));
	}
}
