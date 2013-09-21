package cuchaz.ships.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cuchaz.ships.ShipWorld;
import cuchaz.ships.TileEntityHelm;

@SideOnly( Side.CLIENT )
public class TileEntityHelmRenderer extends TileEntitySpecialRenderer
{
	private static final ResourceLocation Texture = new ResourceLocation( "ships", "/textures/models/helm.png" );
	
	private ModelHelm m_model;
	
	public TileEntityHelmRenderer( )
	{
		m_model = new ModelHelm();
	}
	
	@Override
	public void renderTileEntityAt( TileEntity tileEntity, double x, double y, double z, float partialTickTime )
	{
		renderTileEntityAt( (TileEntityHelm)tileEntity, x, y, z, partialTickTime );
	}
	
	public void renderTileEntityAt( TileEntityHelm tileEntity, double x, double y, double z, float partialTickTime )
	{
		RenderManager.instance.renderEngine.func_110577_a( Texture );
		
		// get the rotation angle from the block
		int rotation = tileEntity.worldObj.getBlockMetadata( tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord );
		float angle = rotation*360.f/4.0f;
		
		if( tileEntity.worldObj instanceof ShipWorld )
		{
			// set the rotation of the ship wheel
			ShipWorld world = (ShipWorld)tileEntity.worldObj;
			m_model.setWheelAngle( -world.getShip().motionYaw * 20 );
		}
		
		GL11.glPushMatrix();
        GL11.glTranslatef( (float)x + 0.5f, (float)y + 1.5f, (float)z + 0.5f );
        GL11.glScalef( 1.0f, -1.0f, -1.0f );
		GL11.glRotatef( angle, 0.0f, 1.0f, 0.0f );
		m_model.renderAll();
		GL11.glPopMatrix();
	}
}
