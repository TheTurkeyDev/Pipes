package com.theprogrammingturkey.pipes.network.interfacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theprogrammingturkey.pipes.util.FilterStack;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class FluidInterface implements INetworkInterface
{
	private HashMap<Long, InterfaceInfo> interfaces = new HashMap<>();
	private List<TEHolder> toUpdate = new ArrayList<>();

	private Comparator<InterfaceInfo> inputPrioritySort = new Comparator<InterfaceInfo>()
	{
		@Override
		public int compare(InterfaceInfo ii1, InterfaceInfo ii2)
		{
			return ii1.filter.inputFilter.priority - ii2.filter.inputFilter.priority;
		}
	};

	private Comparator<InterfaceInfo> outputPrioritySort = new Comparator<InterfaceInfo>()
	{
		@Override
		public int compare(InterfaceInfo ii1, InterfaceInfo ii2)
		{
			return ii1.filter.outputFilter.priority - ii2.filter.outputFilter.priority;
		}
	};

	public void tick()
	{
		for(TEHolder holder : this.toUpdate)
		{
			int teHash = holder.te.hashCode();
			long hash = getKeyHash(holder.pos, holder.facing);
			if(interfaces.containsKey(hash))
			{
				InterfaceInfo info = interfaces.get(hash);
				if(teHash != info.teHash && holder.te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, holder.facing))
					interfaces.put(hash, new InterfaceInfo(holder.te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, holder.facing), new InterfaceFilter(), holder.facing, teHash));
			}
			else
			{
				interfaces.put(hash, new InterfaceInfo(holder.te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, holder.facing), new InterfaceFilter(), holder.facing, teHash));
			}
		}
		this.toUpdate.clear();
	}

	@Override
	public void processTransfers()
	{
		Map<FilterStack, List<StackInfo>> avilable = new HashMap<>();

		//TODO: Should we sort all interfaces? Even ones not configured as both inputs and outputs

		List<InterfaceInfo> sortedInterfaces = new ArrayList<>(interfaces.values());

		Collections.sort(sortedInterfaces, inputPrioritySort);
		for(InterfaceInfo info : sortedInterfaces)
		{
			
		}

		Map<IItemHandler, List<Integer>> ignoredInvSlots = new HashMap<>();
		Collections.sort(sortedInterfaces, outputPrioritySort);
		for(InterfaceInfo info : sortedInterfaces)
		{
			for(FilterStack stack : avilable.keySet())
			{

			}
		}
	}

	private boolean wontSendBack(InterfaceFilter toFilter, InterfaceFilter fromFilter)
	{
		if(toFilter.inputFilter.priority < fromFilter.inputFilter.priority)
			return true;
		return false;
	}

	@Override
	public void addInterfacedBlock(World world, BlockPos pos, EnumFacing facing)
	{
		TileEntity te = world.getTileEntity(pos);
		//Because of Furnaces we need to cache this stuff and only add it all once per tick
		if(te != null)
		{
			for(int i = toUpdate.size() - 1; i >= 0; i--)
			{
				TEHolder holder = toUpdate.get(i);
				if(holder.pos.equals(pos))
					toUpdate.remove(i);
			}
			this.toUpdate.add(new TEHolder(world, pos, te, facing));
		}
	}

	@Override
	public void updateInterfacedBlock(World world, BlockPos pos, EnumFacing facing)
	{
		TileEntity te = world.getTileEntity(pos);

		//Because of Furnaces we need to cache this stuff and only add it all once per tick
		for(int i = toUpdate.size() - 1; i >= 0; i--)
		{
			TEHolder holder = toUpdate.get(i);
			if(holder.pos.equals(pos))
				toUpdate.remove(i);
		}

		if(te != null)
			this.toUpdate.add(new TEHolder(world, pos, te, facing));
		else
			removeInterfacedBlock(world, pos, facing);
	}

	@Override
	public void removeInterfacedBlock(World world, BlockPos pos, EnumFacing facing)
	{
		interfaces.remove(getKeyHash(pos, facing));
	}

	@Override
	public void updateFilter(BlockPos pos, InterfaceFilter filter)
	{
		InterfaceInfo posInterfaces = interfaces.get(getKeyHash(pos, filter.facing));
		posInterfaces.filter = filter;
	}

	@Override
	public void merge(INetworkInterface netInterface)
	{
		this.interfaces.putAll(((FluidInterface) netInterface).interfaces);
	}

	private BlockPos undoKeyHash(long serialized)
	{
		return BlockPos.fromLong(serialized);
	}

	public Long getKeyHash(BlockPos pos, EnumFacing facing)
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

	private static class StackInfo
	{
		public IFluidHandler inv;
		public int slot;
		public int amount;
		public int amountLeft;
		public InterfaceFilter filter;

		public StackInfo(IFluidHandler inv, InterfaceFilter filter, int slot, int amount)
		{
			this.inv = inv;
			this.filter = filter;
			this.slot = slot;
			this.amount = amount;
			this.amountLeft = amount;
		}
	}

	private static class InterfaceInfo
	{
		public IFluidHandler inv;
		public InterfaceFilter filter;
		public EnumFacing facing;
		public int teHash;

		public InterfaceInfo(IFluidHandler inv, InterfaceFilter filter, EnumFacing facing, int teHash)
		{
			this.inv = inv;
			this.filter = filter;
			this.facing = facing;
			this.teHash = teHash;
		}
	}

	private static class TEHolder
	{
		public World world;
		public BlockPos pos;
		public TileEntity te;
		public EnumFacing facing;

		public TEHolder(World world, BlockPos pos, TileEntity te, EnumFacing facing)
		{
			this.world = world;
			this.pos = pos;
			this.te = te;
			this.facing = facing;
		}
	}
}
