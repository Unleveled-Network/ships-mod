/*******************************************************************************
 * Copyright (c) 2013 jeff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     jeff - initial API and implementation
 ******************************************************************************/
package cuchaz.ships;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import cuchaz.modsShared.BoundingBoxInt;

public class BlocksStorage
{
	private static final String Encoding = "UTF-8";
	private static final ChunkCoordinates Origin = new ChunkCoordinates( 0, 0, 0 );
	
	private TreeMap<ChunkCoordinates,BlockStorage> m_blocks;
	private final BlockStorage m_airBlockStorage;
	private ShipGeometry m_geometry;
	
	public BlocksStorage( )
	{
		m_blocks = new TreeMap<ChunkCoordinates,BlockStorage>();
		m_airBlockStorage = new BlockStorage();
		m_geometry = null;
	}
	
	public void clear( )
	{
		m_blocks.clear();
		m_geometry = null;
	}
	
	public void readFromWorld( World world, ChunkCoordinates originCoords, List<ChunkCoordinates> blocks )
	{
		clear();
		
		// copy the blocks into storage
		for( ChunkCoordinates worldCoords : blocks )
		{
			BlockStorage storage = new BlockStorage();
			storage.readFromWorld( world, worldCoords );
			
			// make all the blocks relative to the origin block
			ChunkCoordinates relativeCoords = new ChunkCoordinates( worldCoords.posX - originCoords.posX, worldCoords.posY - originCoords.posY, worldCoords.posZ - originCoords.posZ );
			m_blocks.put( relativeCoords, storage );
		}
	}
	
	public void writeToWorld( World world, Map<ChunkCoordinates,ChunkCoordinates> correspondence )
	{
		// copy the blocks to the world
		for( Map.Entry<ChunkCoordinates,BlockStorage> entry : m_blocks.entrySet() )
		{
			ChunkCoordinates coordsShip = entry.getKey();
			ChunkCoordinates coordsWorld = correspondence.get( coordsShip );
			BlockStorage storage = entry.getValue();
			storage.writeToWorld( world, coordsWorld );
		}
	}

	public void readFromStream( DataInputStream in )
	throws IOException
	{
		clear();
		
		int numBlocks = in.readInt();
		for( int i = 0; i < numBlocks; i++ )
		{
			ChunkCoordinates coords = new ChunkCoordinates( in.readInt(), in.readInt(), in.readInt() );
			
			BlockStorage storage = new BlockStorage();
			storage.readFromStream( in );
			
			m_blocks.put( coords, storage );
		}
	}
	
	public void writeToStream( DataOutputStream out )
	throws IOException
	{
		out.writeInt( m_blocks.size() );
		for( Map.Entry<ChunkCoordinates, BlockStorage> entry : m_blocks.entrySet() )
		{
			ChunkCoordinates coords = entry.getKey();
			BlockStorage storage = entry.getValue();
			
			out.writeInt( coords.posX );
			out.writeInt( coords.posY );
			out.writeInt( coords.posZ );
			storage.writeToStream( out );
		}
	}
	
	public void readFromString( String data )
	throws IOException
	{
		clear();
		
		// STREAM MADNESS!!! @_@  MADNESS, I TELL YOU!!
		DataInputStream in = new DataInputStream( new GZIPInputStream( new Base64InputStream( new ByteArrayInputStream( data.getBytes( Encoding ) ) ) ) );
		readFromStream( in );
		in.close();
	}
	
	public String writeToString( )
	throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream( new GZIPOutputStream( new Base64OutputStream( buffer ) ) );
		writeToStream( out );
		out.close();
		return new String( buffer.toByteArray(), Encoding );
	}
	
	public String dumpBlocks( )
	{
		StringBuilder buf = new StringBuilder();
		for( Map.Entry<ChunkCoordinates,BlockStorage> entry : m_blocks.entrySet() )
		{
			ChunkCoordinates coords = entry.getKey();
			BlockStorage storage = entry.getValue();
			
			buf.append( String.format( "%3d,%3d,%3d %4d %4d\n", coords.posX, coords.posY, coords.posZ, storage.id, storage.meta ) );
		}
		return buf.toString();
	}
	
	public ShipGeometry getGeometry( )
	{
		if( m_geometry == null )
		{
			m_geometry = new ShipGeometry( m_blocks.keySet() );
		}
		return m_geometry;
	}
	
	public int getNumBlocks( )
	{
		return m_blocks.size();
	}
	
	public Set<ChunkCoordinates> coords( )
	{
		return m_blocks.keySet();
	}
	
	public BlockStorage getBlock( ChunkCoordinates coords )
	{
		BlockStorage storage = m_blocks.get( coords );
		if( storage == null )
		{
			storage = m_airBlockStorage;
		}
		return storage;
	}
	
	public BoundingBoxInt getBoundingBox( )
	{
		return getGeometry().getEnvelopes().getBoundingBox();
	}
	
	public ShipType getShipType( )
	{
		return ShipType.getByMeta( getShipBlock().meta );
	}
	
	public BlockStorage getShipBlock( )
	{
		BlockStorage block = m_blocks.get( Origin );
		if( block == null )
		{
			throw new ShipConfigurationException( "Ship does not have a ship block!" );
		}
		if( block.id != Ships.m_blockShip.blockID )
		{
			throw new ShipConfigurationException( "Ship origin block is not a ship block!" );
		}
		return block;
	}
}
