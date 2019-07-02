package com.theprogrammingturkey.pipes.capabilities;

import com.theprogrammingturkey.pipes.RegistryHelper;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityUtil
{
	public static IEntityHolder getEntityBlockHandler(World world, BlockPos pos)
	{
		Block block = world.getBlockState(pos).getBlock();
		if(block == RegistryHelper.ENTITY_PLACER || block == RegistryHelper.ENTITY_VACUUM)
			return new EntityBlockWrapper(world, pos);
		return null;
	}
}
