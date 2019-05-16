package com.theprogrammingturkey.pipes.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class FilterStack
{
	public Item item;
	public int meta;
	public NBTTagCompound nbt;

	public FilterStack(ItemStack stack)
	{
		this.item = stack.getItem();
		this.meta = stack.getItemDamage();
		this.nbt = stack.getTagCompound();
	}

	public boolean equals(Object obj)
	{
		if(!(obj instanceof FilterStack))
			return false;

		return true;
	}

	public ItemStack getAsItemStack()
	{
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
}
