package com.theprogrammingturkey.pipes.packets;

import com.theprogrammingturkey.pipes.network.interfacing.InterfaceFilter;
import com.theprogrammingturkey.pipes.ui.FilterUI;
import com.theprogrammingturkey.pipes.util.FilterStack;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RecieveFilterPacket implements IMessage
{
	private InterfaceFilter filter;
	private boolean openUI;

	public RecieveFilterPacket()
	{
		this.filter = new InterfaceFilter();
		this.openUI = false;
	}

	public RecieveFilterPacket(InterfaceFilter filter, boolean openUI)
	{
		this.filter = filter;
		this.openUI = openUI;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(openUI);
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
		this.openUI = buf.readBoolean();

		filter = new InterfaceFilter();
		filter.facing = EnumFacing.VALUES[buf.readByte()];

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

	public static final class Handler implements IMessageHandler<RecieveFilterPacket, IMessage>
	{
		@Override
		public IMessage onMessage(RecieveFilterPacket message, MessageContext ctx)
		{
			if(message.openUI)
				FMLCommonHandler.instance().showGuiScreen(new FilterUI(Minecraft.getMinecraft().player, message.filter));
			return null;
		}
	}
}
