package micdoodle8.mods.galacticraft.moon.wgen.dungeon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import micdoodle8.mods.galacticraft.moon.blocks.GCMoonBlocks;
import micdoodle8.mods.galacticraft.moon.wgen.GCMoonBiomeGenBase;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;

public class GCMapGenDungeon {
	
	public static final int DUNGEON_WALL_ID = GCMoonBlocks.blockMoon.blockID;
	public static final int DUNGEON_WALL_META = 14;
	public static final int RANGE = 8;
	public static final int HALLWAY_LENGTH = 16;
	public static final int HALLWAY_HEIGHT = 3;
	
	public static boolean useArrays = false;
	
	public World worldObj;
	
	private ArrayList<GCDungeonRoom> rooms = new ArrayList<GCDungeonRoom>();
	
	public GCMapGenDungeon()
	{
	}
	
	public void generateUsingArrays(World world, long seed, int x, int y, int z, int chunkX, int chunkZ, short[] blocks, byte[] metas)
	{
		ChunkCoordinates dungeonCoords = this.getDungeonNear(seed, chunkX, chunkZ);
		if(dungeonCoords != null)
		{
			this.generate(world, new Random(seed * dungeonCoords.posX * dungeonCoords.posZ * 24789), dungeonCoords.posX, y, dungeonCoords.posZ, chunkX, chunkZ, blocks, metas, true);
		}
	}
	
	public void generateUsingSetBlock(World world, int x, int y, int z)
	{
		this.generate(world, new Random(worldObj.getWorldInfo().getSeed() * x * z * 24789), x, y, z, x, z, null, null, false);
	}
	
