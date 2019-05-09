package com.theprogrammingturkey.pipes.network.interfacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theprogrammingturkey.pipes.util.FilterStack;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemInterface implements INetworkInterface
{
	private HashMap<Long, InterfaceInfo> interfaces = new HashMap<>();

	private Comparator<InterfaceInfo> inputPrioritySort = new Comparator<InterfaceInfo>()
	{
		@Override
		public int compare(InterfaceInfo ii1, InterfaceInfo ii2)
		{
			return ii1.filter.inputPriority - ii2.filter.inputPriority;
		}
	};

	private Comparator<InterfaceInfo> outputPrioritySort = new Comparator<InterfaceInfo>()
	{
		@Override
		public int compare(InterfaceInfo ii1, InterfaceInfo ii2)
		{
			return ii1.filter.outputPriority - ii2.filter.outputPriority;
		}
	};

	@Override
	public void processTransfers()
	{
		System.out.println("# of Interfaces: " + interfaces.size());
		Map<FilterStack, List<StackInfo>> avilable = new HashMap<>();

		//TODO: Should we sort all interfaces? Even ones not configured as both inputs and outputs

		List<InterfaceInfo> sortedInterfaces = new ArrayList<>(interfaces.values());

		Collections.sort(sortedInterfaces, inputPrioritySort);
		for(InterfaceInfo info : sortedInterfaces)
		{
			for(int i = 0; i < info.inv.getSlots(); i++)
			{
				//TODO: Not use 64
				ItemStack stack = info.inv.extractItem(i, 64, true);
				if(info.filter.isWhiteList)
				{
					FilterStack fs = new FilterStack(stack);
					if(info.filter.hasStackInFilter(fs))
						avilable.get(fs).add(new StackInfo(info.inv, i, stack.getCount()));
				}
				else
				{
					FilterStack fs = new FilterStack(stack);
					if(!info.filter.hasStackInFilter(fs))
						avilable.get(fs).add(new StackInfo(info.inv, i, stack.getCount()));
				}
			}
		}

		Collections.sort(sortedInterfaces, outputPrioritySort);
		for(InterfaceInfo info : sortedInterfaces)
		{
			for(FilterStack stack : info.filter.getStacks())
			{
				boolean hasStack = avilable.containsKey(stack);
				if((hasStack && info.filter.isWhiteList) || (!hasStack && !info.filter.isWhiteList))
				{
					int stackInfoIndex = 0;
					List<StackInfo> fromStacks = avilable.get(stack);
					ItemStack toInsert = null;
					for(int i = 0; i < info.inv.getSlots(); i++)
					{
						for(int j = stackInfoIndex; j < fromStacks.size(); j++)
						{
							StackInfo stackInfo = fromStacks.get(j);
							toInsert = stack.getAsItemStack();
							toInsert.setCount(stackInfo.amount);

							toInsert = info.inv.insertItem(i, toInsert, false);

							if(toInsert.getCount() != 0)
								break;
							else
								stackInfo.inv.extractItem(stackInfo.slot, stackInfo.amount, false);

							stackInfoIndex = j;
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

	@Override
	public void addInterfacedBlock(World world, BlockPos pos, EnumFacing facing)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing))
		{
			long hash = getKeyHash(pos, facing);
			interfaces.put(hash, new InterfaceInfo(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing), new InterfaceFilter(), facing));
		}
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

	//TODO: Make a better hash
	public Long getKeyHash(BlockPos pos, EnumFacing facing)
	{
		return pos.toLong() + facing.getIndex();
	}

	private static class StackInfo
	{
		public IItemHandler inv;
		public int slot;
		public int amount;

		public StackInfo(IItemHandler inv, int slot, int amount)
		{
			this.inv = inv;
			this.slot = slot;
			this.amount = amount;
		}
	}

	private static class InterfaceInfo
	{
		public IItemHandler inv;
		public InterfaceFilter filter;
		public EnumFacing facing;

		public InterfaceInfo(IItemHandler inv, InterfaceFilter filter, EnumFacing facing)
		{
			this.inv = inv;
			this.filter = filter;
			this.facing = facing;
		}
	}
}
