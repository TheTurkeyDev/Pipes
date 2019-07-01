package com.theprogrammingturkey.pipes;

import com.theprogrammingturkey.pipes.blocks.BasePipeBlock;
import com.theprogrammingturkey.pipes.blocks.EnergyPipeBlock;
import com.theprogrammingturkey.pipes.blocks.EntityPipeBlock;
import com.theprogrammingturkey.pipes.blocks.FluidPipeBlock;
import com.theprogrammingturkey.pipes.blocks.FluidPumpBlock;
import com.theprogrammingturkey.pipes.blocks.ItemPipeBlock;
import com.theprogrammingturkey.pipes.items.PipeItemBlock;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RegistryHelper
{
	public static BasePipeBlock ITEM_PIPE;
	public static BasePipeBlock FLUID_PIPE;
	public static FluidPumpBlock FLUID_PUMP;
	public static BasePipeBlock ENERGY_PIPE;
	public static BasePipeBlock ENTITY_PIPE;

	@SubscribeEvent
	public void onBlockRegistry(RegistryEvent.Register<Block> e)
	{
		e.getRegistry().register(ITEM_PIPE = new ItemPipeBlock());
		e.getRegistry().register(FLUID_PIPE = new FluidPipeBlock());
		e.getRegistry().register(FLUID_PUMP = new FluidPumpBlock());
		e.getRegistry().register(ENERGY_PIPE = new EnergyPipeBlock());
		e.getRegistry().register(ENTITY_PIPE = new EntityPipeBlock());
	}

	@SubscribeEvent
	public void onItemRegistry(RegistryEvent.Register<Item> e)
	{
		e.getRegistry().register(new PipeItemBlock(ITEM_PIPE).setRegistryName(ITEM_PIPE.getRegistryName()));
		e.getRegistry().register(new PipeItemBlock(FLUID_PIPE).setRegistryName(FLUID_PIPE.getRegistryName()));
		e.getRegistry().register(new PipeItemBlock(FLUID_PUMP).setRegistryName(FLUID_PUMP.getRegistryName()));
		e.getRegistry().register(new PipeItemBlock(ENERGY_PIPE).setRegistryName(ENERGY_PIPE.getRegistryName()));
		e.getRegistry().register(new PipeItemBlock(ENTITY_PIPE).setRegistryName(ENTITY_PIPE.getRegistryName()));
	}

	public static void registerItemsModels()
	{
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		mesher.register(Item.getItemFromBlock(ITEM_PIPE), 0, new ModelResourceLocation(PipesCore.MODID + ":" + ITEM_PIPE.getBlockName(), "inventory"));
		mesher.register(Item.getItemFromBlock(FLUID_PIPE), 0, new ModelResourceLocation(PipesCore.MODID + ":" + FLUID_PIPE.getBlockName(), "inventory"));
		mesher.register(Item.getItemFromBlock(FLUID_PUMP), 0, new ModelResourceLocation(PipesCore.MODID + ":" + FLUID_PUMP.getBlockName(), "inventory"));
		mesher.register(Item.getItemFromBlock(ENERGY_PIPE), 0, new ModelResourceLocation(PipesCore.MODID + ":" + ENERGY_PIPE.getBlockName(), "inventory"));
		mesher.register(Item.getItemFromBlock(ENTITY_PIPE), 0, new ModelResourceLocation(PipesCore.MODID + ":" + ENTITY_PIPE.getBlockName(), "inventory"));
	}
}