	public void generate(World world, Random rand, int x, int y, int z, int chunkX, int chunkZ, short[] blocks, byte[] metas, boolean useArrays)
	{
		this.useArrays = useArrays;
		this.worldObj = world;
		
		List<GCDungeonBoundingBox> boundingBoxes = new ArrayList<GCDungeonBoundingBox>();
		
		int length = rand.nextInt(4) + 5;
		
		GCDungeonRoom currentRoom = GCDungeonRoom.makeRoom(worldObj, rand, x, y, z, 4);
		currentRoom.generate(blocks, metas, chunkX, chunkZ);
		rooms.add(currentRoom);
		
		for(int i = 0; i <= length; i++)
		{
		   tryLoop:
			for(int j = 0; j < 6; j++)
			{
				int offsetX = 0;
				int offsetZ = 0;
				int dir = randDir(rand, currentRoom.entranceDir);
				int entranceDir = dir;
				switch(dir) //East = 0, North = 1, South = 2, West = 3
				{
					case 0: //East z++
						offsetZ = HALLWAY_LENGTH + rand.nextInt(15);
						if(rand.nextBoolean())
						{
							if(rand.nextBoolean())
							{
								entranceDir = 1;
								offsetX = HALLWAY_LENGTH + rand.nextInt(15);
							}
							else
							{
								entranceDir = 2;
								offsetX = -HALLWAY_LENGTH - rand.nextInt(15);
							}
						}
						break;
					case 1: //North x++
						offsetX = HALLWAY_LENGTH + rand.nextInt(15);
						if(rand.nextBoolean())
						{
							if(rand.nextBoolean())
							{
								entranceDir = 0;
								offsetZ = HALLWAY_LENGTH + rand.nextInt(15);
							}
							else
							{
								entranceDir = 3;
								offsetZ = -HALLWAY_LENGTH - rand.nextInt(15);
							}
						}
						break;
					case 2: //South x--
						offsetX = -HALLWAY_LENGTH - rand.nextInt(15);
						if(rand.nextBoolean())
						{
							if(rand.nextBoolean())
							{
								entranceDir = 0;
								offsetZ = HALLWAY_LENGTH + rand.nextInt(15);
							}
							else
							{
								entranceDir = 3;
								offsetZ = -HALLWAY_LENGTH - rand.nextInt(15);
							}
						}
						break;
					case 3: //West z--
						offsetZ = -HALLWAY_LENGTH - rand.nextInt(15);
						if(rand.nextBoolean())
						{
							if(rand.nextBoolean())
							{
								entranceDir = 1;
								offsetX = HALLWAY_LENGTH + rand.nextInt(15);
							}
							else
							{
								entranceDir = 2;
								offsetX = -HALLWAY_LENGTH - rand.nextInt(15);
							}
						}
						break;
					default:
						break;
				}
				
				GCDungeonRoom possibleRoom = GCDungeonRoom.makeRoom(worldObj, rand, currentRoom.posX + offsetX, y, currentRoom.posZ + offsetZ, getOppositeDir(entranceDir));
				if(i == length - 1)
				{
					possibleRoom = GCDungeonRoom.makeBossRoom(worldObj, rand, currentRoom.posX + offsetX, y, currentRoom.posZ + offsetZ, getOppositeDir(entranceDir));
				}
				if(i == length)
				{
					possibleRoom = GCDungeonRoom.makeTreasureRoom(worldObj, rand, currentRoom.posX + offsetX, y, currentRoom.posZ + offsetZ, getOppositeDir(entranceDir));
				}
				GCDungeonBoundingBox possibleRoomBb = possibleRoom.getBoundingBox();
				GCDungeonBoundingBox currentRoomBb = currentRoom.getBoundingBox();
				if(!isIntersecting(possibleRoomBb, boundingBoxes))
				{
					int cx = (currentRoomBb.minX + currentRoomBb.maxX) / 2;
					int cz = (currentRoomBb.minZ + currentRoomBb.maxZ) / 2;
					int px = (possibleRoomBb.minX + possibleRoomBb.maxX) / 2;
					int pz = (possibleRoomBb.minZ + possibleRoomBb.maxZ) / 2;
					int ax = (cx + px) / 2;
					int az = (cz + pz) / 2;
					if(offsetX == 0 || offsetZ == 0) //Only 1 hallway
					{
						GCDungeonBoundingBox corridor1 = null;
						switch(dir) //East = 0, North = 1, South = 2, West = 3
						{
							case 0: //East z++
								corridor1 = new GCDungeonBoundingBox(ax - 1, currentRoomBb.maxZ, ax, possibleRoomBb.minZ - 1);
								break;
							case 1: //North x++
								corridor1 = new GCDungeonBoundingBox(currentRoomBb.maxX, az - 1, possibleRoomBb.minX - 1, az);
								break;
							case 2: //South x--
								corridor1 = new GCDungeonBoundingBox(possibleRoomBb.maxX, az - 1, currentRoomBb.minX - 1, az);
								break;
							case 3: //West z--
								corridor1 = new GCDungeonBoundingBox(ax - 1, possibleRoomBb.maxZ, ax, currentRoomBb.minZ - 1);
								break;
							default:
								break;
						}
						if(!isIntersecting(corridor1, boundingBoxes))
						{
							boundingBoxes.add(possibleRoomBb);
							boundingBoxes.add(corridor1);
							currentRoom = possibleRoom;
							currentRoom.generate(blocks, metas, chunkX, chunkZ);
							rooms.add(currentRoom);
							if(corridor1 != null)
							{
								genCorridor(corridor1, rand, possibleRoom.posY, chunkX, chunkZ, dir, blocks, metas, false);
							}
							break;
						}
						else
						{
							continue tryLoop;
						}
					}
					else //Two Hallways
					{
						GCDungeonBoundingBox corridor1 = null;
						GCDungeonBoundingBox corridor2 = null;
						int dir2 = 0;
						int extraLength = 0;
						if(rand.nextInt(6) == 0)
						{
							extraLength = rand.nextInt(7);
						}
						switch(dir) //East = 0, North = 1, South = 2, West = 3
						{
							case 0: //East z++
								corridor1 = new GCDungeonBoundingBox(cx - 1, currentRoomBb.maxZ, cx + 1, pz - 1);
								if(offsetX > 0) //x++
								{
									corridor2 = new GCDungeonBoundingBox(corridor1.minX - extraLength, corridor1.maxZ + 1, possibleRoomBb.minX, corridor1.maxZ + 3);
									dir2 = 1;
								}
								else //x--
								{
									corridor2 = new GCDungeonBoundingBox(possibleRoomBb.maxX, corridor1.maxZ + 1, corridor1.maxX + extraLength, corridor1.maxZ + 3);
									dir2 = 2;
								}
								break;
							case 1: //North x++
								corridor1 = new GCDungeonBoundingBox(currentRoomBb.maxX, cz - 1, px - 1, cz + 1);
								if(offsetZ > 0) //z++
								{
									corridor2 = new GCDungeonBoundingBox(corridor1.maxX + 1, corridor1.minZ - extraLength, corridor1.maxX + 4, possibleRoomBb.minZ);
									dir2 = 0;
								}
								else //z--
								{
									corridor2 = new GCDungeonBoundingBox(corridor1.maxX + 1, possibleRoomBb.maxZ, corridor1.maxX + 4, corridor1.maxZ + extraLength);
									dir2 = 3;
								}
								break;
							case 2: //South x--
								corridor1 = new GCDungeonBoundingBox(px + 1, cz - 1, currentRoomBb.minX - 1, cz + 1);
								if(offsetZ > 0) //z++
								{
									corridor2 = new GCDungeonBoundingBox(corridor1.minX - 3, corridor1.minZ - extraLength, corridor1.minX - 1, possibleRoomBb.minZ);
									dir2 = 0;
								}
								else //z--
								{
									corridor2 = new GCDungeonBoundingBox(corridor1.minX - 3, possibleRoomBb.maxZ, corridor1.minX - 1, corridor1.maxZ + extraLength);
									dir2 = 3;
								}
								break;
							case 3: //West z--
								corridor1 = new GCDungeonBoundingBox(cx - 1, pz + 1, cx + 1, currentRoomBb.minZ - 1);
								if(offsetX > 0) //x++
								{
									corridor2 = new GCDungeonBoundingBox(corridor1.minX - extraLength, corridor1.minZ - 3, possibleRoomBb.minX, corridor1.minZ - 1);
									dir2 = 1;
								}
								else //x--
								{
									corridor2 = new GCDungeonBoundingBox(possibleRoomBb.maxX, corridor1.minZ - 3, corridor1.maxX + extraLength, corridor1.minZ - 1);
									dir2 = 2;
								}
								break;
							default:
								break;
						}
						if(!isIntersecting(corridor1, boundingBoxes) && !isIntersecting(corridor2, boundingBoxes))
						{
							boundingBoxes.add(possibleRoomBb);
							boundingBoxes.add(corridor1);
							boundingBoxes.add(corridor2);
							currentRoom = possibleRoom;
							currentRoom.generate(blocks, metas, chunkX, chunkZ);
							rooms.add(currentRoom);
							if(corridor1 != null && corridor2 != null)
							{
								genCorridor(corridor2, rand, possibleRoom.posY, chunkX, chunkZ, dir2, blocks, metas, true);
								genCorridor(corridor1, rand, possibleRoom.posY, chunkX, chunkZ, dir, blocks, metas, false);
							}
							break;
						}
						else
						{
							continue tryLoop;
						}				
					}
				}
				else
				{
					continue tryLoop;
				}
			}
		}
	}
	
