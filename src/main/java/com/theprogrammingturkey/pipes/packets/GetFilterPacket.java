package com.theprogrammingturkey.pipes.packets;

import com.theprogrammingturkey.pipes.network.IPipeNetwork;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class GetFilterPacket implements IMessage
{
	private NetworkType type;
	private BlockPos pos;
	private EnumFacing side;

	public GetFilterPacket()
	{
		pos = BlockPos.ORIGIN;
		side = EnumFacing.DOWN;
	}

	/**
	 * 
	 * @param pos
	 *            Position of the pipe block/ block holding the filter
	 * @param facing
	 *            The interfacing face of the ItemHandler
	 */
	public GetFilterPacket(NetworkType type, BlockPos pos, EnumFacing side)
	{
		this.type = type;
		this.pos = pos;
		this.side = side;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(type.getID());
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		buf.writeByte(side.getIndex());
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.type = NetworkType.getFromID(buf.readInt());
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
				World world = ctx.getServerHandler().player.world;
				PipeNetworkManager networkManager = PipeNetworkManager.getNetworkManagerForType(message.type);
				IPipeNetwork network = networkManager.getNetwork(message.pos, world.provider.getDimension());
				if(network == null)
					return null;
				return new RecieveFilterPacket(message.pos, network.getFilterFromPipe(message.pos, message.side));
			}
			return null;
		}
	}
}
