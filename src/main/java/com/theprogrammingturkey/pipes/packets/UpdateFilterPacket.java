package com.theprogrammingturkey.pipes.packets;

import com.theprogrammingturkey.pipes.network.PipeNetwork;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.network.interfacing.InterfaceFilter;
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

		buf.writeBoolean(filter.inputFilter.enabled);
		buf.writeInt(filter.inputFilter.priority);
		buf.writeBoolean(filter.inputFilter.isWhiteList);
		buf.writeInt(filter.inputFilter.getStacks().size());
		for(FilterStack stack : filter.inputFilter.getStacks())
			ByteBufUtils.writeItemStack(buf, stack.getAsItemStack());

		buf.writeBoolean(filter.outputFilter.enabled);
		buf.writeInt(filter.outputFilter.priority);
		buf.writeBoolean(filter.outputFilter.isWhiteList);
		buf.writeInt(filter.outputFilter.getStacks().size());
		for(FilterStack stack : filter.outputFilter.getStacks())
			ByteBufUtils.writeItemStack(buf, stack.getAsItemStack());
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

		filter = new InterfaceFilter(EnumFacing.VALUES[buf.readByte()]);

		filter.inputFilter.enabled = buf.readBoolean();
		filter.inputFilter.priority = buf.readInt();
		filter.inputFilter.isWhiteList = buf.readBoolean();
		int amount = buf.readInt();
		for(int i = 0; i < amount; i++)
			filter.inputFilter.addStackToFilter(new FilterStack(ByteBufUtils.readItemStack(buf)));

		filter.outputFilter.enabled = buf.readBoolean();
		filter.outputFilter.priority = buf.readInt();
		filter.outputFilter.isWhiteList = buf.readBoolean();
		amount = buf.readInt();
		for(int i = 0; i < amount; i++)
			filter.outputFilter.addStackToFilter(new FilterStack(ByteBufUtils.readItemStack(buf)));
	}

	public static final class Handler implements IMessageHandler<UpdateFilterPacket, IMessage>
	{
		@Override
		public IMessage onMessage(UpdateFilterPacket message, MessageContext ctx)
		{
			PipeNetworkManager networkManager = PipeNetworkManager.getNetworkManagerAtPos(ctx.getServerHandler().player.world, message.pos);
			if(networkManager == null)
				return null;
			PipeNetwork network = networkManager.getNetwork(message.pos);
			if(network == null)
				return null;
			network.getNetworkInterface().updateFilter(message.pos, message.filter);
			return null;
		}
	}
}
