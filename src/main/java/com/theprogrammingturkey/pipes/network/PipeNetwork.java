package com.theprogrammingturkey.pipes.network;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theprogrammingturkey.pipes.capabilities.EntityUtil;
import com.theprogrammingturkey.pipes.capabilities.IEntityHolder;
import com.theprogrammingturkey.pipes.network.filtering.InterfaceFilter;
import com.theprogrammingturkey.pipes.util.Util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public abstract class PipeNetwork<T> implements IPipeNetwork
{
	public static final long FACING_NUM_BITS = 3;
	//I think this is right..... see BlockPos.NUM_Y_BITS
	public static final long FACING_BIT_SHIFT = 60 - MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
	public static final long FACING_MASK = (1L << FACING_NUM_BITS) - 1L;

	private boolean isActive = true;
	private int networkID;
	private int dimID;
	private NetworkType type;
	private List<Long> containedBlockPos = new ArrayList<>();
	private Map<Long, List<Long>> chunkToBlocks = new HashMap<>();

	private Capability<T> holderCap;

	protected HashMap<Long, InterfaceInfo<T>> interfaces = new HashMap<>();
	protected List<HandlerHolder<T>> toUpdate = new ArrayList<>();

	private boolean networkChanged = true;
	private boolean requiresUpdate = false;
	private boolean requiresPassiveUpdate = false;

	private int passiveBlockCheck = 600;
	private int passiveBlockCheckTick = passiveBlockCheck;

	protected Comparator<InterfaceInfo<T>> insertPrioritySort = new Comparator<InterfaceInfo<T>>()
	{
		@Override
		public int compare(InterfaceInfo<T> ii1, InterfaceInfo<T> ii2)
		{
			return ii2.filter.insertFilter.priority - ii1.filter.insertFilter.priority;
		}
	};

	protected Comparator<InterfaceInfo<T>> extractPrioritySort = new Comparator<InterfaceInfo<T>>()
	{
		@Override
		public int compare(InterfaceInfo<T> ii1, InterfaceInfo<T> ii2)
		{
			return ii2.filter.extractFilter.priority - ii1.filter.extractFilter.priority;
		}
	};

	public PipeNetwork(int networkID, int dimID, NetworkType type, Capability<T> holderCap)
	{
		this.networkID = networkID;
		this.dimID = dimID;
		this.type = type;
		this.holderCap = holderCap;
		networkChanged = true;
	}

	@Override
	public void tick()
	{
		if(containedBlockPos.size() == 0)
		{
			this.isActive = false;
			return;
		}

		for(HandlerHolder<T> holder : this.toUpdate)
		{
			long hash = getKeyHash(holder.pos, holder.facing);
			if(interfaces.containsKey(hash))
			{
				/*
				 * TE hash is just here because this method will often get triggered multiple times
				 * without the te actually changing. This may help keep thing clean in the future
				 * instead of making a new InterfaceInfo every time and only do it when the te
				 * actually changes.
				 */
				InterfaceInfo<T> info = interfaces.get(hash);
				if(!holder.isTE || holder.teHash != info.teHash)
				{
					interfaces.put(hash, new InterfaceInfo<T>(holder.handler, holder.filter, holder.facing, holder.teHash));
					setNetworkChanged();
				}
			}
			else
			{
				interfaces.put(hash, new InterfaceInfo<T>(holder.handler, holder.filter, holder.facing, holder.teHash));
				setNetworkChanged();
			}
		}
		this.toUpdate.clear();

		passiveBlockCheckTick--;
		if(passiveBlockCheckTick == 0)
		{
			this.requiresPassiveUpdate = true;
			passiveBlockCheckTick = passiveBlockCheck;
		}

		processTransfers();
	}

	public void update(World world)
	{
		this.requiresUpdate = false;
		PipeNetworkManager manager = PipeNetworkManager.getNetworkManagerForType(type);
		List<BlockPos> toRemove = new ArrayList<>();
		for(Long posLong : this.containedBlockPos)
		{
			BlockPos pos = BlockPos.fromLong(posLong);

			if(type.areBlockAndTypeEqual(world.getBlockState(pos)))
			{
				for(EnumFacing facing : EnumFacing.VALUES)
				{
					InterfaceFilter filter = new InterfaceFilter(facing.getOpposite(), this.type);
					long hash = this.getKeyHash(pos, facing.getOpposite());

					//Check both the loaded and queued interfaces for the capholder to update
					if(interfaces.containsKey(hash))
						filter = interfaces.get(hash).filter;
					else
						for(HandlerHolder<T> holder : this.toUpdate)
							if(holder.handlerPos.equals(pos.offset(facing)) && holder.facing == facing.getOpposite())
								filter = holder.filter;

					addInterfacedBlock(world, pos, facing.getOpposite(), filter);
				}
			}
			else
			{
				toRemove.add(pos);
			}
		}

		for(BlockPos pos : toRemove)
			manager.removePipeFromNetwork(world, pos);

		interfaces.clear();
	}

	public void passiveUpdate(World world)
	{
		this.requiresPassiveUpdate = false;
		PipeNetworkManager manager = PipeNetworkManager.getNetworkManagerForType(type);
		List<BlockPos> toRemove = new ArrayList<>();
		for(Long posLong : this.containedBlockPos)
		{
			BlockPos pos = BlockPos.fromLong(posLong);
			if(!type.areBlockAndTypeEqual(world.getBlockState(pos)))
				toRemove.add(pos);
		}

		for(BlockPos pos : toRemove)
			manager.removePipeFromNetwork(world, pos);
	}

	public boolean requiresUpdate()
	{
		return this.requiresUpdate;
	}

	public boolean requiresPassiveUpdate()
	{
		return this.requiresPassiveUpdate;
	}

	public abstract void processTransfers();

	@Override
	public void addBlockPosToNetwork(BlockPos pos)
	{
		long posLong = pos.toLong();
		containedBlockPos.add(posLong);
		long chunkHash = Util.chunkToLong(pos);
		List<Long> containedBlocks = chunkToBlocks.get(chunkHash);
		if(containedBlocks == null)
		{
			containedBlocks = new ArrayList<>();
			chunkToBlocks.put(chunkHash, containedBlocks);
		}
		containedBlocks.add(posLong);
		setNetworkChanged();
	}

	public void removeBlockPosFromNetwork(BlockPos pos)
	{
		long posLong = pos.toLong();
		containedBlockPos.remove(posLong);
		long chunkHash = Util.chunkToLong(pos);
		List<Long> containedBlocks = chunkToBlocks.get(chunkHash);
		if(containedBlocks != null)
			containedBlocks.remove(posLong);
		setNetworkChanged();
	}

	public boolean isPosInNetwork(BlockPos pos)
	{
		return containedBlockPos.contains(pos.toLong());
	}

	public List<Long> getcontainedBlockPos()
	{
		return containedBlockPos;
	}

	@SuppressWarnings("unchecked")
	public void addInterfacedBlock(World world, BlockPos pos, EnumFacing facing, InterfaceFilter filter)
	{
		BlockPos offsetPos = pos.offset(facing.getOpposite());
		TileEntity te = world.getTileEntity(offsetPos);
		//Because of Furnaces we need to cache this stuff and only add it all once per tick
		for(int i = toUpdate.size() - 1; i >= 0; i--)
		{
			HandlerHolder<T> holder = toUpdate.get(i);
			if(holder.handlerPos.equals(offsetPos))
				toUpdate.remove(i);
		}

		if(te != null)
		{
			if(!te.hasCapability(holderCap, facing))
				return;
			this.toUpdate.add(new HandlerHolder<T>(te.getCapability(holderCap, facing), world, pos, facing, filter, true, te.hashCode()));
		}
		else
		{
			if(this.type == NetworkType.FLUID)
			{
				IFluidHandler handler = FluidUtil.getFluidHandler(world, offsetPos, facing);
				if(handler != null)
					this.toUpdate.add((HandlerHolder<T>) new HandlerHolder<IFluidHandler>(handler, world, pos, facing, filter, false, 0));
			}
			else if(this.type == NetworkType.ENTITY)
			{
				IEntityHolder handler = EntityUtil.getEntityBlockHandler(world, offsetPos);
				if(handler != null)
					this.toUpdate.add((HandlerHolder<T>) new HandlerHolder<IEntityHolder>(handler, world, pos, facing, filter, false, 0));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void updateInterfacedBlock(World world, BlockPos pos, EnumFacing facing, InterfaceFilter filter)
	{
		BlockPos offsetPos = pos.offset(facing.getOpposite());
		TileEntity te = world.getTileEntity(offsetPos);

		//Because of Furnaces we need to cache this stuff and only add it all once per tick
		for(int i = toUpdate.size() - 1; i >= 0; i--)
		{
			HandlerHolder<T> holder = toUpdate.get(i);
			if(holder.handlerPos.equals(offsetPos))
				toUpdate.remove(i);
		}

		if(te != null)
		{
			if(!te.hasCapability(holderCap, facing))
				return;
			this.toUpdate.add(new HandlerHolder<T>(te.getCapability(holderCap, facing), world, pos, facing, filter, true, te.hashCode()));
		}
		else
		{
			boolean flag = true;
			if(this.type == NetworkType.FLUID)
			{
				IFluidHandler handler = FluidUtil.getFluidHandler(world, offsetPos, facing);
				if(handler != null)
				{
					this.toUpdate.add((HandlerHolder<T>) new HandlerHolder<IFluidHandler>(handler, world, pos, facing, filter, false, 0));
					flag = false;
				}
			}
			else if(this.type == NetworkType.ENTITY)
			{
				IEntityHolder handler = EntityUtil.getEntityBlockHandler(world, offsetPos);
				if(handler != null)
					this.toUpdate.add((HandlerHolder<T>) new HandlerHolder<IEntityHolder>(handler, world, pos, facing, filter, false, 0));
			}

			if(flag)
				removeInterfacedBlock(world, pos, facing);
		}
	}

	public void removeInterfacedBlock(World world, BlockPos pos, EnumFacing facing)
	{
		interfaces.remove(getKeyHash(pos, facing));
		setNetworkChanged();
	}

	public void updateFilter(BlockPos pos, InterfaceFilter filter)
	{
		InterfaceInfo<?> posInterfaces = interfaces.get(getKeyHash(pos, filter.facing));
		posInterfaces.filter = filter;
	}

	public InterfaceFilter getFilterFromPipe(BlockPos pos, EnumFacing facing)
	{
		InterfaceInfo<?> posInterfaces = interfaces.get(this.getKeyHash(pos, facing));
		return posInterfaces == null ? null : posInterfaces.filter;
	}

	@Override
	public int getNetworkID()
	{
		return this.networkID;
	}

	@Override
	public int getDimID()
	{
		return this.dimID;
	}

	@Override
	public NetworkType getNetworkType()
	{
		return this.type;
	}

	@SuppressWarnings("unchecked")
	public void mergeWithNetwork(IPipeNetwork toMerge)
	{
		if(toMerge.getNetworkType().equals(this.type))
		{
			this.containedBlockPos.addAll(toMerge.getcontainedBlockPos());
			this.interfaces.putAll(((PipeNetwork<T>) toMerge).interfaces);
		}
		setNetworkChanged();
	}

	@Override
	public void deleteNetwork()
	{
		isActive = false;
		containedBlockPos.clear();
		this.chunkToBlocks.clear();
		setNetworkChanged();
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}

	public boolean isInChunk(int x, int z)
	{
		return this.chunkToBlocks.containsKey(Util.chunkToLong(x, z));
	}

	public void setNetworkChanged()
	{
		this.networkChanged = true;
	}

	public boolean shouldSave()
	{
		return networkChanged;
	}

	public void loadNetworkInChunk(World world, int x, int z, NBTTagCompound nbt)
	{
		List<Long> blocksContained = new ArrayList<>();

		for(String keyLong : nbt.getKeySet())
		{
			Long l = Long.parseLong(keyLong);
			blocksContained.add(l);
			NBTTagCompound pipe = nbt.getCompoundTag(keyLong);

			BlockPos pos = new BlockPos(pipe.getInteger("x"), pipe.getInteger("y"), pipe.getInteger("z"));
			this.addBlockPosToNetwork(pos);

			NBTTagList interfaces = pipe.getTagList("interfaces", 10);
			for(NBTBase interfaceBase : interfaces)
			{
				NBTTagCompound interfaceNBT = (NBTTagCompound) interfaceBase;
				EnumFacing facing = EnumFacing.byIndex(interfaceNBT.getInteger("facing"));
				InterfaceFilter filter = InterfaceFilter.fromNBT(facing, type, interfaceNBT.getCompoundTag("filter"));

				//TODO: Look into possible chunk boundry issues with the interfaced blocks
				this.addInterfacedBlock(world, pos, facing, filter);
			}
		}

		requiresUpdate = true;
		this.setNetworkChanged();
	}

	public NBTTagCompound saveNetworkInchunk(int x, int z)
	{
		NBTTagCompound nbt = new NBTTagCompound();

		for(Long l : this.chunkToBlocks.get(Util.chunkToLong(x, z)))
		{
			NBTTagCompound pipeNBT = new NBTTagCompound();
			BlockPos pos = BlockPos.fromLong(l);
			pipeNBT.setInteger("x", pos.getX());
			pipeNBT.setInteger("y", pos.getY());
			pipeNBT.setInteger("z", pos.getZ());
			NBTTagList interfaces = new NBTTagList();
			pipeNBT.setTag("interfaces", interfaces);
			nbt.setTag(String.valueOf(l), pipeNBT);

			for(EnumFacing facing : EnumFacing.VALUES)
			{
				long hash = this.getKeyHash(l, facing);
				if(this.interfaces.containsKey(hash))
				{
					NBTTagCompound face = new NBTTagCompound();
					InterfaceInfo<T> interfaceInfo = this.interfaces.get(hash);

					face.setInteger("facing", interfaceInfo.facing.getIndex());
					face.setTag("filter", interfaceInfo.filter.toNBT());

					interfaces.appendTag(face);
				}
			}
		}

		networkChanged = false;
		return nbt;
	}

	public Long getKeyHash(BlockPos pos, EnumFacing facing)
	{
		return getKeyHash(pos.toLong(), facing);
	}

	public Long getKeyHash(long pos, EnumFacing facing)
	{
		/*
		 * Essentially I'm using the upper 3 bits of the Y coordinate value. Based on my maths and
		 * info found in BlockPos, the Y_SHIFT should be 12 allowing for values of 0-4096, but since
		 * the y coord should never go that high, I'm using the upper 3 bits to store the facing
		 * value (0-5) leaving 9 bits left for the y before it overflows (0-512), it's close, but I
		 * think it'll work. Maybe there's a better way, but idk.
		 */
		return pos | ((long) facing.getIndex() & FACING_MASK) << FACING_BIT_SHIFT;
	}

	public static IPipeNetwork getNewNetwork(NetworkType type, int networkID, int dimID)
	{
		switch(type)
		{
			case ITEM:
				return new ItemPipeNetwork(networkID, dimID);
			case FLUID:
				return new FluidPipeNetwork(networkID, dimID);
			case ENERGY:
				return new EnergyPipeNetwork(networkID, dimID);
			case ENTITY:
				return new EntityPipeNetwork(networkID, dimID);
			default:
				return new ItemPipeNetwork(networkID, dimID);
		}
	}
}