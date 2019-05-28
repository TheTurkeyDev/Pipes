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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemPipeNetwork extends PipeNetwork<IItemHandler>
{
	public ItemPipeNetwork(int networkID, int dimId)
	{
		super(networkID, dimId, NetworkType.ITEM, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	}

	public void processTransfers()
	{
		Map<FilterStack, List<StackInfo>> avilable = new HashMap<>();

		//TODO: Should we sort all interfaces? Even ones not configured as both inputs and outputs

		List<InterfaceInfo<IItemHandler>> sortedInterfaces = new ArrayList<>(interfaces.values());

		Collections.sort(sortedInterfaces, extractPrioritySort);
		for(InterfaceInfo<IItemHandler> info : sortedInterfaces)
		{
			info.filter.setShowInsertFilter(false);
			if(!info.filter.isEnabled())
				continue;
			for(int i = 0; i < info.inv.getSlots(); i++)
			{
				ItemStack stack = info.inv.extractItem(i, info.inv.getSlotLimit(i), true);
				if(stack == null || stack.isEmpty())
					continue;
				FilterStack fs = new FilterStack(stack);
				if((info.filter.isWhiteList() && info.filter.hasStackInFilter(fs)) || (!info.filter.isWhiteList() && !info.filter.hasStackInFilter(fs)))
				{
					List<StackInfo> fsInfo = avilable.get(fs);
					if(fsInfo == null)
					{
						fsInfo = new ArrayList<StackInfo>();
						avilable.put(fs, fsInfo);
					}
					fsInfo.add(new StackInfo(info.inv, info.filter, i, stack.getCount()));
				}
			}
		}

		Collections.sort(sortedInterfaces, insertPrioritySort);
		for(InterfaceInfo<IItemHandler> info : sortedInterfaces)
		{
			info.filter.setShowInsertFilter(true);
			if(!info.filter.isEnabled())
				continue;
			for(FilterStack stack : avilable.keySet())
			{
				boolean hasStack = info.filter.hasStackInFilter(stack);
				if((hasStack && info.filter.isWhiteList()) || (!hasStack && !info.filter.isWhiteList()))
				{
					//Inserting into a specific inventory
					int stackInfoIndex = 0;
					List<StackInfo> fromStacks = avilable.get(stack);
					ItemStack toInsert = null;
					for(int i = 0; i < info.inv.getSlots(); i++)
					{
						for(int j = stackInfoIndex; j < fromStacks.size(); j++)
						{
							StackInfo stackInfo = fromStacks.get(j);
							if(stackInfo.amountLeft != 0 && !info.inv.equals(stackInfo.inv) && wontSendBack(info.filter, stackInfo.filter, stack))
							{
								toInsert = stack.getAsItemStack();
								toInsert.setCount(stackInfo.amountLeft);

								toInsert = info.inv.insertItem(i, toInsert, false);

								if(stackInfo.amountLeft != toInsert.getCount() || toInsert.isEmpty())
								{
									stackInfo.amountLeft = toInsert.getCount();

									if(toInsert.getCount() == 0 || toInsert.isEmpty())
									{
										stackInfo.amountLeft = 0;
										stackInfo.inv.extractItem(stackInfo.slot, stackInfo.amount, false);
										stackInfoIndex = j;
									}
								}
							}
						}

					}

					if(stackInfoIndex != fromStacks.size() && toInsert != null)
					{
						StackInfo stackInfo = fromStacks.get(stackInfoIndex);
						stackInfo.inv.extractItem(stackInfo.slot, stackInfo.amount - toInsert.getCount(), false);
					}
				}
			}
		}
	}

	private boolean wontSendBack(InterfaceFilter toFilter, InterfaceFilter fromFilter, FilterStack stack)
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

		if(toFilter.insertFilter.priority > fromOpposite.priority)
			return true;

		return false;
	}

	private static class StackInfo
	{
		public IItemHandler inv;
		public int slot;
		public int amount;
		public int amountLeft;
		public InterfaceFilter filter;

		public StackInfo(IItemHandler inv, InterfaceFilter filter, int slot, int amount)
		{
			this.inv = inv;
			this.filter = filter;
			this.slot = slot;
			this.amount = amount;
			this.amountLeft = amount;
		}
	}
}
