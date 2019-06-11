package com.theprogrammingturkey.pipes.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theprogrammingturkey.pipes.RegistryHelper;
import com.theprogrammingturkey.pipes.blocks.properties.BundledPipeProperty;
import com.theprogrammingturkey.pipes.blocks.properties.EnumAttachType;
import com.theprogrammingturkey.pipes.blocks.properties.PipeBundle;
import com.theprogrammingturkey.pipes.network.IPipeNetwork;
import com.theprogrammingturkey.pipes.network.InterfaceFilter;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BundledPipeBlock extends BasePipeBlock
{
	public static PropertyBool ITEM_CONTAINED = PropertyBool.create(NetworkType.ITEM.name().toLowerCase());
	public static PropertyBool FLUID_CONTAINED = PropertyBool.create(NetworkType.FLUID.name().toLowerCase());
	public static PropertyBool ENERGY_CONTAINED = PropertyBool.create(NetworkType.ENERGY.name().toLowerCase());
	public static BundledPipeProperty NORTH = new BundledPipeProperty("north");
	public static BundledPipeProperty EAST = new BundledPipeProperty("east");
	public static BundledPipeProperty SOUTH = new BundledPipeProperty("south");
	public static BundledPipeProperty WEST = new BundledPipeProperty("west");
	public static BundledPipeProperty UP = new BundledPipeProperty("up");
	public static BundledPipeProperty DOWN = new BundledPipeProperty("down");

	@SuppressWarnings("serial")
	public static final Map<EnumFacing, CustomFacingHolder> MAPPING = new HashMap<EnumFacing, CustomFacingHolder>()
	{
		{
			put(EnumFacing.NORTH, new CustomFacingHolder(NORTH, BaseNetworkPipeBlock.NORTH, new AxisAlignedBB(0.4375, 0.4375, 0, 0.5625, 0.5625, 0.5)));
			put(EnumFacing.EAST, new CustomFacingHolder(EAST, BaseNetworkPipeBlock.EAST, new AxisAlignedBB(0.5, 0.4375, 0.4375, 1, 0.5625, 0.5625)));
			put(EnumFacing.SOUTH, new CustomFacingHolder(SOUTH, BaseNetworkPipeBlock.SOUTH, new AxisAlignedBB(0.4375, 0.4375, 0.5, 0.5625, 0.5625, 1)));
			put(EnumFacing.WEST, new CustomFacingHolder(WEST, BaseNetworkPipeBlock.WEST, new AxisAlignedBB(0, 0.4375, 0.4375, 0.5, 0.5625, 0.5625)));
			put(EnumFacing.UP, new CustomFacingHolder(UP, BaseNetworkPipeBlock.UP, new AxisAlignedBB(0.4375, 0.5, 0.4375, 0.5625, 1, 0.5625)));
			put(EnumFacing.DOWN, new CustomFacingHolder(DOWN, BaseNetworkPipeBlock.DOWN, new AxisAlignedBB(0.4375, 0, 0.4375, 0.5625, 0.5, 0.5625)));
		}
	};

	public BundledPipeBlock()
	{
		super("bundled_pipe");
		this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, new PipeBundle()));
		//@formatter:off
