package com.theprogrammingturkey.pipes.containers;

import com.theprogrammingturkey.pipes.network.InterfaceFilter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class FilterContainer extends Container
{
	private final InterfaceFilter filter;
	private final int numRows;

	public FilterContainer(IInventory playerInventory, InterfaceFilter filter, EntityPlayer player)
	{
		this.filter = filter;
		this.numRows = 1;
		int i = (this.numRows - 4) * 18;

		for(int l = 0; l < 3; ++l)
		{
			for(int j1 = 0; j1 < 9; ++j1)
			{
				this.addSlotToContainer(new Slot(playerInventory, j1 + l * 9 + 9, 47 + j1 * 18, 150 + l * 18 + i));
			}
		}

		for(int i1 = 0; i1 < 9; ++i1)
		{
			this.addSlotToContainer(new Slot(playerInventory, i1, 47 + i1 * 18, 208 + i));
		}
	}

	/**
	 * Determines whether supplied player can use this container
	 */
	public boolean canInteractWith(EntityPlayer playerIn)
	{
		return true;
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack
	 * between the player inventory and the other inventory(s).
	 */
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
	{
		return ItemStack.EMPTY;
	}
}
