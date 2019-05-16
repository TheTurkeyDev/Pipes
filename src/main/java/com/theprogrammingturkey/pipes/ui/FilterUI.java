package com.theprogrammingturkey.pipes.ui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import com.theprogrammingturkey.pipes.PipesCore;
import com.theprogrammingturkey.pipes.containers.FilterContainer;
import com.theprogrammingturkey.pipes.network.interfacing.InterfaceFilter;
import com.theprogrammingturkey.pipes.packets.PipesPacketHandler;
import com.theprogrammingturkey.pipes.packets.UpdateFilterPacket;
import com.theprogrammingturkey.pipes.util.FilterStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class FilterUI extends GuiContainer
{
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(PipesCore.MODID + ":textures/gui/filter_ui.png");
	private EntityPlayer player;
	private InterfaceFilter filter;
	private BlockPos pos;

	private GuiCheckBox enabledCheckBox;
	private GuiButton whitelistButton;

	protected TextureMap backgroundMap;

	private int filterX = 47;
	private int filterWidth = 162;
	private int filterY = 50;
	private int filterHeight = 18;

	public FilterUI(EntityPlayer player, InterfaceFilter filter, BlockPos pos)
	{
		super(new FilterContainer(player.inventory, filter, player));
		this.pos = pos;
		this.player = player;
		this.filter = filter;
		this.xSize = 256;
		this.ySize = 177;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui()
	{
		this.mc = Minecraft.getMinecraft();
		super.initGui();
		this.buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.add(new GuiButton(0, guiLeft + xSize, guiTop, 50, 20, "Import"));
		this.buttonList.add(new GuiButton(1, guiLeft + xSize, guiTop + 20, 50, 20, "Export"));
		this.buttonList.add(enabledCheckBox = new GuiCheckBox(2, guiLeft + 10, guiTop + 10, "Enabled", filter.isEnabled()));
		this.buttonList.add(whitelistButton = new GuiButton(3, guiLeft + 70, guiTop + 5, 50, 20, filter.isWhiteList() ? "Whitelist" : "Blacklist"));
		this.buttonList.add(new GuiButton(4, guiLeft + 175, guiTop + 5, 15, 20, "-"));
		this.buttonList.add(new GuiButton(5, guiLeft + 230, guiTop + 5, 15, 20, "+"));
	}

	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTick);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

		int slot = getSlotAt(mouseX, mouseY);
		if(slot != -1)
		{
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int j1 = filterX + this.guiLeft + slot * 18;
			int k1 = filterY + this.guiTop;
			GlStateManager.colorMask(true, true, true, false);
			this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
		}
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawString(this.mc.fontRenderer, "Priority", 191, 4, 0xFFFFFF);
		super.drawCenteredString(this.mc.fontRenderer, String.valueOf(filter.getPriority()), 210, 15, 0xFFFFFF);

		for(int k = 0; k < this.filter.getStacks().size(); k++)
		{
			FilterStack fs = this.filter.getStacks().get(k);
			int i = filterX + k * 18;
			int j = filterY;
			ItemStack itemstack = fs.getAsItemStack();
			String s = null;

			this.zLevel = 100.0F;
			this.itemRender.zLevel = 100.0F;

			GlStateManager.enableDepth();
			this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, itemstack, i, j);
			this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, itemstack, i, j, s);

			this.itemRender.zLevel = 0.0F;
			this.zLevel = 0.0F;
		}
	}

	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int slot = getSlotAt(mouseX, mouseY);
		if(slot != -1)
		{
			ItemStack stack = player.inventory.getItemStack();

			for(int i = this.filter.getStacks().size(); i <= slot; i++)
				this.filter.getStacks().add(new FilterStack(ItemStack.EMPTY));

			if(!stack.isEmpty())
			{
				FilterStack fs = new FilterStack(stack);
				if(!this.filter.hasStackInFilter(fs))
					this.filter.getStacks().set(slot, fs);
			}
			else
			{
				this.filter.getStacks().set(slot, new FilterStack(ItemStack.EMPTY));
			}

			PipesPacketHandler.INSTANCE.sendToServer(new UpdateFilterPacket(pos, filter));
		}
	}

	public int getSlotAt(int x, int y)
	{
		int xLower = guiLeft + filterX;
		int xUpper = xLower + filterWidth;
		int yLower = guiTop + filterY;
		int yUpper = yLower + filterHeight;

		if(x > xLower && x < xUpper && y > yLower && y < yUpper)
			return((x - xLower) / 18);
		return -1;
	}

	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();

	}

	protected void actionPerformed(GuiButton button)
	{
		if(button.enabled)
		{
			boolean flag = false;
			if(button.id == 0)
			{
				filter.setShowInputFilter(true);
				updateButtons();

			}
			else if(button.id == 1)
			{
				filter.setShowInputFilter(false);
				updateButtons();
			}
			else if(button.id == 2)
			{
				filter.setEnabled(((GuiCheckBox) button).isChecked());
				flag = true;
			}
			else if(button.id == 3)
			{
				filter.setWhiteList(!filter.isWhiteList());
				button.displayString = filter.isWhiteList() ? "Whitelist" : "Blacklist";
				flag = true;
			}
			else if(button.id == 4)
			{
				filter.decPriority();
				flag = true;
			}
			else if(button.id == 5)
			{
				filter.incPriority();
				flag = true;
			}

			if(flag)
				PipesPacketHandler.INSTANCE.sendToServer(new UpdateFilterPacket(pos, filter));
		}
	}

	public void updateButtons()
	{
		enabledCheckBox.setIsChecked(filter.isEnabled());
		whitelistButton.displayString = filter.isWhiteList() ? "Whitelist" : "Blacklist";
	}
}