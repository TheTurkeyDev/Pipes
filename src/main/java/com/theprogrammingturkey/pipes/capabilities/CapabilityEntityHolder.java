package com.theprogrammingturkey.pipes.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityEntityHolder
{
	@CapabilityInject(IEntityHolder.class)
	public static Capability<IEntityHolder> ENTITY = null;

	public static void register()
	{
		CapabilityManager.INSTANCE.register(IEntityHolder.class, new IStorage<IEntityHolder>()
		{
			@Override
			public NBTBase writeNBT(Capability<IEntityHolder> capability, IEntityHolder instance, EnumFacing side)
			{
				return new NBTTagCompound();
			}

			@Override
			public void readNBT(Capability<IEntityHolder> capability, IEntityHolder instance, EnumFacing side, NBTBase nbt)
			{

			}
		}, () -> new EntityHolder());
	}
}
