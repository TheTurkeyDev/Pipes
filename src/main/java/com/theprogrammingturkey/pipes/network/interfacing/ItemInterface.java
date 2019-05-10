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
		Map<FilterStack, List<StackInfo>> avilable = new HashMap<>();

		//TODO: Should we sort all interfaces? Even ones not configured as both inputs and outputs

		System.out.println(interfaces.size());
		for(long info : interfaces.keySet())
		{
			System.out.println(undoKeyHash(info));
		}

		List<InterfaceInfo> sortedInterfaces = new ArrayList<>(interfaces.values());

		Collections.sort(sortedInterfaces, inputPrioritySort);
		for(InterfaceInfo info : sortedInterfaces)
		{
			for(int i = 0; i < info.inv.getSlots(); i++)
			{
				ItemStack stack = info.inv.extractItem(i, info.inv.getSlotLimit(i), true);
				if(stack == null || stack.isEmpty())
					continue;
				FilterStack fs = new FilterStack(stack);
				if((info.filter.isWhiteList && info.filter.hasStackInFilter(fs)) || (!info.filter.isWhiteList && !info.filter.hasStackInFilter(fs)))
				{
					List<StackInfo> fsInfo = avilable.get(fs);
					if(fsInfo == null)
					{
						fsInfo = new ArrayList<StackInfo>();
						avilable.put(fs, fsInfo);
					}
					fsInfo.add(new StackInfo(info.inv, i, stack.getCount()));
				}
			}
		}

		Map<IItemHandler, List<Integer>> ignoredInvSlots = new HashMap<>();
		Collections.sort(sortedInterfaces, outputPrioritySort);
		for(InterfaceInfo info : sortedInterfaces)
		{
			for(FilterStack stack : avilable.keySet())
			{
				boolean hasStack = info.filter.hasStackInFilter(stack);
				if((hasStack && info.filter.isWhiteList) || (!hasStack && !info.filter.isWhiteList))
				{
					int stackInfoIndex = 0;
					List<StackInfo> fromStacks = avilable.get(stack);
					ItemStack toInsert = null;
					for(int i = 0; i < info.inv.getSlots(); i++)
					{
						if(!ignoredInvSlots.containsKey(info.inv) || !ignoredInvSlots.get(info.inv).contains(i))
						{
							for(int j = stackInfoIndex; j < fromStacks.size(); j++)
							{
								StackInfo stackInfo = fromStacks.get(j);
								if(!stackInfo.inserted && !info.inv.equals(stackInfo.inv))
								{
									toInsert = stack.getAsItemStack();
									toInsert.setCount(stackInfo.amount);

									toInsert = info.inv.insertItem(i, toInsert, false);

									if(stackInfo.amount != toInsert.getCount())
									{
										stackInfo.amount = toInsert.getCount();
										List<Integer> ignoredSlots = ignoredInvSlots.get(info.inv);
										if(ignoredSlots == null)
										{
											ignoredSlots = new ArrayList<Integer>();
											ignoredInvSlots.put(info.inv, ignoredSlots);
										}
										ignoredSlots.add(i);

										if(toInsert.getCount() == 0 || toInsert.isEmpty())
										{
											stackInfo.inv.extractItem(stackInfo.slot, stackInfo.amount, false);
											stackInfo.inserted = true;
											stackInfoIndex = j;
										}
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

	@Override
	public void updateInterfacedBlock(World world, BlockPos pos, EnumFacing facing)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te != null)
		{
			int teHash = te.hashCode();
			long hash = getKeyHash(pos, facing);
			if(interfaces.containsKey(hash))
			{
				/*
				 * TE hash is just here because this method will often get triggered multiple times
				 * without the te actually changing. This may help keep thing clean in the future
				 * instead of making a new InterfaceInfo every time and only do it when the te
				 * actually changes.
				 */
				//TODO: Find a fix for Furnaces as it changes TE, but then resets to its old te when switching block state
				InterfaceInfo info = interfaces.get(hash);
				if(teHash != info.teHash && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing))
					interfaces.put(hash, new InterfaceInfo(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing), new InterfaceFilter(), facing, teHash));
			}
			else
			{
				interfaces.put(hash, new InterfaceInfo(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing), new InterfaceFilter(), facing, teHash));
			}
		}
		else
		{
			removeInterfacedBlock(world, pos, facing);
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

	@Override
	public void merge(INetworkInterface netInterface)
	{
		this.interfaces.putAll(((ItemInterface) netInterface).interfaces);
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
		public IItemHandler inv;
		public int slot;
		public int amount;
		public boolean inserted = false;

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
		public int teHash;

		public InterfaceInfo(IItemHandler inv, InterfaceFilter filter, EnumFacing facing, int teHash)
		{
			this.inv = inv;
			this.filter = filter;
			this.facing = facing;
			this.teHash = teHash;
		}
	}
}
