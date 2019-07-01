package com.theprogrammingturkey.pipes.util;

import com.theprogrammingturkey.pipes.network.filtering.InterfaceFilter;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class StackInfo<T>
{
	public T inv;
	public int slot;
	public int amount;
	public int amountLeft;
	public InterfaceFilter filter;

	public StackInfo(T inv, InterfaceFilter filter, int amount)
	{
		this(inv, filter, amount, 0);
	}

	public StackInfo(T inv, InterfaceFilter filter, int amount, int slot)
	{
		this.inv = inv;
		this.filter = filter;
		this.amount = amount;
		this.amountLeft = amount;
	}

	public FluidStack getFluidStack(Fluid fluid)
	{
		return new FluidStack(fluid, amountLeft);
	}
}