package com.theprogrammingturkey.pipes.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.theprogrammingturkey.pipes.network.IPipeNetwork;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldListener
{
	public static List<String> loadingChunk = new ArrayList<String>();

	private File dataDir;

	@SubscribeEvent
	public void onChunkLoad(ChunkDataEvent.Load event)
	{
		World world = event.getWorld();
		if(!world.isRemote && world instanceof WorldServer)
		{
			if(dataDir == null) //Delayed down here do that world has time to be initialized first.
			{
				dataDir = new File(((WorldServer) world).getChunkSaveLocation(), "data/pipes");
				dataDir.mkdirs();
			}

			int dimID = world.provider.getDimension();
			int x = event.getChunk().x;
			int z = event.getChunk().z;

			String chunk = x + "." + z;

			if(loadingChunk.contains(chunk))
				return;

			loadingChunk.add(chunk);

			File file = new File(dataDir, "c." + dimID + "." + chunk + ".dat");
			if(file.exists())
			{
				NBTTagCompound nbtdata;
				try
				{
					FileInputStream is = new FileInputStream(file);
					nbtdata = CompressedStreamTools.readCompressed(is);
					is.close();
				} catch(IOException e)
				{
					e.printStackTrace();
					loadingChunk.remove(chunk);
					return;
				}

				if(nbtdata != null)
				{
					for(String key : nbtdata.getKeySet())
					{
						NBTTagCompound networkNBT = nbtdata.getCompoundTag(key);
						int id = networkNBT.getInteger("id");
						NetworkType type = NetworkType.getFromID(networkNBT.getInteger("type"));
						NBTTagCompound pipesNBT = networkNBT.getCompoundTag("pipes");

						PipeNetworkManager manager = PipeNetworkManager.getNetworkManagerForType(type);
						IPipeNetwork network = manager.getOrInitNewNetwork(id, type, dimID);
						network.loadNetworkInChunk(event.getWorld(), x, z, pipesNBT);
					}
				}
			}

			loadingChunk.remove(chunk);
		}
	}

	@SubscribeEvent
	public void onChunkSave(ChunkDataEvent.Save event)
	{
		World world = event.getWorld();
		if(!world.isRemote && world instanceof WorldServer)
		{
			if(dataDir == null) //Delayed down here do that world has time to be initialized first.
			{
				dataDir = new File(((WorldServer) world).getChunkSaveLocation(), "data/pipes");
				dataDir.mkdirs();
			}

			int dimID = world.provider.getDimension();

			List<IPipeNetwork> networksToSave = PipeNetworkManager.getAllNetworksToSave(dimID, event.getChunk().x, event.getChunk().z);
			if(networksToSave.isEmpty())
				return;

			NBTTagCompound nbtdata = new NBTTagCompound();

			for(IPipeNetwork network : networksToSave)
			{
				NBTTagCompound networkNBT = new NBTTagCompound();
				networkNBT.setInteger("id", network.getNetworkID());
				networkNBT.setInteger("type", network.getNetworkType().getID());
				networkNBT.setTag("pipes", network.saveNetworkInchunk(event.getChunk().x, event.getChunk().z));
				nbtdata.setTag(network.getNetworkType().getID() + "-" + network.getNetworkID(), networkNBT);
			}

			File file = new File(dataDir, "c." + dimID + "." + event.getChunk().x + "." + event.getChunk().z + ".dat");
			if(!file.exists())
			{
				try
				{
					file.createNewFile();
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}

			try
			{
				FileOutputStream os = new FileOutputStream(file);
				CompressedStreamTools.writeCompressed(nbtdata, os);
				os.close();
			} catch(IOException e)
			{
				e.printStackTrace();
				return;
			}
		}
	}
}
