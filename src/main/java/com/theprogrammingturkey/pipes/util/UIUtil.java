package com.theprogrammingturkey.pipes.util;

import com.theprogrammingturkey.pipes.containers.FilterContainer;
import com.theprogrammingturkey.pipes.network.filtering.InterfaceFilter;
import com.theprogrammingturkey.pipes.packets.PipesPacketHandler;
import com.theprogrammingturkey.pipes.packets.RecieveFilterPacket;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;

public class UIUtil
{
	public static void openFilterUI(EntityPlayerMP player, BlockPos pos, InterfaceFilter filter)
	{
		if(filter == null)
			return;
		player.getNextWindowId();
		player.closeContainer();
		int id = player.currentWindowId;
		PipesPacketHandler.INSTANCE.sendTo(new RecieveFilterPacket(pos, filter, id), player);
		player.openContainer = new FilterContainer(player.inventory, filter, player);
		player.openContainer.windowId = id;
		player.openContainer.addListener(player);
	}
}
