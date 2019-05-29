package com.theprogrammingturkey.pipes.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

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

		}
	}

}
