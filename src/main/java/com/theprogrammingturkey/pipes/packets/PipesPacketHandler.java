package com.theprogrammingturkey.pipes.packets;

import com.theprogrammingturkey.pipes.PipesCore;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PipesPacketHandler
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PipesCore.MODID);
	private static int id = 0;
	
	public static void init()
	{
		INSTANCE.registerMessage(GetFilterPacket.Handler.class, GetFilterPacket.class, id++, Side.SERVER);
		INSTANCE.registerMessage(UpdateFilterPacket.Handler.class, UpdateFilterPacket.class, id++, Side.SERVER);
		INSTANCE.registerMessage(RecieveFilterPacket.Handler.class, RecieveFilterPacket.class, id++, Side.CLIENT);
	}
}
