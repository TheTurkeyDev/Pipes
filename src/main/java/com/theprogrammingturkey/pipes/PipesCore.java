package com.theprogrammingturkey.pipes;

import org.apache.logging.log4j.Logger;

import com.theprogrammingturkey.pipes.commands.PipesCommands;
import com.theprogrammingturkey.pipes.listener.GameListener;
import com.theprogrammingturkey.pipes.listener.WorldListener;
import com.theprogrammingturkey.pipes.packets.PipesPacketHandler;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = PipesCore.MODID, name = PipesCore.NAME, version = PipesCore.VERSION)
public class PipesCore
{
	public static final String MODID = "pipes";
	public static final String NAME = "Pipes";
	public static final String VERSION = "0.1";

	public static Logger logger;

	public static CreativeTabs modTab = new CreativeTabs(MODID)
	{
		public ItemStack createIcon()
		{
			return new ItemStack(RegistryHelper.ITEM_PIPE);
		}
	};

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		MinecraftForge.EVENT_BUS.register(new RegistryHelper());
		MinecraftForge.EVENT_BUS.register(new GameListener());
		MinecraftForge.EVENT_BUS.register(new WorldListener());

		PipesPacketHandler.init();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		if(event.getSide().equals(Side.CLIENT))
			RegistryHelper.registerItemsModels();
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new PipesCommands());
	}
}
