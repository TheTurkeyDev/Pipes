package com.theprogrammingturkey.pipes.listener;

import com.theprogrammingturkey.pipes.network.PipeNetworkManager;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class GameListener
{
	@SubscribeEvent
	public void onTick(WorldTickEvent event)
	{
		if(event.side == Side.SERVER && event.phase == Phase.START)
			PipeNetworkManager.tickManagers(event.world);
	}
}
