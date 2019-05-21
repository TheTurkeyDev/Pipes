package com.theprogrammingturkey.pipes.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theprogrammingturkey.pipes.network.InterfaceFilter.DirectionFilter;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;
import com.theprogrammingturkey.pipes.util.FilterStack;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidPipeNetwork extends PipeNetwork<IFluidHandler>
{
	public FluidPipeNetwork(int networkID)
	{
		super(networkID, NetworkType.FLUID, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
	}

	@Override
	public void processTransfers()
	{
		Map<Fluid, List<StackInfo>> avilable = new HashMap<>();

		//TODO: Should we sort all interfaces? Even ones not configured as both inputs and outputs

		List<InterfaceInfo<IFluidHandler>> sortedInterfaces = new ArrayList<>(interfaces.values());

		Collections.sort(sortedInterfaces, extractPrioritySort);
		for(InterfaceInfo<IFluidHandler> info : sortedInterfaces)
		{
			info.filter.setShowInsertFilter(false);
			if(!info.filter.isEnabled())
				continue;

			FluidStack fs;
			if(info.inv instanceof IFluidTank)
				fs = info.inv.drain(((IFluidTank) info.inv).getCapacity(), false);
			else
				fs = info.inv.drain(Fluid.BUCKET_VOLUME, false);

			if(fs == null)
				continue;
			List<StackInfo> fsInfo = avilable.get(fs.getFluid());
			if(fsInfo == null)
			{
				fsInfo = new ArrayList<StackInfo>();
				avilable.put(fs.getFluid(), fsInfo);
			}
			fsInfo.add(new StackInfo(info.inv, info.filter, fs.amount));
		}

		Collections.sort(sortedInterfaces, insertPrioritySort);
		for(InterfaceInfo<IFluidHandler> info : sortedInterfaces)
		{
			info.filter.setShowInsertFilter(true);
			if(!info.filter.isEnabled())
				continue;
			for(Fluid fluid : avilable.keySet())
			{
				FilterStack stack = new FilterStack(FluidUtil.getFilledBucket(new FluidStack(fluid, 1)));
				boolean hasStack = info.filter.hasStackInFilter(stack);
				if((hasStack && info.filter.isWhiteList()) || (!hasStack && !info.filter.isWhiteList()))
				{
					//Inserting into a specific inventory
					int stackInfoIndex = 0;
					List<StackInfo> fromStacks = avilable.get(fluid);
					ItemStack toInsert = null;
					for(int j = stackInfoIndex; j < fromStacks.size(); j++)
					{
						StackInfo stackInfo = fromStacks.get(j);
						if(stackInfo.amountLeft != 0 && !info.inv.equals(stackInfo.inv) && wontSendBack(info.filter, stackInfo.filter, fluid, stack))
						{
							toInsert = stack.getAsItemStack();
							toInsert.setCount(stackInfo.amountLeft);

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
	}

	private boolean wontSendBack(InterfaceFilter toFilter, InterfaceFilter fromFilter, Fluid fluid, FilterStack stack)
	{
		DirectionFilter fromOpposite = fromFilter.insertFilter;
		DirectionFilter toOpposite = toFilter.extractFilter;

		if(!fromOpposite.enabled || !toOpposite.enabled)
			return true;

		boolean hasStack = fromOpposite.hasStackInFilter(stack);
		if((hasStack && !fromOpposite.isWhiteList) || (!hasStack && fromOpposite.isWhiteList))
			return true;

		hasStack = toOpposite.hasStackInFilter(stack);
		if((hasStack && !toOpposite.isWhiteList) || (!hasStack && toOpposite.isWhiteList))
			return true;

		if(toOpposite.priority < fromOpposite.priority)
			return true;

		return false;
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
}
