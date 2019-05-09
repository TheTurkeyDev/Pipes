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
		ItemStack stack = new ItemStack(item, meta);
		stack.setTagCompound(nbt);
		return stack;
	}
}