	private void genCorridor(GCDungeonBoundingBox corridor, Random rand, int y, int cx, int cz, int dir, short[] blocks, byte[] metas, boolean doubleCorridor)
	{
		for(int i = corridor.minX - 1; i <= corridor.maxX + 1; i++)
		{
			for(int k = corridor.minZ - 1; k <= corridor.maxZ + 1; k++)
			{
			   loopj:
				for(int j = y - 1; j <= y + HALLWAY_HEIGHT; j++)
				{
					boolean flag = false;
					switch(dir)
					{
						case 0:
							if((k == corridor.minZ - 1 && !doubleCorridor) || k == corridor.maxZ + 1)
							{
								break loopj;
							}
							if(doubleCorridor && k == corridor.minZ - 1)
							{
								flag = true;
							}
							if(i == corridor.minX - 1 || i == corridor.maxX + 1 || j == y - 1 || j == y + 3)
							{
								flag = true;
							}
							break;
						case 3:
							if(k == corridor.minZ - 1 || (k == corridor.maxZ + 1 && !doubleCorridor))
							{
								break loopj;
							}
							if(doubleCorridor && k == corridor.maxX + 1)
							{
								flag = true;
							}
							if(i == corridor.minX - 1 || i == corridor.maxX + 1 || j == y - 1 || j == y + 3)
							{
								flag = true;
							}
							break;
						case 1:
							if((i == corridor.minX - 1 && !doubleCorridor) || i == corridor.maxX + 1)
							{
								break loopj;
							}
							if(i == corridor.minX - 1)
							{
								flag = true;
							}
							if(k == corridor.minZ - 1 || k == corridor.maxZ + 1 || j == y - 1 || j == y + 3)
							{
								flag = true;
							}
							break;
						case 2:
							if(i == corridor.minX - 1 || (i == corridor.maxX + 1 && !doubleCorridor))
							{
								break loopj;
							}
							if(i == corridor.maxX + 1)
							{
								flag = true;
							}
							if(k == corridor.minZ - 1 || k == corridor.maxZ + 1 || j == y - 1 || j == y + 3)
							{
								flag = true;
							}
							break;
					}
					if(!flag)
					{
						placeBlock(blocks, metas, i, j, k, cx, cz, 0, 0);
					}
					else
					{
						placeBlock(blocks, metas, i, j, k, cx, cz, DUNGEON_WALL_ID, DUNGEON_WALL_META);
					}
				}
				
				if(rand.nextInt(50) == 0)
				{
					placeBlock(blocks, metas, i, y - 1, k, cx, cz, Block.glowStone.blockID, 0);
				}
			}
		}
	}
	
