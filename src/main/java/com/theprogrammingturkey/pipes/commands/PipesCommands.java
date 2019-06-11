package com.theprogrammingturkey.pipes.commands;

import java.util.List;

import com.theprogrammingturkey.pipes.network.IPipeNetwork;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class PipesCommands extends CommandBase
{

	@Override
	public String getName()
	{
		return "pipes";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/pipes";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length == 0)
			return;
		if(args[0].equalsIgnoreCase("test"))
		{
			sender.sendMessage(new TextComponentString("Network ID's Below:"));
			List<IPipeNetwork> networks = PipeNetworkManager.getNetworksAtPos(sender.getPosition().add(0, -1, 0), sender.getEntityWorld().provider.getDimension());
			if(networks.size() > 0)
			{
				for(IPipeNetwork network : networks)
					sender.sendMessage(new TextComponentString(String.valueOf(network.getNetworkID())));
			}
			else
			{
				sender.sendMessage(new TextComponentString("None"));
			}
		}
	}

}
