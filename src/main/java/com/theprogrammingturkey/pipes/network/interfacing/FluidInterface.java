package com.theprogrammingturkey.pipes.network.interfacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
					interfaces.put(hash, new InterfaceInfo(holder.te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, holder.facing), holder.filter, holder.facing, teHash));
			}
			else
			{
				interfaces.put(hash, new InterfaceInfo(holder.te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, holder.facing), holder.filter, holder.facing, teHash));
			}
		}
		this.toUpdate.clear();
	}

	@Override
	public void processTransfers()
	{
		Map<Fluid, List<StackInfo>> avilable = new HashMap<>();

		//TODO: Should we sort all interfaces? Even ones not configured as both inputs and outputs

		List<InterfaceInfo> sortedInterfaces = new ArrayList<>(interfaces.values());

		Collections.sort(sortedInterfaces, inputPrioritySort);
		for(InterfaceInfo info : sortedInterfaces)
		{
			FluidStack fs;
			if(info.inv instanceof IFluidTank)
				fs = info.inv.drain(((IFluidTank) info.inv).getCapacity(), false);
			else
				fs = info.inv.drain(Fluid.BUCKET_VOLUME, false);
			List<StackInfo> fsInfo = avilable.get(fs.getFluid());
			if(fsInfo == null)
			{
				fsInfo = new ArrayList<StackInfo>();
				avilable.put(fs.getFluid(), fsInfo);
			}
			fsInfo.add(new StackInfo(info.inv, info.filter, fs.amount));
		}

		Collections.sort(sortedInterfaces, outputPrioritySort);
		for(InterfaceInfo info : sortedInterfaces)
		{
			for(Fluid fluid : avilable.keySet())
			{
				for(StackInfo stackInfo : avilable.get(fluid))
				{
					if(!stackInfo.inv.equals(info.inv) && wontSendBack(info.filter, stackInfo.filter))
					{
						int amonutUsed = info.inv.fill(stackInfo.getStack(fluid), true);
						if(amonutUsed != 0)
						{
							stackInfo.amountLeft -= amonutUsed;
							if(stackInfo.amountLeft == 0)
								stackInfo.inv.drain(stackInfo.amount, true);
						}
					}
				}
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
	public void addInterfacedBlock(World world, BlockPos pos, EnumFacing facing, InterfaceFilter filter)
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
			this.toUpdate.add(new TEHolder(world, pos, te, facing, filter));
		}
	}

	@Override
	public void updateInterfacedBlock(World world, BlockPos pos, EnumFacing facing, InterfaceFilter filter)
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
			this.toUpdate.add(new TEHolder(world, pos, te, facing, filter));
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
	public InterfaceFilter getFilterFromPipe(BlockPos pos, EnumFacing facing)
	{
		return interfaces.get(this.getKeyHash(pos, facing)).filter;
	}

	@Override
	public void merge(INetworkInterface netInterface)
	{
		this.interfaces.putAll(((FluidInterface) netInterface).interfaces);
	}

	private static class StackInfo
	{
		public IFluidHandler inv;
		public int amount;
		public int amountLeft;
		public InterfaceFilter filter;

		public StackInfo(IFluidHandler inv, InterfaceFilter filter, int amount)
		{
			this.inv = inv;
			this.filter = filter;
			this.amount = amount;
			this.amountLeft = amount;
		}

		public FluidStack getStack(Fluid fluid)
		{
			return new FluidStack(fluid, amountLeft);
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
		public InterfaceFilter filter;

		public TEHolder(World world, BlockPos pos, TileEntity te, EnumFacing facing, InterfaceFilter filter)
		{
			this.world = world;
			this.pos = pos;
			this.te = te;
			this.facing = facing;
			this.filter = filter;
		}
	}
}
