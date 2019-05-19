package com.theprogrammingturkey.pipes.packets;

import com.theprogrammingturkey.pipes.network.interfacing.InterfaceFilter;
import com.theprogrammingturkey.pipes.ui.FilterUI;
import com.theprogrammingturkey.pipes.util.FilterStack;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RecieveFilterPacket implements IMessage
{
	private InterfaceFilter filter;
	private boolean openUI;
	private int windowID;
	private BlockPos pos;

	public RecieveFilterPacket()
	{

	}

	/**
	 * 
	 * @param pos
	 *            Position of the pipe block/ block holding the filter
	 * @param facing
	 *            The interfacing face of the ItemHandler
	 */
	public RecieveFilterPacket(BlockPos pos, InterfaceFilter filter)
	{
		this.filter = filter;
		this.openUI = false;
		this.pos = pos;
	}

	/**
	 * 
	 * @param pos
	 *            Position of the pipe block/ block holding the filter
	 * @param facing
	 *            The interfacing face of the ItemHandler
	 * @param windowID
	 *            id of the window to open for the player
	 */
	public RecieveFilterPacket(BlockPos pos, InterfaceFilter filter, int windowID)
	{
		this.filter = filter;
		this.openUI = true;
		this.windowID = windowID;
		this.pos = pos;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(openUI);
		buf.writeByte(windowID);

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
		this.openUI = buf.readBoolean();
		this.windowID = buf.readByte();

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

	public static final class Handler implements IMessageHandler<RecieveFilterPacket, IMessage>
	{
		@Override
		public IMessage onMessage(RecieveFilterPacket message, MessageContext ctx)
		{
			if(message.openUI)
			{
				IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.getClientHandler());
				if(thread.isCallingFromMinecraftThread())
				{
					EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
					FMLCommonHandler.instance().showGuiScreen(new FilterUI(player, message.filter, message.pos));
					player.openContainer.windowId = (Integer) message.windowID;
				}
				else
				{
					thread.addScheduledTask(() -> {
						EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
						FMLCommonHandler.instance().showGuiScreen(new FilterUI(player, message.filter, message.pos));
						player.openContainer.windowId = (Integer) message.windowID;
					});
				}
			}
			return null;
		}
	}
}
