package com.theprogrammingturkey.pipes.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.theprogrammingturkey.pipes.RegistryHelper;

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

	public static List<IPipeNetwork> getNetworksAtPos(BlockPos pos, int dimID)
	{
		List<IPipeNetwork> networks = new ArrayList<>();
		for(PipeNetworkManager networkManager : NETWORK_MANAGERS.values())
			networks.add(networkManager.getNetwork(pos, dimID));
		return networks;
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

	public static List<IPipeNetwork> getAllNetworksToSave(int dimID, int x, int z)
	{
		List<IPipeNetwork> toReturn = new ArrayList<>();
		for(PipeNetworkManager networkManager : NETWORK_MANAGERS.values())
			toReturn.addAll(networkManager.getNetworksToSave(dimID, x, z));
		return toReturn;
	}

	public int nextID = 0;
	public Map<Integer, IPipeNetwork> networks = new HashMap<>();
	public Map<Integer, IPipeNetwork> networksToAdd = new HashMap<>();

	private NetworkType type;

	public PipeNetworkManager(NetworkType type)
	{
		this.type = type;
	}

	public void tick(World world)
	{
		//System.out.println(type.name() + " " + this.networks.size());
		int dimID = world.provider.getDimension();
		List<Integer> deadNetworks = new ArrayList<>();
		for(Entry<Integer, IPipeNetwork> networkEntry : networks.entrySet())
		{
			IPipeNetwork network = networkEntry.getValue();

			if(network.getDimID() == dimID)
			{
				if(network.requiresUpdate())
					network.update(world);
				if(network.requiresPassiveUpdate())
					network.passiveUpdate(world);

				if(network.isActive())
					network.tick();
				else
					deadNetworks.add(networkEntry.getKey());
			}
		}

		for(Integer i : deadNetworks)
			networks.remove(i);
		deadNetworks.clear();

		for(IPipeNetwork network : networksToAdd.values())
			networks.put(network.getNetworkID(), network);
		networksToAdd.clear();

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
			if(type.areBlockAndTypeEqual(neighbor))
			{
				IPipeNetwork network = getNetwork(offset, dimId);
				if(network != null)
					adjacentNetworks.add(network);
			}
		}

		if(adjacentNetworks.size() == 0)
		{
			IPipeNetwork newNetwork = PipeNetwork.getNewNetwork(type, this.getNextNetworkID(), dimId);
			networksToAdd.put(newNetwork.getNetworkID(), newNetwork);
			newNetwork.addBlockPosToNetwork(pos);
			return newNetwork;
		}
		else if(adjacentNetworks.size() == 1)
		{
			adjacentNetworks.get(0).addBlockPosToNetwork(pos);
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

			firstNetwork.addBlockPosToNetwork(pos);
			return firstNetwork;
		}
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
			if(type.areBlockAndTypeEqual(neighbor) && network.isPosInNetwork(offset))
				adjecentPipes.add(offset);
		}

		if(adjecentPipes.size() == 0)
		{
			network.removeBlockPosFromNetwork(pos);
			deleteNetwork(network);
		}
		else if(adjecentPipes.size() == 1)
		{
			network.removeBlockPosFromNetwork(pos);
		}
		else
		{
			boolean reformedNetwork = false;
			Map<BlockPos, Float> priorityQueue = new HashMap<>();
			List<BlockPos> visited = new ArrayList<>();
			visited.add(pos);
			priorityQueue.put(adjecentPipes.remove(0), 0f);

			/*
			 * First try to reform the network by reconnecting each of the adjacent pipes from the
			 * pipe that is being removed.
			 */
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
						if(adjecentPipes.size() == 0)
						{
							priorityQueue.clear();
							reformedNetwork = true;
							break;
						}
					}
					IBlockState neighbor = world.getBlockState(offset);
					if(type.areBlockAndTypeEqual(neighbor))
					{
						float distPts = 0f;
						for(BlockPos goalPos : adjecentPipes)
							distPts += goalPos.getDistance(offset.getX(), offset.getY(), offset.getZ());
						priorityQueue.put(offset, distPts / (float) adjecentPipes.size());
					}
				}
			}

			if(!reformedNetwork)
			{
				/*
				 * If we get here, the network could not be fully reformed so we create new networks
				 * for the adjacent pipes that could not be reached and removed all pipes that they
				 * connect to from the original network.
				 */
				List<BlockPos> toVisit = new ArrayList<>();
				IPipeNetwork newNetwork = null;
				while(adjecentPipes.size() > 0)
				{
					visited.clear();
					visited.add(pos);
					newNetwork = PipeNetwork.getNewNetwork(type, this.getNextNetworkID(), dimId);
					networksToAdd.put(newNetwork.getNetworkID(), newNetwork);
					toVisit.add(adjecentPipes.remove(0));
					while(!toVisit.isEmpty())
					{
						BlockPos nextPos = toVisit.remove(0);
						visited.add(nextPos);
						network.removeBlockPosFromNetwork(nextPos);
						newNetwork.addBlockPosToNetwork(nextPos);
						for(EnumFacing side : EnumFacing.VALUES)
						{
							BlockPos offset = nextPos.offset(side);
							if(adjecentPipes.contains(offset))
								adjecentPipes.remove(offset);
							else if(!visited.contains(offset))
								if(type.areBlockAndTypeEqual(world.getBlockState(offset)))
									toVisit.add(offset);
						}
					}
				}
			}

			network.removeBlockPosFromNetwork(pos);
		}
	}

	public void mergeNetworks(IPipeNetwork firstNetwork, IPipeNetwork otherNetwork)
	{
		firstNetwork.mergeWithNetwork(otherNetwork);
		deleteNetwork(otherNetwork);
	}

	public IPipeNetwork getOrInitNewNetwork(Integer id, NetworkType type, int dimID)
	{
		if(this.networks.containsKey(id))
			return this.networks.get(id);

		if(this.nextID <= id)
			this.nextID = id + 1;

		IPipeNetwork network = PipeNetwork.getNewNetwork(type, id, dimID);
		networksToAdd.put(network.getNetworkID(), network);
		return network;
	}

	public void deleteNetwork(IPipeNetwork network)
	{
		network.deleteNetwork();
	}

	public Integer getNetworkID(BlockPos pos, int dimId)
	{
		for(IPipeNetwork network : this.networks.values())
			if(network.getDimID() == dimId && network.isPosInNetwork(pos))
				return network.getNetworkID();

		for(IPipeNetwork network : this.networksToAdd.values())
			if(network.getDimID() == dimId && network.isPosInNetwork(pos))
				return network.getNetworkID();

		return null;
	}

	public IPipeNetwork getNetwork(BlockPos pos, World world)
	{
		return getNetwork(pos, world.provider.getDimension());
	}

	public IPipeNetwork getNetwork(BlockPos pos, int dimId)
	{
		Integer id = getNetworkID(pos, dimId);
		if(id == null)
			return null;

		return networks.get(id);
	}

	public int getNextNetworkID()
	{
		//TODO: Plan a more elegant way to clean up networkID's
		while(this.networks.containsKey(this.nextID) || this.networksToAdd.containsKey(this.nextID))
			this.nextID++;
		return this.nextID;
	}

	public NetworkType getType()
	{
		return this.type;
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

		public boolean areBlockAndTypeEqual(IBlockState state)
		{
			if(this == NetworkType.ITEM)
				return state.getBlock().equals(RegistryHelper.ITEM_PIPE);
			else if(this == NetworkType.FLUID)
				return state.getBlock().equals(RegistryHelper.FLUID_PIPE) || state.getBlock().equals(RegistryHelper.FLUID_PUMP);
			else if(this == NetworkType.ENERGY)
				return state.getBlock().equals(RegistryHelper.ENERGY_PIPE);
			return false;
		}
	}
}
