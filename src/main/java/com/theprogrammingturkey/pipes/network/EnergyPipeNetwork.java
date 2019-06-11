package com.theprogrammingturkey.pipes.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.theprogrammingturkey.pipes.network.InterfaceFilter.DirectionFilter;
import com.theprogrammingturkey.pipes.network.PipeNetworkManager.NetworkType;

import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyPipeNetwork extends PipeNetwork<IEnergyStorage>
{

	public EnergyPipeNetwork(int networkID, int dimID)
	{
		super(networkID, dimID, NetworkType.ENERGY, CapabilityEnergy.ENERGY);
	}

	@Override
	public void processTransfers()
	{
		List<StackInfo> avilable = new ArrayList<>();

		//TODO: Should we sort all interfaces? Even ones not configured as both inputs and outputs

		List<InterfaceInfo<IEnergyStorage>> sortedInterfaces = new ArrayList<>(interfaces.values());

		Collections.sort(sortedInterfaces, extractPrioritySort);
		for(InterfaceInfo<IEnergyStorage> info : sortedInterfaces)
		{
			info.filter.setShowInsertFilter(false);
			if(!info.filter.isEnabled())
				continue;

			//Find a better way than Integer.MAX_VALUE
			int energy = info.inv.extractEnergy(Integer.MAX_VALUE, true);

			if(energy != 0)
				avilable.add(new StackInfo(info.inv, info.filter, energy));
		}

		Collections.sort(sortedInterfaces, insertPrioritySort);
		for(InterfaceInfo<IEnergyStorage> info : sortedInterfaces)
		{
			info.filter.setShowInsertFilter(true);
			if(!info.filter.isEnabled())
				continue;

			//Inserting into a specific inventory
			int stackInfoIndex = 0;
			int toInsert = 0;
			for(int j = stackInfoIndex; j < avilable.size(); j++)
			{
				StackInfo stackInfo = avilable.get(j);
				if(stackInfo.amountLeft != 0 && !info.inv.equals(stackInfo.inv) && wontSendBack(info.filter, stackInfo.filter, info.inv, stackInfo.inv))
				{
					toInsert = stackInfo.amountLeft;

					int amonutUsed = info.inv.receiveEnergy(toInsert, false);
					if(amonutUsed != 0)
					{
						stackInfo.amountLeft -= amonutUsed;
						if(stackInfo.amountLeft == 0)
							stackInfo.inv.extractEnergy(stackInfo.amount, false);
					}
				}
			}
		}
	}

	private boolean wontSendBack(InterfaceFilter toFilter, InterfaceFilter fromFilter, IEnergyStorage to, IEnergyStorage from)
	{
		DirectionFilter fromOpposite = fromFilter.insertFilter;
		DirectionFilter toOpposite = toFilter.extractFilter;

		if(!fromOpposite.enabled || !toOpposite.enabled)
			return true;

		if(!to.canExtract() || !from.canReceive())
			return true;

		if(toOpposite.priority < fromOpposite.priority)
			return true;

		return false;
	}

	private static class StackInfo
	{
		public IEnergyStorage inv;
		public int amount;
		public int amountLeft;
		public InterfaceFilter filter;

		public StackInfo(IEnergyStorage inv, InterfaceFilter filter, int amount)
		{
			this.inv = inv;
			this.filter = filter;
			this.amount = amount;
			this.amountLeft = amount;
		}
	}
}
