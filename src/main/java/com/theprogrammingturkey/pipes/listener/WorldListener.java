package com.theprogrammingturkey.pipes.listener;

import net.minecraftforge.event.world.WorldEvent;

public class WorldListener
{
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(!event.getWorld().isRemote)
			System.out.println("Here " + event.getWorld());
	}
}
