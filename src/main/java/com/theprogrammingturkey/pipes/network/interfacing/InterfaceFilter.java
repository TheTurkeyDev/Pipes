package com.theprogrammingturkey.pipes.network.interfacing;

import java.util.ArrayList;
import java.util.List;

import com.theprogrammingturkey.pipes.util.FilterStack;
import com.theprogrammingturkey.pipes.util.ItemStackHelper;

import net.minecraft.util.EnumFacing;

public class InterfaceFilter
{
	public DirectionFilter inputFilter = new DirectionFilter();
	public DirectionFilter outputFilter = new DirectionFilter();

	public EnumFacing facing = EnumFacing.NORTH;

	public boolean hasStackInFilter(boolean input, FilterStack stack)
	{
		if(input)
			return inputFilter.hasStackInFilter(stack);
		return outputFilter.hasStackInFilter(stack);
	}

	public void addStackToFilter(boolean input, FilterStack stack)
	{
		if(input)
			inputFilter.addStackToFilter(stack);
		else
			outputFilter.addStackToFilter(stack);
	}

	public List<FilterStack> getStacks(boolean input)
	{
		return input ? inputFilter.getStacks() : outputFilter.getStacks();
	}

	public boolean isWhiteList(boolean input)
	{
		return input ? inputFilter.isWhiteList : outputFilter.isWhiteList;
	}

	public static class DirectionFilter
	{
		public boolean isWhiteList = false;
		public int priority = 0;
		public boolean enabled = true;
		private List<FilterStack> filterStacks = new ArrayList<>();

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
}
