package com.theprogrammingturkey.pipes.listener;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldListener
{
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(!event.getWorld().isRemote)
			System.out.println("Here " + event.getWorld());
	}
}
