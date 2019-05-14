package com.theprogrammingturkey.pipes.blocks;

import com.theprogrammingturkey.pipes.network.PipeNetwork;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.network.interfacing.InterfaceFilter;
import com.theprogrammingturkey.pipes.packets.GetFilterPacket;
import com.theprogrammingturkey.pipes.packets.PipesPacketHandler;
import com.theprogrammingturkey.pipes.ui.FilterUI;
import com.theprogrammingturkey.pipes.util.RegistryHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
			else if(neighbor.getBlock().hasTileEntity(state) && world.getTileEntity(offset).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
				state = state.withProperty(FACING_MAPPING.get(side).direction, EnumAttachType.INVENTORY);
		}
		return state;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!world.isRemote)
		{
			PipeNetwork network = PipeNetworkManager.ITEM_NETWORK.getNetwork(pos);
			if(network != null)
				for(EnumFacing side : EnumFacing.VALUES)
					network.getNetworkInterface().removeInterfacedBlock(world, pos.offset(side), side.getOpposite());

			PipeNetworkManager.ITEM_NETWORK.removePipeFromNetwork(world, pos);
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if(!world.isRemote)
		{
			PipeNetwork network = PipeNetworkManager.ITEM_NETWORK.addPipeToNetwork(world, pos);
			for(EnumFacing side : EnumFacing.VALUES)
				network.getNetworkInterface().addInterfacedBlock(world, pos.offset(side), side.getOpposite(), new InterfaceFilter());
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
		PipeNetwork network = PipeNetworkManager.ITEM_NETWORK.getNetwork(pos);
		if(network != null)
			network.getNetworkInterface().updateInterfacedBlock(world, neighbor, side, new InterfaceFilter());
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote)
		{
			if(hitY < 0.4375)
				PipesPacketHandler.INSTANCE.sendToServer(new GetFilterPacket(pos, EnumFacing.UP));
			else if(hitY > 0.5625)
				PipesPacketHandler.INSTANCE.sendToServer(new GetFilterPacket(pos, EnumFacing.DOWN));
			else if(hitX < 0.4375)
				PipesPacketHandler.INSTANCE.sendToServer(new GetFilterPacket(pos, EnumFacing.EAST));
			else if(hitX > 0.5625)
				PipesPacketHandler.INSTANCE.sendToServer(new GetFilterPacket(pos, EnumFacing.WEST));
			else if(hitZ < 0.4375)
				PipesPacketHandler.INSTANCE.sendToServer(new GetFilterPacket(pos, EnumFacing.SOUTH));
			else if(hitZ > 0.5625)
				PipesPacketHandler.INSTANCE.sendToServer(new GetFilterPacket(pos, EnumFacing.NORTH));
			return true;
		}
		return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
	}
}
