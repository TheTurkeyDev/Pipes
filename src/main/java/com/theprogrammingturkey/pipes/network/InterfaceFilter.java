package com.theprogrammingturkey.pipes.network;

import java.util.ArrayList;
import java.util.List;

import com.theprogrammingturkey.pipes.util.FilterStack;
import com.theprogrammingturkey.pipes.util.ItemStackHelper;

import net.minecraft.util.EnumFacing;

public class InterfaceFilter
{
	public DirectionFilter insertFilter = new DirectionFilter();
	public DirectionFilter extractFilter = new DirectionFilter();

	//Facing direction of the ItemHandler that this is interfacing with
	public EnumFacing facing;

	private boolean showingInsert = true;

	public InterfaceFilter(EnumFacing facing)
	{
		this.facing = facing;
	}

	public boolean hasStackInFilter(FilterStack stack)
	{
		if(showingInsert)
			return insertFilter.hasStackInFilter(stack);
		return extractFilter.hasStackInFilter(stack);
	}

	public void addStackToFilter(FilterStack stack)
	{
		if(showingInsert)
			insertFilter.addStackToFilter(stack);
		else
			extractFilter.addStackToFilter(stack);
	}

	public List<FilterStack> getStacks()
	{
		return showingInsert ? insertFilter.getStacks() : extractFilter.getStacks();
	}

	public boolean isWhiteList()
	{
		return showingInsert ? insertFilter.isWhiteList : extractFilter.isWhiteList;
	}

	public void setWhiteList(boolean whiteList)
	{
		if(showingInsert)
			insertFilter.isWhiteList = whiteList;
		else
			extractFilter.isWhiteList = whiteList;
	}

	public boolean isEnabled()
	{
		return showingInsert ? insertFilter.enabled : extractFilter.enabled;
	}

	public void setEnabled(boolean enabled)
	{
		if(showingInsert)
			insertFilter.enabled = enabled;
		else
			extractFilter.enabled = enabled;
	}

	public int getPriority()
	{
		return showingInsert ? insertFilter.priority : extractFilter.priority;
	}

	public void setPriority(int priority)
	{
		if(showingInsert)
			insertFilter.priority = priority;
		else
			extractFilter.priority = priority;
	}

	public void incPriority()
	{
		if(showingInsert)
			insertFilter.priority++;
		else
			extractFilter.priority++;
	}

	public void decPriority()
	{
		if(showingInsert)
			insertFilter.priority--;
		else
			extractFilter.priority--;
	}

	public void setShowInsertFilter(boolean showingInsert)
	{
		this.showingInsert = showingInsert;
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
