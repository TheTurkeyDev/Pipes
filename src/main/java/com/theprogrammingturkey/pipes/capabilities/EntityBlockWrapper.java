package com.theprogrammingturkey.pipes.capabilities;

import java.util.ArrayList;
import java.util.List;

import com.theprogrammingturkey.pipes.RegistryHelper;
import com.theprogrammingturkey.pipes.blocks.EntityPlacerBlock;
import com.theprogrammingturkey.pipes.blocks.EntityVacuumBlock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityBlockWrapper implements IEntityHolder
{
	protected final World world;
	protected final BlockPos blockPos;
	private static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0, 0, 0, 1, 1, 1);

	public EntityBlockWrapper(World world, BlockPos blockPos)
	{
		this.world = world;
		this.blockPos = blockPos;
	}

	@Override
	public List<Entity> suckupEntities(List<Entity> blacklist, boolean simulate)
	{
		IBlockState state = world.getBlockState(blockPos);
		List<Entity> toReturn = new ArrayList<Entity>();
		if(state.getBlock() == RegistryHelper.ENTITY_VACUUM)
		{
			EnumFacing facing = state.getValue(EntityVacuumBlock.PLACER_FACING);
			BlockPos over = blockPos.offset(facing);
			AxisAlignedBB newAABB = BLOCK_AABB.offset(over);
			System.out.println(newAABB);
			toReturn = world.getEntitiesWithinAABBExcludingEntity(null, newAABB);
			if(!simulate)
				for(Entity ent : toReturn)
					ent.setDead();
		}
		return toReturn;
	}

	@Override
	public boolean suckupEntity(Entity ent, boolean simulate)
	{
		IBlockState state = world.getBlockState(blockPos);
		if(state.getBlock() == RegistryHelper.ENTITY_VACUUM)
		{
			EnumFacing facing = state.getValue(EntityVacuumBlock.PLACER_FACING);
			BlockPos over = blockPos.offset(facing);
			List<Entity> ents = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(over, over.add(1, 1, 1)));
			if(!simulate)
			{
				for(Entity entGrabbed : ents)
				{
					if(entGrabbed.equals(ent))
					{
						if(!simulate)
							ent.setDead();
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<Entity> shootoutEntities(List<Entity> ents, boolean simulate)
	{
		IBlockState state = world.getBlockState(blockPos);
		if(state.getBlock() == RegistryHelper.ENTITY_PLACER)
		{
			EnumFacing facing = state.getValue(EntityPlacerBlock.PLACER_FACING);
			BlockPos over = blockPos.offset(facing);
			if(!simulate)
			{
				for(Entity ent : ents)
				{
					Entity entity = EntityList.newEntity(ent.getClass(), world);

					if(entity != null)
					{
						entity.readFromNBT(ent.writeToNBT(new NBTTagCompound()));
						entity.moveToBlockPosAndAngles(over, entity.rotationYaw, entity.rotationPitch);

						boolean flag = entity.forceSpawn;
						entity.forceSpawn = true;
						world.spawnEntity(entity);
						entity.forceSpawn = flag;
						world.updateEntityWithOptionalForce(entity, false);
					}
				}
			}
		}
		return new ArrayList<Entity>();
	}

	@Override
	public boolean shootoutEntity(Entity ent, boolean simulate)
	{
		IBlockState state = world.getBlockState(blockPos);
		if(state.getBlock() == RegistryHelper.ENTITY_PLACER)
		{
			EnumFacing facing = state.getValue(EntityPlacerBlock.PLACER_FACING);
			BlockPos over = blockPos.offset(facing);
			if(!simulate)
			{
				Entity entity = EntityList.newEntity(ent.getClass(), world);

				if(entity != null)
				{
					entity.readFromNBT(ent.writeToNBT(new NBTTagCompound()));
					entity.moveToBlockPosAndAngles(over, entity.rotationYaw, entity.rotationPitch);

					boolean flag = entity.forceSpawn;
					entity.forceSpawn = true;
					world.spawnEntity(entity);
					entity.forceSpawn = flag;
					world.updateEntityWithOptionalForce(entity, false);
				}
			}
		}
		return true;
	}
}
