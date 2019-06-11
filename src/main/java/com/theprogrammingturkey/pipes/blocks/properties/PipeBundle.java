package com.theprogrammingturkey.pipes.blocks.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

public class PipeBundle implements Comparable<PipeBundle>
{
	private Map<NetworkType, EnumAttachType> pipes = new HashMap<>();

	public PipeBundle()
	{
		for(NetworkType type : NetworkType.values())
			pipes.put(type, EnumAttachType.NONE);
	}

	public void setAttachType(NetworkType netType, EnumAttachType attachType)
	{
		pipes.replace(netType, attachType);
	}

	public EnumAttachType getAttachTypeForNetwork(NetworkType netType)
	{
		return pipes.get(netType);
	}

	@Override
	public String toString()
	{
		System.out.println(pipes.size());
		StringBuilder builder = new StringBuilder();

		for(Entry<NetworkType, EnumAttachType> entry : pipes.entrySet())
		{
			builder.append(entry.getKey().getID());
			builder.append("_");
			builder.append(entry.getValue().name().toLowerCase());
			builder.append("_");
		}
		return builder.deleteCharAt(builder.length() - 1).toString();
	}

	@Override
	public int compareTo(PipeBundle bundle)
	{
		for(NetworkType type : NetworkType.values())
		{
			EnumAttachType atThis = this.getAttachTypeForNetwork(type);
			EnumAttachType atBundle = bundle.getAttachTypeForNetwork(type);
			if(atThis != atBundle)
				return atThis.compareTo(atBundle);
		}
		return 0;
	}
}
