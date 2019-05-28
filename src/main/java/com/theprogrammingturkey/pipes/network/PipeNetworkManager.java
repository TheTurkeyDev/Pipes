package com.theprogrammingturkey.pipes.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.theprogrammingturkey.pipes.util.Util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PipeNetworkManager
{
	private static final Map<NetworkType, PipeNetworkManager> NETWORK_MANAGERS = new HashMap<NetworkType, PipeNetworkManager>();

	static
	{
		NETWORK_MANAGERS.put(NetworkType.ITEM, new PipeNetworkManager(NetworkType.ITEM));
		NETWORK_MANAGERS.put(NetworkType.FLUID, new PipeNetworkManager(NetworkType.FLUID));
		NETWORK_MANAGERS.put(NetworkType.ENERGY, new PipeNetworkManager(NetworkType.ENERGY));
	}

	public static PipeNetworkManager getNetworkManagerAtPos(World world, BlockPos pos)
	{
		return getNetworkManagerForBlockState(world.getBlockState(pos));
	}

	public static PipeNetworkManager getNetworkManagerForBlockState(IBlockState state)
	{
		if(Util.areBlockAndTypeEqual(NetworkType.ITEM, state.getBlock()))
			return NETWORK_MANAGERS.get(NetworkType.ITEM);
		else if(Util.areBlockAndTypeEqual(NetworkType.FLUID, state.getBlock()))
			return NETWORK_MANAGERS.get(NetworkType.FLUID);
		else if(Util.areBlockAndTypeEqual(NetworkType.ENERGY, state.getBlock()))
			return NETWORK_MANAGERS.get(NetworkType.ENERGY);
		return null;
	}

	public static PipeNetworkManager getNetworkManagerForType(NetworkType type)
	{
		return NETWORK_MANAGERS.get(type);
	}

	public static void tickManagers(World world)
	{
		for(PipeNetworkManager networkManager : NETWORK_MANAGERS.values())
			networkManager.tick(world);
	}

	public static void purgeForwardingTables()
	{
		for(PipeNetworkManager networkManager : NETWORK_MANAGERS.values())
			networkManager.purgeForwardingTable();
	}

	public static List<IPipeNetwork> getAllNetworksToSave(int dimID, int x, int z)
	{
		List<IPipeNetwork> toReturn = new ArrayList<>();
		for(PipeNetworkManager networkManager : NETWORK_MANAGERS.values())
			toReturn.addAll(networkManager.getNetworksToSave(dimID, x, z));
		return toReturn;
	}

	public int nextID = 0;
	public Map<Integer, IPipeNetwork> networks = new HashMap<Integer, IPipeNetwork>();
	public Map<Integer, Map<Long, Integer>> dimAndPosToNetworkID = new HashMap<>();
	public Map<Integer, Integer> idForwardingTable = new HashMap<>();

	private NetworkType type;

	public PipeNetworkManager(NetworkType type)
	{
		this.type = type;
	}

	public void tick(World world)
	{
//		System.out.println(type.name() + " " + this.networks.size());
		int dimID = world.provider.getDimension();
		List<Integer> deadNetworks = new ArrayList<>();
		for(Entry<Integer, IPipeNetwork> networkEntry : networks.entrySet())
		{
			IPipeNetwork network = networkEntry.getValue();
			if(network.getDimID() == dimID)
			{
				if(network.requiresUpdate())
					network.update(world);

				if(network.isActive())
					network.tick();
				else
					deadNetworks.add(networkEntry.getKey());
			}
		}

		for(Integer i : deadNetworks)
			networks.remove(i);

		deadNetworks.clear();
	}

	public List<IPipeNetwork> getNetworksToSave(int dimID, int x, int z)
	{
		List<IPipeNetwork> toReturn = new ArrayList<>();
		for(IPipeNetwork network : networks.values())
			if(network.getDimID() == dimID && network.isInChunk(x, z))
				toReturn.add(network);
		return toReturn;
	}

	public IPipeNetwork addPipeToNetwork(World world, BlockPos pos)
	{
		int dimId = world.provider.getDimension();
		List<IPipeNetwork> adjacentNetworks = new ArrayList<>();
		for(EnumFacing side : EnumFacing.VALUES)
		{
			BlockPos offset = pos.offset(side);
			IBlockState neighbor = world.getBlockState(offset);
			if(Util.areBlockAndTypeEqual(type, neighbor.getBlock()))
			{
				IPipeNetwork network = getNetwork(offset, dimId);
				if(network != null)
					adjacentNetworks.add(network);
			}
		}

		if(adjacentNetworks.size() == 0)
		{
			IPipeNetwork newNetwork = PipeNetwork.getNewNetwork(type, nextID++, dimId);
			networks.put(newNetwork.getNetworkID(), newNetwork);
			addPosToNetwork(newNetwork, pos);
			return newNetwork;
		}
		else if(adjacentNetworks.size() == 1)
		{
			addPosToNetwork(adjacentNetworks.get(0), pos);
			return adjacentNetworks.get(0);
		}
		else
		{
			IPipeNetwork firstNetwork = adjacentNetworks.get(0);
			for(int i = 1; i < adjacentNetworks.size(); i++)
			{
				IPipeNetwork otherNetwork = adjacentNetworks.get(i);
				if(!firstNetwork.equals(otherNetwork))
					mergeNetworks(firstNetwork, otherNetwork);
			}

			addPosToNetwork(firstNetwork, pos);
			return firstNetwork;
		}
	}

	public void addPosToNetwork(IPipeNetwork network, BlockPos pos)
	{
		network.addBlockPosToNetwork(pos);
		Map<Long, Integer> posToID = dimAndPosToNetworkID.get(network.getDimID());
		if(posToID == null)
		{
			posToID = new HashMap<>();
			dimAndPosToNetworkID.put(network.getDimID(), posToID);
		}
		posToID.put(pos.toLong(), network.getNetworkID());
	}

	public void removePipeFromNetwork(World world, BlockPos pos)
	{
		int dimId = world.provider.getDimension();
		IPipeNetwork network = this.getNetwork(pos, dimId);
		if(network == null)
			return;
		List<BlockPos> adjecentPipes = new ArrayList<BlockPos>();
		for(EnumFacing side : EnumFacing.VALUES)
		{
			BlockPos offset = pos.offset(side);
			IBlockState neighbor = world.getBlockState(offset);
			if(Util.areBlockAndTypeEqual(type, neighbor.getBlock()))
				adjecentPipes.add(offset);
		}

		if(adjecentPipes.size() == 0)
		{
			this.removePosFromNetwork(network, pos);
			deleteNetwork(network);
		}
		else if(adjecentPipes.size() == 1)
		{
			IPipeNetwork adjNetwork = this.getNetwork(adjecentPipes.get(0), dimId);
			if(adjNetwork != null)
				this.removePosFromNetwork(adjNetwork, pos);
		}
		else
		{
			boolean firstTry = true;
			IPipeNetwork origNetwork = this.getNetwork(adjecentPipes.get(0), dimId);
			IPipeNetwork newNetwork = null;
			while(adjecentPipes.size() > 0)
			{
				if(!firstTry)
				{
					newNetwork = PipeNetwork.getNewNetwork(type, nextID++, dimId);
					networks.put(newNetwork.getNetworkID(), newNetwork);
				}
				BlockPos startPos = adjecentPipes.remove(0);
				Map<BlockPos, Float> priorityQueue = new HashMap<>();
				List<BlockPos> visited = new ArrayList<>();
				visited.add(pos);
				priorityQueue.put(startPos, 0f);
				if(!firstTry)
				{
					removePosFromNetwork(origNetwork, startPos);
					addPosToNetwork(newNetwork, startPos);
				}

				while(priorityQueue.size() > 0)
				{
					Entry<BlockPos, Float> nextEntry = null;
					for(Entry<BlockPos, Float> entry : priorityQueue.entrySet())
						if(nextEntry == null || nextEntry.getValue() > entry.getValue())
							nextEntry = entry;

					visited.add(nextEntry.getKey());
					priorityQueue.remove(nextEntry.getKey());

					for(EnumFacing side : EnumFacing.VALUES)
					{
						BlockPos offset = nextEntry.getKey().offset(side);
						if(visited.contains(offset))
							continue;
						else if(adjecentPipes.contains(offset))
						{
							adjecentPipes.remove(offset);
							if(adjecentPipes.size() == 0 && firstTry)
							{
								priorityQueue.clear();
								break;
							}
						}
						IBlockState neighbor = world.getBlockState(offset);
						if(Util.areBlockAndTypeEqual(type, neighbor.getBlock()))
						{
							float distPts = 0f;
							for(BlockPos goalPos : adjecentPipes)
								distPts += goalPos.getDistance(offset.getX(), offset.getY(), offset.getZ());
							priorityQueue.put(offset, distPts / (float) adjecentPipes.size());
							if(!firstTry)
							{
								removePosFromNetwork(origNetwork, offset);
								addPosToNetwork(newNetwork, offset);
							}
						}
					}
				}
				firstTry = false;
			}
			removePosFromNetwork(origNetwork, pos);
		}
	}

	public void removePosFromNetwork(IPipeNetwork network, BlockPos pos)
	{
		network.removeBlockPosFromNetwork(pos);
		Map<Long, Integer> posToID = dimAndPosToNetworkID.get(network.getDimID());
		if(posToID == null)
		{
			posToID = new HashMap<>();
			dimAndPosToNetworkID.put(network.getDimID(), posToID);
		}
		posToID.remove(pos.toLong());
	}

	public void mergeNetworks(IPipeNetwork firstNetwork, IPipeNetwork otherNetwork)
	{
		firstNetwork.mergeWithNetwork(otherNetwork);
		idForwardingTable.put(otherNetwork.getNetworkID(), firstNetwork.getNetworkID());
		deleteNetwork(otherNetwork);
	}

	public IPipeNetwork getOrInitNewNetwork(Integer id, NetworkType type, int dimID)
	{
		if(this.networks.containsKey(id))
			return this.networks.get(id);

		IPipeNetwork network = PipeNetwork.getNewNetwork(type, nextID++, dimID);
		networks.put(network.getNetworkID(), network);
		return network;
	}

	public void deleteNetwork(IPipeNetwork network)
	{
		networks.remove(network.getNetworkID());
		network.deleteNetwork();
	}

	public Integer getNetworkID(BlockPos pos, int dimId)
	{
		Map<Long, Integer> posToID = dimAndPosToNetworkID.get(dimId);
		if(posToID == null)
			return null;

		Integer id = posToID.get(pos.toLong());
		if(id == null)
			return null;

		int originID = id;
		while(idForwardingTable.containsKey(id))
		{
			id = idForwardingTable.get(id);

			//TODO: Possibly throw an error
			if(originID == id)
				break;
		}

		return id;
	}

	public IPipeNetwork getNetwork(BlockPos pos, int dimId)
	{
		Integer id = getNetworkID(pos, dimId);
		if(id == null)
			return null;

		return networks.get(id);
	}

	public NetworkType getType()
	{
		return this.type;
	}

	public void purgeForwardingTable()
	{
		for(Entry<Integer, Map<Long, Integer>> dim : dimAndPosToNetworkID.entrySet())
			for(Entry<Long, Integer> blockpos : dim.getValue().entrySet())
				blockpos.setValue(getNetworkID(BlockPos.fromLong(blockpos.getKey()), dim.getKey()));
		idForwardingTable.clear();
	}

	public enum NetworkType
	{
		ITEM(0), FLUID(1), ENERGY(2);

		private int id;

		NetworkType(int id)
		{
			this.id = id;
		}

		public int getID()
		{
			return this.id;
		}

		public static NetworkType getFromID(int id)
		{
			for(NetworkType type : NetworkType.values())
				if(type.getID() == id)
					return type;
			return ITEM;
		}
	}
}
