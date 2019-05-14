package com.theprogrammingturkey.pipes.network.interfacing;

import java.util.ArrayList;
import java.util.Arrays;
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
				/*
				 * TE hash is just here because this method will often get triggered multiple times
				 * without the te actually changing. This may help keep thing clean in the future
				 * instead of making a new InterfaceInfo every time and only do it when the te
				 * actually changes.
				 */
				//TODO: Find a fix for Furnaces as it changes TE, but then resets to its old te when switching block state
				InterfaceInfo info = interfaces.get(hash);
				if(teHash != info.teHash && holder.te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, holder.facing))
					interfaces.put(hash, new InterfaceInfo(holder.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, holder.facing), holder.filter, holder.facing, teHash));
			}
			else
			{
				interfaces.put(hash, new InterfaceInfo(holder.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, holder.facing), holder.filter, holder.facing, teHash));
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
			for(int i = 0; i < info.inv.getSlots(); i++)
			{
				ItemStack stack = info.inv.extractItem(i, info.inv.getSlotLimit(i), true);
				if(stack == null || stack.isEmpty())
					continue;
				FilterStack fs = new FilterStack(stack);
				if((info.filter.isWhiteList(false) && info.filter.hasStackInFilter(false, fs)) || (!info.filter.isWhiteList(false) && !info.filter.hasStackInFilter(false, fs)))
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

		Map<IItemHandler, List<Integer>> ignoredInvSlots = new HashMap<>();
		Collections.sort(sortedInterfaces, outputPrioritySort);
		for(InterfaceInfo info : sortedInterfaces)
		{
			for(FilterStack stack : avilable.keySet())
			{
				boolean hasStack = info.filter.hasStackInFilter(true, stack);
				if((hasStack && info.filter.isWhiteList(true)) || (!hasStack && !info.filter.isWhiteList(true)))
				{
					//Inserting into a specific inventory
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
								if(stackInfo.amountLeft != 0 && !info.inv.equals(stackInfo.inv) && wontSendBack(info.filter, stackInfo.filter))
								{
									toInsert = stack.getAsItemStack();
									toInsert.setCount(stackInfo.amountLeft);

									toInsert = info.inv.insertItem(i, toInsert, false);

									if(stackInfo.amountLeft != toInsert.getCount() || toInsert.isEmpty())
									{
										stackInfo.amountLeft = toInsert.getCount();
										List<Integer> ignoredSlots = ignoredInvSlots.get(info.inv);
										if(ignoredSlots == null)
										{
											ignoredSlots = new ArrayList<Integer>();
											ignoredInvSlots.put(info.inv, ignoredSlots);
										}
										ignoredSlots.add(i);

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
		return interfaces.get(this.getKeyHash(pos.offset(facing.getOpposite()), facing)).filter;
	}

	@Override
	public void merge(INetworkInterface netInterface)
	{
		this.interfaces.putAll(((ItemInterface) netInterface).interfaces);
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
