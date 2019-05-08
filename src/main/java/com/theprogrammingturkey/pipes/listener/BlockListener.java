package com.theprogrammingturkey.pipes.listener;

import com.theprogrammingturkey.pipes.blocks.BasePipeBlock;
import com.theprogrammingturkey.pipes.util.RegistryHelper;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockListener
{
	@SubscribeEvent
	public void onBlockPlace(PlaceEvent event)
	{
		if(event.getState().getBlock().hasTileEntity(event.getState()))
		{
			for(EnumFacing side : EnumFacing.VALUES)
			{
				BlockPos sidePos = event.getPos().offset(side);
				Block sideBlock = event.getWorld().getBlockState(sidePos).getBlock();
				if(sideBlock instanceof BasePipeBlock)
				{
					if(sideBlock.equals(RegistryHelper.ITEM_PIPE))
					{
						System.out.println("TE Added!");
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event)
	{
		if(event.getState().getBlock().hasTileEntity(event.getState()))
		{
			for(EnumFacing side : EnumFacing.VALUES)
			{
				BlockPos sidePos = event.getPos().offset(side);
				Block sideBlock = event.getWorld().getBlockState(sidePos).getBlock();
				if(sideBlock instanceof BasePipeBlock)
				{
					if(sideBlock.equals(RegistryHelper.ITEM_PIPE))
					{
						System.out.println("TE Removed!");
					}
				}
			}
		}
	}
}
