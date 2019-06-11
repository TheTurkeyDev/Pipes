package com.theprogrammingturkey.pipes.commands;

import com.theprogrammingturkey.pipes.network.IPipeNetwork;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
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
			BlockPos below = sender.getPosition().add(0, -1, 0);
			IPipeNetwork network = PipeNetworkManager.getNetworksAtPos(below, sender.getEntityWorld().provider.getDimension()).get(0);
			if(network != null)
				sender.sendMessage(new TextComponentString(String.valueOf(network.getNetworkID())));
			else
				sender.sendMessage(new TextComponentString("None"));
		}
	}

}
