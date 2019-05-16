package com.theprogrammingturkey.pipes.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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

		return areNBTTagsEqual(stack1.nbt, stack2.nbt);
	}

	public static boolean areNBTTagsEqual(NBTTagCompound nbt1, NBTTagCompound nbt2)
	{
		if((nbt1 == null || nbt1.isEmpty()) && (nbt2 == null || nbt2.isEmpty()))
			return true;
		return nbt1 == null ? false : nbt1.equals(nbt2);
	}

}
