package cuchaz.ships.gui;

import static cuchaz.ships.gui.GuiSettings.LeftMargin;
import static cuchaz.ships.gui.GuiSettings.LineSpacing;
import static cuchaz.ships.gui.GuiSettings.TopMargin;
import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.inventory.Container;

import org.lwjgl.opengl.GL11;

public abstract class GuiShip extends GuiCloseable
{
	private static final ResourceLocation BackgroundTexture = new ResourceLocation( "ships", "/textures/gui/ship.png" );
	
	public GuiShip( Container container )
	{
		super( container );
		
		xSize = 176;
		ySize = 166;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer( float renderPartialTicks, int mouseX, int mouseY )
	{
		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
		
		// this call loads the texture. The deobfuscation mappings haven't picked this one up yet in 1.6.1
		this.mc.func_110434_K().func_110577_a( BackgroundTexture );
		
		drawTexturedModalRect( guiLeft, guiTop, 0, 0, xSize, ySize );
	}
	
	protected void drawText( String text, int lineNum, int textColor )
	{
		fontRenderer.drawString( text, LeftMargin, getLineY( lineNum ), textColor );
	}
	
	protected String getYesNoText( boolean flag )
	{
		return flag ? GuiString.Yes.getLocalizedText() : GuiString.No.getLocalizedText();
	}
	
	protected int getLineY( int lineNum )
	{
		return TopMargin + ( fontRenderer.FONT_HEIGHT + LineSpacing )*lineNum;
	}
}
