package com.theprogrammingturkey.pipes.network.filtering;

import com.theprogrammingturkey.pipes.util.ItemStackHelper;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class FilterStackItem implements IFilterStack
{
	public Item item;
	public int meta;
	public NBTTagCompound nbt;

	public FilterStackItem(ItemStack stack)
	{
		this.item = stack.getItem();
		this.meta = stack.getItemDamage();
		this.nbt = stack.getTagCompound();
	}

	public boolean equals(Object obj)
	{
		if(!(obj instanceof FilterStackItem))
			return false;

		return true;
	}

	public ItemStack getAsItemStack()
	{
		if(item == null)
			return ItemStack.EMPTY;
		ItemStack stack = new ItemStack(item, 1, meta);
		stack.setTagCompound(nbt);
		return stack;
	}

	public String toString()
	{
		String toReturn = "";
		if(item != null)
			toReturn += item.toString();
		else
			toReturn += "NULL";

		toReturn += " | " + meta + " | ";

		if(nbt != null)
			toReturn += nbt.toString();
		else
			toReturn += "NULL";

		return toReturn;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		return this.getAsItemStack().serializeNBT();
	}

	@Override
	public boolean isEqual(IFilterStack ifs)
	{
		if(!(ifs instanceof FilterStackItem))
			return false;

		FilterStackItem stack = (FilterStackItem) ifs;

		if(this.item != stack.item)
			return false;

		if(this.meta != stack.meta)
			return false;

		return ItemStackHelper.areNBTTagsEqual(this.nbt, stack.nbt);
	}
}
