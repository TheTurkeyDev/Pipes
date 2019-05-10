package com.theprogrammingturkey.pipes.util;

import com.theprogrammingturkey.pipes.PipesCore;
import com.theprogrammingturkey.pipes.blocks.BasePipeBlock;
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

	@SubscribeEvent
	public void onBlockRegistry(RegistryEvent.Register<Block> e)
	{
		e.getRegistry().register(ITEM_PIPE = new ItemPipeBlock());
		e.getRegistry().register(FLUID_PIPE = new ItemPipeBlock());
	}

	@SubscribeEvent
	public void onItemRegistry(RegistryEvent.Register<Item> e)
	{
		e.getRegistry().register(new PipeItemBlock(ITEM_PIPE).setRegistryName(ITEM_PIPE.getRegistryName()));
		e.getRegistry().register(new PipeItemBlock(FLUID_PIPE).setRegistryName(FLUID_PIPE.getRegistryName()));
	}

	public static void registerItemsModels()
	{
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		mesher.register(Item.getItemFromBlock(ITEM_PIPE), 0, new ModelResourceLocation(PipesCore.MODID + ":" + ITEM_PIPE.getBlockName(), "inventory"));
		mesher.register(Item.getItemFromBlock(FLUID_PIPE), 0, new ModelResourceLocation(PipesCore.MODID + ":" + FLUID_PIPE.getBlockName(), "inventory"));
	}
}