//		this.setDefaultState(this.blockState.getBaseState().withProperty(ITEM_CONTAINED, false).withProperty(FLUID_CONTAINED, false)
//				.withProperty(ENERGY_CONTAINED, false).withProperty(NORTH, new PipeBundle()).withProperty(EAST, new PipeBundle())
//				.withProperty(SOUTH, new PipeBundle()).withProperty(WEST, new PipeBundle()).withProperty(UP, new PipeBundle())
//				.withProperty(DOWN, new PipeBundle()));
		//@formatter:on
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		for(BaseNetworkPipeBlock pipe : this.getActiveBlocks(state))
		{
			IBlockState pipeState = pipe.getDefaultState();
			pipeState = pipeState.getActualState(world, pos);
			for(EnumFacing side : EnumFacing.VALUES)
			{
				CustomFacingHolder holder = MAPPING.get(side);
				PipeBundle bundle = state.getValue(holder.bundleDir);
				bundle.setAttachType(pipe.getNetworkType(), pipeState.getValue(holder.direction));
				state = state.withProperty(holder.bundleDir, bundle);
			}
		}
		return state;
	}

	//	@Override
	//	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	//	{
	//		if(!world.isRemote)
	//		{
	//			PipeNetworkManager networkManager = PipeNetworkManager.getNetworkManagerForType(type);
	//			IPipeNetwork network = networkManager.getNetwork(pos, world.provider.getDimension());
	//			if(network == null)
	//				return false;
	//
	//			int faceHit = -1;
	//			if(hitX > .999 || hitX < 0.001)
	//				faceHit = 0;
	//			else if(hitY > .999 || hitY < 0.001)
	//				faceHit = 1;
	//			else if(hitZ > .999 || hitZ < 0.001)
	//				faceHit = 2;
	//
	//			InterfaceFilter filter = null;
	//			if(hitX < 0.4375 && faceHit != 0)
	//				filter = network.getFilterFromPipe(pos, EnumFacing.EAST);
	//			else if(hitX > 0.5625 && faceHit != 0)
	//				filter = network.getFilterFromPipe(pos, EnumFacing.WEST);
	//			else if(hitY < 0.4375 && faceHit != 1)
	//				filter = network.getFilterFromPipe(pos, EnumFacing.UP);
	//			else if(hitY > 0.5625 && faceHit != 1)
	//				filter = network.getFilterFromPipe(pos, EnumFacing.DOWN);
	//			else if(hitZ < 0.4375 && faceHit != 2)
	//				filter = network.getFilterFromPipe(pos, EnumFacing.SOUTH);
	//			else if(hitZ > 0.5625 && faceHit != 2)
	//				filter = network.getFilterFromPipe(pos, EnumFacing.NORTH);
	//
	//			if(filter != null)
	//			{
	//				UIUtil.openFilterUI((EntityPlayerMP) player, pos, filter);
	//				return true;
	//			}
	//
	//			return false;
	//		}
	//		return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
	//	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!world.isRemote)
		{
			List<IPipeNetwork> networks = PipeNetworkManager.getNetworksAtPos(pos, world.provider.getDimension());
			for(IPipeNetwork network : networks)
			{
				for(EnumFacing side : EnumFacing.VALUES)
					network.removeInterfacedBlock(world, pos, side.getOpposite());

				PipeNetworkManager.getNetworkManagerForType(network.getNetworkType()).removePipeFromNetwork(world, pos);
			}
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	public void observedNeighborChange(IBlockState observerState, World world, BlockPos pos, Block changedBlock, BlockPos neighbor)
	{
		if(!world.isRemote)
		{
			List<IPipeNetwork> networks = PipeNetworkManager.getNetworksAtPos(pos, world.provider.getDimension());
			for(IPipeNetwork network : networks)
			{
				EnumFacing side = EnumFacing.getFacingFromVector(pos.getX() - neighbor.getX(), pos.getY() - neighbor.getY(), pos.getZ() - neighbor.getZ());
				network.updateInterfacedBlock(world, pos, side, new InterfaceFilter(side, network.getNetworkType()));
			}
		}
	}

	public List<NetworkType> getActiveTypes(IBlockState state)
	{
		List<NetworkType> networks = new ArrayList<>();
		if(state.getValue(ITEM_CONTAINED))
			networks.add(NetworkType.ITEM);
		if(state.getValue(FLUID_CONTAINED))
			networks.add(NetworkType.FLUID);
		if(state.getValue(ENERGY_CONTAINED))
			networks.add(NetworkType.ENERGY);
		return networks;
	}

	public List<BaseNetworkPipeBlock> getActiveBlocks(IBlockState state)
	{
		List<BaseNetworkPipeBlock> networks = new ArrayList<>();
		if(state.getValue(ITEM_CONTAINED))
			networks.add(RegistryHelper.ITEM_PIPE);
		if(state.getValue(FLUID_CONTAINED))
			networks.add(RegistryHelper.FLUID_PIPE);
		if(state.getValue(ENERGY_CONTAINED))
			networks.add(RegistryHelper.ENERGY_PIPE);
		return networks;
	}

	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { NORTH });
	}

	public static class CustomFacingHolder
	{
		public BundledPipeProperty bundleDir;
		public PropertyEnum<EnumAttachType> direction;
		public AxisAlignedBB boundingBox;

		public CustomFacingHolder(BundledPipeProperty bundleDir, PropertyEnum<EnumAttachType> direction, AxisAlignedBB boundingBox)
		{
			this.bundleDir = bundleDir;
			this.direction = direction;
			this.boundingBox = boundingBox;
		}
	}

}