	public void handleTileEntities(Random rand)
	{
		ArrayList<GCDungeonRoom> rooms = new ArrayList<GCDungeonRoom>();
		rooms.addAll(this.rooms);
		for(GCDungeonRoom room : rooms)
		{
			room.handleTileEntities(rand);
		}
	}
	
	protected boolean canGenDungeonAtCoords(long worldSeed, int i, int j)
    {
        byte numChunks = 32;
        byte offsetChunks = 8;
        int oldi = i;
        int oldj = j;

        if (i < 0)
        {
            i -= numChunks - 1;
        }

        if (j < 0)
        {
            j -= numChunks - 1;
        }

        int randX = i / numChunks;
        int randZ = j / numChunks;
        long dungeonSeed = (long)randX * 341873128712L + (long)randZ * 132897987541L + worldSeed + (long)4291726;
        Random rand = new Random(dungeonSeed);
        randX *= numChunks;
        randZ *= numChunks;
        randX += rand.nextInt(numChunks - offsetChunks);
        randZ += rand.nextInt(numChunks - offsetChunks);

        if (oldi == randX && oldj == randZ)
        {
            return true;
        }

        return false;
    }

    public ChunkCoordinates getDungeonNear(long worldSeed, int i, int j) {
        int range = 8;
        for(int x = i - range; x <= i + range; x++) {
            for(int z = j - range; z <= j + range; z++) {
                if (canGenDungeonAtCoords(worldSeed, x, z)) {
                    return new ChunkCoordinates(x * 16 + 8, 0, z * 16 + 8);
                }
            }
        }
        
        return null;
    }
	
	private void placeBlock(short[] blocks, byte[] metas, int x, int y, int z, int cx, int cz, int id, int meta)
	{
		if(useArrays)
		{
			cx *= 16;
			cz *= 16;
			x -= cx;
			z -= cz;
			if(x < 0 || x >= 16 || z < 0 || z >= 16)
			{
				return;
			}
			int index = getIndex(x, y, z);
			blocks[index] = (short) id;
			metas[index] = (byte) meta;
		}
		else
		{
			worldObj.setBlock(x, y, z, id, meta, 3);
		}
	}
	
	private int getOppositeDir(int dir)
	{
		switch(dir)
		{
			case 0:
				return 3;
			case 1:
				return 2;
			case 2:
				return 1;
			case 3:
				return 0;
			default:
				return 5;
		}
	}
	
	private int getIndex(int x, int y, int z)
	{
		return y << 8 | z << 4 | x;
	}
	
	private int randDir(Random rand, int dir)
	{
		int[] dirHelper = new int[dir < 4 ? 3 : 4];
		int k = 0;
		for(int i = 0; i < 4; i++)
		{
			if(i != dir)
			{
				dirHelper[k] = i;
				k++;
			}
		}
		return dirHelper[rand.nextInt(dirHelper.length)];
	}
	
	private boolean isIntersecting(GCDungeonBoundingBox bb, List<GCDungeonBoundingBox> dungeonBbs)
	{
		for(GCDungeonBoundingBox bb2 : dungeonBbs)
		{
			if(bb.isOverlapping(bb2))
			{
				return true;
			}
		}
		return false;
	}
	
}
