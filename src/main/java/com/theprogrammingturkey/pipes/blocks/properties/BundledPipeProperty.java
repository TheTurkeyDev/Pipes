package com.theprogrammingturkey.pipes.blocks.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraft.block.properties.PropertyHelper;

public class BundledPipeProperty extends PropertyHelper<PipeBundle>
{
	private static final ImmutableSet<PipeBundle> allowedValues;

	static
	{
		List<PipeBundle> bundles = new ArrayList<PipeBundle>();

		for(EnumAttachType item : EnumAttachType.values())
		{
			for(EnumAttachType fluid : EnumAttachType.values())
			{
				for(EnumAttachType energy : EnumAttachType.values())
				{
					PipeBundle bundle = new PipeBundle();
					bundle.setAttachType(NetworkType.ENERGY, energy);
					bundle.setAttachType(NetworkType.FLUID, fluid);
					bundle.setAttachType(NetworkType.ITEM, item);
					bundles.add(bundle);
				}
			}
		}
		allowedValues = ImmutableSet.copyOf(bundles);
	}

	public BundledPipeProperty(String name)
	{
		super(name, PipeBundle.class);
	}

	@Override
	public Collection<PipeBundle> getAllowedValues()
	{
		return allowedValues;
	}

	@Override
	public Optional<PipeBundle> parseValue(String value)
	{
		PipeBundle bundle = new PipeBundle();
		String[] parts = value.split("_");
		//Guarantees that this will work and throw out the last part if the size is somehow odd
		for(int i = 0; i < parts.length - 1; i += 2)
			bundle.setAttachType(NetworkType.getFromID(Integer.parseInt(parts[i])), EnumAttachType.valueOf(parts[i + 1].toUpperCase()));
		return Optional.of(bundle);
	}

	@Override
	public String getName(PipeBundle value)
	{
		return value.toString();
	}

}
