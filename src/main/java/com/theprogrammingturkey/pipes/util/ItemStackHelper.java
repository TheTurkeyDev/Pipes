package com.theprogrammingturkey.pipes.util;

import net.minecraft.item.ItemStack;

public class ItemStackHelper
{

	public static boolean areItemStacksEqual(ItemStack stack1, ItemStack stack2)
	{
		if(stack1.getItem() != stack2.getItem())
			return false;

		if(stack1.getItemDamage() != stack2.getItemDamage())
			return false;

		if(stack1.getTagCompound() == null && stack2.getTagCompound() != null && !stack2.getTagCompound().isEmpty())
			return false;

		if(stack1.getTagCompound().equals(stack2.getTagCompound()))
			return false;

		if(!stack1.areCapsCompatible(stack2))
			return false;

		return true;
	}

	public static boolean areFilterStacksEqual(FilterStack stack1, FilterStack stack2)
	{
		if(stack1.item != stack2.item)
			return false;

		if(stack1.meta != stack2.meta)
			return false;

		if(stack1.nbt == null && stack2.nbt != null && !stack2.nbt.isEmpty())
			return false;

		if(stack1.nbt.equals(stack2.nbt))
			return false;

		return true;
	}
	
	
}
