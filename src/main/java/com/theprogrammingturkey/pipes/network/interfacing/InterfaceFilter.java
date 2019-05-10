package com.theprogrammingturkey.pipes.network.interfacing;

import java.util.ArrayList;
import java.util.List;

import com.theprogrammingturkey.pipes.util.FilterStack;
import com.theprogrammingturkey.pipes.util.ItemStackHelper;

import net.minecraft.util.EnumFacing;

public class InterfaceFilter
{
	public TransferType transferType = TransferType.BOTH;
	public boolean isWhiteList = false;
	public int inputPriority = 0;
	public int outputPriority = 0;
	private List<FilterStack> filterStacks = new ArrayList<>();
	public EnumFacing facing = EnumFacing.NORTH;

	public enum TransferType
	{
		INPUT, OUTPUT, BOTH;
	}

	public boolean hasStackInFilter(FilterStack stack)
	{
		for(FilterStack s : filterStacks)
			if(ItemStackHelper.areFilterStacksEqual(stack, s))
				return true;
		return false;
	}

	public void addStackToFilter(FilterStack stack)
	{
		if(!hasStackInFilter(stack))
			filterStacks.add(stack);
	}

	public List<FilterStack> getStacks()
	{
		return this.filterStacks;
	}
}
