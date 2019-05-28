package com.theprogrammingturkey.pipes.listener;

import com.theprogrammingturkey.pipes.network.PipeNetworkManager;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class GameListener
{
	private static final int FORWARDING_TABLE_INTERVAL = 1200;

	private static int tick = 0;

	@SubscribeEvent
	public void onTick(WorldTickEvent event)
	{
		if(event.side == Side.SERVER && event.phase == Phase.START)
		{
			tick++;
			PipeNetworkManager.tickManagers(event.world);
			if(tick % FORWARDING_TABLE_INTERVAL == 0)
				PipeNetworkManager.purgeForwardingTables();
		}
	}
}
