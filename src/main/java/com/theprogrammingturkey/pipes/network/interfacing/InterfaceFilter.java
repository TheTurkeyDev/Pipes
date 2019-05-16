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

	//Facing direction of the ItemHandler that this is interfacing with
	public EnumFacing facing;

	private boolean showingInput = true;

	public InterfaceFilter(EnumFacing facing)
	{
		this.facing = facing;
	}

	public boolean hasStackInFilter(FilterStack stack)
	{
		if(showingInput)
			return inputFilter.hasStackInFilter(stack);
		return outputFilter.hasStackInFilter(stack);
	}

	public void addStackToFilter(FilterStack stack)
	{
		if(showingInput)
			inputFilter.addStackToFilter(stack);
		else
			outputFilter.addStackToFilter(stack);
	}

	public List<FilterStack> getStacks()
	{
		return showingInput ? inputFilter.getStacks() : outputFilter.getStacks();
	}

	public boolean isWhiteList()
	{
		return showingInput ? inputFilter.isWhiteList : outputFilter.isWhiteList;
	}

	public void setWhiteList(boolean whiteList)
	{
		if(showingInput)
			inputFilter.isWhiteList = whiteList;
		else
			outputFilter.isWhiteList = whiteList;
	}

	public boolean isEnabled()
	{
		return showingInput ? inputFilter.enabled : outputFilter.enabled;
	}

	public void setEnabled(boolean enabled)
	{
		if(showingInput)
			inputFilter.enabled = enabled;
		else
			outputFilter.enabled = enabled;
	}

	public int getPriority()
	{
		return showingInput ? inputFilter.priority : outputFilter.priority;
	}

	public void setPriority(int priority)
	{
		if(showingInput)
			inputFilter.priority = priority;
		else
			outputFilter.priority = priority;
	}

	public void incPriority()
	{
		if(showingInput)
			inputFilter.priority++;
		else
			outputFilter.priority++;
	}

	public void decPriority()
	{
		if(showingInput)
			inputFilter.priority--;
		else
			outputFilter.priority--;
	}

	public void setShowInputFilter(boolean showingInput)
	{
		this.showingInput = showingInput;
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
