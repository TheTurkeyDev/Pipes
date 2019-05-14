package com.theprogrammingturkey.pipes.ui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.theprogrammingturkey.pipes.PipesCore;
import com.theprogrammingturkey.pipes.network.interfacing.InterfaceFilter;
import com.theprogrammingturkey.pipes.network.interfacing.InterfaceFilter.DirectionFilter;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo.Color;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class FilterUI extends GuiScreen
{
	private static final ResourceLocation guiTextures = new ResourceLocation(PipesCore.MODID + ":textures/gui/filter_ui.png");
	private EntityPlayer player;
	private InterfaceFilter filter;
	private int imageWidth = 256;
	private int imageHeight = 155;
	private DirectionFilter currentDirFilter;

	private GuiCheckBox enabledCheckBox;
	private GuiButton whitelistButton;

	public FilterUI(EntityPlayer player, InterfaceFilter filter)
	{
		this.player = player;
		this.filter = filter;
		currentDirFilter = filter.inputFilter;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui()
	{
		System.out.println(this.width + " x " + this.height);
		this.buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.buttonList.add(new GuiButton(0, i + imageWidth, j, 50, 20, "Import"));
		this.buttonList.add(new GuiButton(1, i + imageWidth, j + 20, 50, 20, "Export"));
		this.buttonList.add(enabledCheckBox = new GuiCheckBox(2, i + 10, j + 10, "Enabled", currentDirFilter.enabled));
		this.buttonList.add(whitelistButton = new GuiButton(3, i + 70, j + 5, 50, 20, currentDirFilter.isWhiteList ? "Whitelist" : "Blacklist"));
		this.buttonList.add(new GuiButton(4, i + 175, j + 5, 15, 20, "-"));
		this.buttonList.add(new GuiButton(5, i + 230, j + 5, 15, 20, "+"));
	}

	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	protected void actionPerformed(GuiButton button)
	{
		if(button.enabled)
		{
			if(button.id == 0)
			{
				currentDirFilter = filter.inputFilter;
				updateButtons();
			}
			else if(button.id == 1)
			{
				currentDirFilter = filter.outputFilter;
				updateButtons();
			}
			else if(button.id == 2)
			{
				currentDirFilter.enabled = ((GuiCheckBox) button).isChecked();
			}
			else if(button.id == 3)
			{
				currentDirFilter.isWhiteList = !filter.inputFilter.isWhiteList;
				button.displayString = filter.inputFilter.isWhiteList ? "Whitelist" : "Blacklist";
			}
			else if(button.id == 4)
			{
				currentDirFilter.priority += 1;
			}
			else if(button.id == 5)
			{
				currentDirFilter.priority -= 1;
			}
		}
	}

	public void updateButtons()
	{
		enabledCheckBox.enabled = currentDirFilter.enabled;
		whitelistButton.displayString = filter.inputFilter.isWhiteList ? "Whitelist" : "Blacklist";
	}

	//	protected void keyTyped(char p_73869_1_, int p_73869_2_) throws IOException
	//	{
	//		if(!this.rewardField.textboxKeyTyped(p_73869_1_, p_73869_2_))
	//			super.keyTyped(p_73869_1_, p_73869_2_);
	//	}

	//	protected void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_) throws IOException
	//	{
	//		super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
	//		this.rewardField.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
	//	}

	public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
	{
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		super.drawDefaultBackground();
		this.mc.getTextureManager().bindTexture(guiTextures);
		this.drawTexturedModalRect(i, j, 0, 0, this.imageWidth, this.imageHeight);
		super.drawString(this.mc.fontRenderer, "Priority", i + 191, j + 4, 0xFFFFFF);
		super.drawCenteredString(this.mc.fontRenderer, String.valueOf(currentDirFilter.priority), i + 210, j + 15, 0xFFFFFF);
		super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
	}
}
