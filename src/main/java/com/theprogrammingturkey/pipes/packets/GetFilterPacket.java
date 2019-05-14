package com.theprogrammingturkey.pipes.packets;

import com.theprogrammingturkey.pipes.network.PipeNetwork;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class GetFilterPacket implements IMessage
{
	private BlockPos pos;
	private EnumFacing side;

	public GetFilterPacket()
	{
		pos = BlockPos.ORIGIN;
		side = EnumFacing.DOWN;
	}

	public GetFilterPacket(BlockPos pos, EnumFacing side)
	{
		this.pos = pos;
		this.side = side;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		buf.writeByte(side.getIndex());
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.side = EnumFacing.VALUES[buf.readByte()];
	}

	public static final class Handler implements IMessageHandler<GetFilterPacket, IMessage>
	{
		@Override
		public IMessage onMessage(GetFilterPacket message, MessageContext ctx)
		{
			if(ctx.side == Side.SERVER)
			{
				PipeNetworkManager networkManager = PipeNetworkManager.getNetworkManagerAtPos(ctx.getServerHandler().player.world, message.pos);
				if(networkManager == null)
					return null;
				PipeNetwork network = networkManager.getNetwork(message.pos);
				if(network == null)
					return null;
				return new RecieveFilterPacket(network.getNetworkInterface().getFilterFromPipe(message.pos, message.side), true);
			}
			return null;
		}
	}
}
