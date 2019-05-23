package com.theprogrammingturkey.pipes.packets;

import com.theprogrammingturkey.pipes.network.IPipeNetwork;
import com.theprogrammingturkey.pipes.network.InterfaceFilter;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;
import com.theprogrammingturkey.pipes.util.FilterStack;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateFilterPacket implements IMessage
{
	private InterfaceFilter filter;
	private BlockPos pos;

	public UpdateFilterPacket()
	{
	}

	/**
	 * 
	 * @param pos
	 *            Position of the pipe block/ block holding the filter
	 * @param facing
	 *            The interfacing face of the ItemHandler
	 */
	public UpdateFilterPacket(BlockPos pos, InterfaceFilter filter)
	{
		this.pos = pos;
		this.filter = filter;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());

		buf.writeByte(filter.facing.getIndex());
		buf.writeByte(filter.getNetworkType().getID());

		buf.writeBoolean(filter.extractFilter.enabled);
		buf.writeInt(filter.extractFilter.priority);
		buf.writeBoolean(filter.extractFilter.isWhiteList);
		buf.writeInt(filter.extractFilter.getStacks().size());
		for(FilterStack stack : filter.extractFilter.getStacks())
			ByteBufUtils.writeItemStack(buf, stack.getAsItemStack());

		buf.writeBoolean(filter.insertFilter.enabled);
		buf.writeInt(filter.insertFilter.priority);
		buf.writeBoolean(filter.insertFilter.isWhiteList);
		buf.writeInt(filter.insertFilter.getStacks().size());
		for(FilterStack stack : filter.insertFilter.getStacks())
			ByteBufUtils.writeItemStack(buf, stack.getAsItemStack());
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

		filter = new InterfaceFilter(EnumFacing.VALUES[buf.readByte()], NetworkType.getFromID(buf.readInt()));

		filter.extractFilter.enabled = buf.readBoolean();
		filter.extractFilter.priority = buf.readInt();
		filter.extractFilter.isWhiteList = buf.readBoolean();
		int amount = buf.readInt();
		for(int i = 0; i < amount; i++)
			filter.extractFilter.addStackToFilter(new FilterStack(ByteBufUtils.readItemStack(buf)));

		filter.insertFilter.enabled = buf.readBoolean();
		filter.insertFilter.priority = buf.readInt();
		filter.insertFilter.isWhiteList = buf.readBoolean();
		amount = buf.readInt();
		for(int i = 0; i < amount; i++)
			filter.insertFilter.addStackToFilter(new FilterStack(ByteBufUtils.readItemStack(buf)));
	}

	public static final class Handler implements IMessageHandler<UpdateFilterPacket, IMessage>
	{
		@Override
		public IMessage onMessage(UpdateFilterPacket message, MessageContext ctx)
		{
			PipeNetworkManager networkManager = PipeNetworkManager.getNetworkManagerAtPos(ctx.getServerHandler().player.world, message.pos);
			if(networkManager == null)
				return null;
			IPipeNetwork network = networkManager.getNetwork(message.pos);
			if(network == null)
				return null;
			network.updateFilter(message.pos, message.filter);
			return null;
		}
	}
}
