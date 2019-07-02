package com.theprogrammingturkey.pipes.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theprogrammingturkey.pipes.capabilities.CapabilityEntityHolder;
import com.theprogrammingturkey.pipes.capabilities.IEntityHolder;
import com.theprogrammingturkey.pipes.network.filtering.FilterStackEntity;
import com.theprogrammingturkey.pipes.network.filtering.InterfaceFilter;
import com.theprogrammingturkey.pipes.network.filtering.InterfaceFilter.DirectionFilter;
import com.theprogrammingturkey.pipes.util.StackInfo;

import net.minecraft.entity.Entity;

public class EntityPipeNetwork extends PipeNetwork<IEntityHolder>
{
	public EntityPipeNetwork(int networkID, int dimID)
	{
		super(networkID, dimID, NetworkType.ENTITY, CapabilityEntityHolder.ENTITY);
	}

	@Override
	public void processTransfers()
	{
		Map<FilterStackEntity, List<StackInfo<IEntityHolder>>> avilable = new HashMap<>();

		//TODO: Should we sort all interfaces? Even ones not configured as both inputs and outputs

		List<InterfaceInfo<IEntityHolder>> sortedInterfaces = new ArrayList<>(interfaces.values());

		Collections.sort(sortedInterfaces, extractPrioritySort);
		for(InterfaceInfo<IEntityHolder> info : sortedInterfaces)
		{
			info.filter.setShowInsertFilter(false);
			if(!info.filter.isEnabled())
				continue;

			List<Entity> ents = info.inv.suckupEntities(new ArrayList<Entity>(), true);

			for(Entity ent : ents)
			{
				FilterStackEntity toCheck = new FilterStackEntity(ent);
				FilterStackEntity entStack = null;
				for(FilterStackEntity fse : avilable.keySet())
					if(fse.isEqual(toCheck))
						entStack = fse;

				List<StackInfo<IEntityHolder>> fsInfo;
				if(entStack == null)
				{
					fsInfo = new ArrayList<StackInfo<IEntityHolder>>();
					avilable.put(new FilterStackEntity(ent), fsInfo);
				}
				else
				{
					fsInfo = avilable.get(entStack);
				}

				fsInfo.add(new StackInfo<IEntityHolder>(info.inv, info.filter, 1));
			}
		}

		Collections.sort(sortedInterfaces, insertPrioritySort);
		for(InterfaceInfo<IEntityHolder> info : sortedInterfaces)
		{
			info.filter.setShowInsertFilter(true);
			if(!info.filter.isEnabled())
				continue;
			for(FilterStackEntity stack : avilable.keySet())
			{
				boolean hasStack = info.filter.hasStackInFilter(stack);
				if((hasStack && info.filter.isWhiteList()) || (!hasStack && !info.filter.isWhiteList()))
				{
					//Inserting into a specific inventory
					int stackInfoIndex = 0;
					List<StackInfo<IEntityHolder>> fromStacks = avilable.get(stack);
					for(int j = stackInfoIndex; j < fromStacks.size(); j++)
					{
						StackInfo<IEntityHolder> stackInfo = fromStacks.get(j);
						if(stackInfo.amountLeft != 0 && !info.inv.equals(stackInfo.inv) && wontSendBack(info.filter, stackInfo.filter, stack))
						{
							boolean worked = info.inv.shootoutEntity(stack.getAsEntity(), false);
							if(worked)
								stackInfo.inv.suckupEntity(stack.getAsEntity(), false);
						}
					}
				}
			}
		}
	}

	private boolean wontSendBack(InterfaceFilter toFilter, InterfaceFilter fromFilter, FilterStackEntity stack)
	{
		DirectionFilter fromOpposite = fromFilter.insertFilter;
		DirectionFilter toOpposite = toFilter.extractFilter;

		if(!fromOpposite.enabled || !toOpposite.enabled)
			return true;

		boolean hasStack = fromOpposite.hasStackInFilter(stack);
		if((hasStack && !fromOpposite.isWhiteList) || (!hasStack && fromOpposite.isWhiteList))
			return true;

		hasStack = toOpposite.hasStackInFilter(stack);
		if((hasStack && !toOpposite.isWhiteList) || (!hasStack && toOpposite.isWhiteList))
			return true;

		if(toOpposite.priority < fromOpposite.priority)
			return true;

		return false;
	}
}
