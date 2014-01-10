package ogresean;

import java.util.*;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.Configuration;

public class DeadlyCaves implements ITickHandler{
	
	public DeadlyCaves(){
		fallStonesC = new HashMap<Integer, List<ChunkCoordinates>>();
		caveInStonesC = new HashMap<Integer, List<ChunkCoordinates>>();
		eruptionLavaC = new HashMap<Integer, List<ChunkCoordinates>>();
		fallStonesI = new HashMap<Integer, List<Integer>>();
		caveInStonesI = new HashMap<Integer, List<Integer>>();
		eruptionLavaI = new HashMap<Integer, List<Integer>>();
	}
	
	public void setConfigurationSettings(Configuration config){
        config.addCustomCategoryComment("Deadly caves", "For Frequencies, the higher the number the more often the event can occur.  A frequency of 0 will make the event never occur.  For Magnitudes, the higher the number the greater the effect of the event.  For example, setting the Cave In Magnitude to 100 will create nearly endless Cave Ins that can make entire cave systems collapse (which is extremely dangerous, but very fun to try to outrun).");
        fallStoneFrequency = config.get("Deadly caves", "Falling stone frequency", 125).getInt();
        caveInFrequency = config.get("Deadly caves", "Cave In frequency", 55).getInt();
        eruptionFrequency = config.get("Deadly caves", "Eruption frequency", 30).getInt();
        fallStoneMagnitude = config.get("Deadly caves", "Falling stone magnitude", 1).getInt();
        caveInMagnitude = config.get("Deadly caves", "Cave In magnitude", 3).getInt();
        eruptionMagnitude = config.get("Deadly caves", "Eruption magnitude", 6).getInt();
        TickRegistry.registerTickHandler(this, Side.SERVER);
    }
	
	public void onTickInGame(World currentWorld){
        if(currentWorld == null) return;
		//if new world, create new coordinate arraylists
		if(!fallStonesC.containsKey(currentWorld.provider.dimensionId)){
			fallStonesC.put(currentWorld.provider.dimensionId , new ArrayList<ChunkCoordinates>());
			caveInStonesC.put(currentWorld.provider.dimensionId , new ArrayList<ChunkCoordinates>());
			eruptionLavaC.put(currentWorld.provider.dimensionId , new ArrayList<ChunkCoordinates>());
			fallStonesI.put(currentWorld.provider.dimensionId , new ArrayList<Integer>());
			caveInStonesI.put(currentWorld.provider.dimensionId , new ArrayList<Integer>());
			eruptionLavaI.put(currentWorld.provider.dimensionId , new ArrayList<Integer>());
		}
        List<ChunkCoordinates> fallingStones = fallStonesC.get(currentWorld.provider.dimensionId);
        List<Integer> fallingStonesChance = fallStonesI.get(currentWorld.provider.dimensionId);
        List<ChunkCoordinates> caveinStones = caveInStonesC.get(currentWorld.provider.dimensionId);
        List<Integer> caveinStonesChance = caveInStonesI.get(currentWorld.provider.dimensionId);
        List<ChunkCoordinates> lava = eruptionLavaC.get(currentWorld.provider.dimensionId);
        List<Integer> lavaChance = eruptionLavaI.get(currentWorld.provider.dimensionId);
		if(currentWorld.getWorldTime() % 20 == 0){
            for (Object obj:currentWorld.activeChunkSet) {
                ChunkCoordIntPair chunkIntPair = (ChunkCoordIntPair) obj;
                int i = chunkIntPair.chunkXPos * 16;
                int k = chunkIntPair.chunkZPos * 16;
                Chunk chunk = null;
                if (currentWorld.getChunkProvider().chunkExists(chunkIntPair.chunkXPos, chunkIntPair.chunkZPos)) {
                    chunk = currentWorld.getChunkFromChunkCoords(chunkIntPair.chunkXPos, chunkIntPair.chunkZPos);
                }
                if (chunk != null && chunk.isChunkLoaded && chunk.isTerrainPopulated) {
                    int j = currentWorld.rand.nextInt(64);
			        if(currentWorld.rand.nextInt(1000) < fallStoneFrequency)
                        causeFallStone(currentWorld, i, j, k, fallingStones, fallingStonesChance);
			        if(currentWorld.rand.nextInt(1000) < caveInFrequency)
                        causeCaveIn(currentWorld, i, (int)j/2, k, caveinStones, caveinStonesChance);
			        if(currentWorld.rand.nextInt(1000) < eruptionFrequency)
                        causeEruption(currentWorld, i, (int)j/4, k, lava, lavaChance);
                }
            }
		}
		handleFallStone(currentWorld, fallingStones, fallingStonesChance);
		handleCaveIn(currentWorld, caveinStones, caveinStonesChance);
		handleEruption(currentWorld, lava, lavaChance);
        fallStonesC.put(currentWorld.provider.dimensionId, fallingStones);
        fallStonesI.put(currentWorld.provider.dimensionId, fallingStonesChance);
        caveInStonesC.put(currentWorld.provider.dimensionId, caveinStones);
        caveInStonesI.put(currentWorld.provider.dimensionId, caveinStonesChance);
        eruptionLavaC.put(currentWorld.provider.dimensionId, lava);
        eruptionLavaI.put(currentWorld.provider.dimensionId, lavaChance);
	}
	
	public void causeFallStone(World currentWorld, int i, int j, int k, List<ChunkCoordinates> fallingStones, List<Integer> fallingStonesChance){
        int m = (64 - j) / 10 + 5; //5 to 11
        //find nearby stone blocks with nothing underneath
        for(int a = 0; a < m; a++){
        	int x = i + currentWorld.rand.nextInt(16);
        	int y = j + currentWorld.rand.nextInt(24) - 12;
        	int z = k + currentWorld.rand.nextInt(16);
        	if(y < 4) y += 12;
        	if(currentWorld.getBlockId(x, y, z) == Block.stone.blockID && currentWorld.isAirBlock(x, y-1, z)){
        		fallingStones.add(new ChunkCoordinates(x, y, z));
        		fallingStonesChance.add(currentWorld.rand.nextInt(30) + 30 + (currentWorld.rand.nextInt(4) + 1) * 100 * fallStoneMagnitude);
        		break;
        	}
        }
	}
	
	public void causeCaveIn(World currentWorld, int i, int j, int k, List<ChunkCoordinates> caveinStones, List<Integer> caveinStonesChance){
        int m = (32 - j) / 5 + 5; //5 to 11
        //find nearby stone blocks with nothing underneath
        for(int a = 0; a < m; a++){
            int x = i + currentWorld.rand.nextInt(16);
            int y = j + currentWorld.rand.nextInt(24) - 12;
            int z = k + currentWorld.rand.nextInt(16);
        	if(y < 4) y += 12;
        	if(currentWorld.getBlockId(x, y, z) == Block.stone.blockID && currentWorld.isAirBlock(x, y-1, z)){
        		caveinStones.add(new ChunkCoordinates(x, y, z));
        		caveinStonesChance.add(currentWorld.rand.nextInt(320) + 120 + (currentWorld.rand.nextInt(6) + 1) * 1000 * caveInMagnitude);
        		break;
        	}
        }
	}
	
	public void causeEruption(World currentWorld, int i, int j, int k, List<ChunkCoordinates> lava, List<Integer> lavaChance){
        int m = (16 - j) / 2 + 12; //12 to 19
        //find nearby lava source blocks with lava/stone/air above
        for(int a = 0; a < m; a++){
        	int x = i + currentWorld.rand.nextInt(16);
        	int y = j + currentWorld.rand.nextInt(10) - 5;
        	int z = k + currentWorld.rand.nextInt(16);
            if(y < 4) y += 5;
        	if(currentWorld.getBlockId(x, y, z) == Block.lavaMoving.blockID && (currentWorld.isAirBlock(x, y + 1, z) || currentWorld.getBlockId(x, y + 1, z) == Block.stone.blockID || currentWorld.getBlockId(x, y + 1, z) == Block.lavaMoving.blockID || currentWorld.getBlockId(x, y + 1, z) == Block.lavaStill.blockID)){
        		lava.add(new ChunkCoordinates(x, y, z));
        		lavaChance.add(currentWorld.rand.nextInt(100) + 60 + (currentWorld.rand.nextInt(5) + 1) * 1000 * eruptionMagnitude);
        	}
        }
	}
	
	public void handleFallStone(World currentWorld, List<ChunkCoordinates> fallingStones, List<Integer> fallingStonesChance){
		//cycle through each coordinate : value (0-99 = time, 100-1100 = count)
        ChunkCoordinates c;
        Block block;
        for(int i = 0; i < fallingStones.size(); i++)
        {
            int a = fallingStonesChance.get(i);
            c = fallingStones.get(i);
            if(currentWorld.getBlockId(c.posX, c.posY, c.posZ) != Block.stone.blockID){
                fallingStones.remove(i);
                fallingStonesChance.remove(i);
                i--;
                continue;
            }
			//if value % 100 != 0
				//spawn breaking particles under rock
            if(a % 100 != 0){
                fallingStonesChance.set(i, a - 1);
            	if(currentWorld.rand.nextInt(25) > 0) continue;
            	block = Block.stone;
                float f = 0.1F;
                double d = (double)c.posX + currentWorld.rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinX();
                double d1 = ((double)c.posY + block.getBlockBoundsMinY()) - (double)f;
                double d2 = (double)c.posZ + currentWorld.rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinZ();
                currentWorld.spawnParticle("tilecrack_"+block.blockID+"_0", d, d1, d2, 0, 0, 0);
                /*EntityFX digfx = (new EntityDiggingFX(currentWorld, d, d1, d2, 0.0D, 0.0D, 0.0D, block, 0, 0)).func_4041_a(c.posX, c.posY, c.posZ).func_407_b(0.2F).func_405_d(0.6F);
                digfx.renderDistanceWeight = 16D;
                currentWorld.effectRenderer.addEffect(digfx);*/
            }
			//else
			//make block breaking sound
			//convert block to fallingSand(stone ID)
			//Check above or side blocks(if valid spot and not in fallStones already) and add them to fallStones with 3 - 5 time, and 1 less count. 
            else{
        		block = Block.stone;
                currentWorld.playSound((double)c.posX + 0.5F, (double)c.posY - 0.5F, (double)c.posZ + 0.5F, block.stepSound.stepSoundName, (block.stepSound.getVolume() + 2.0F) / 8F, block.stepSound.getPitch() * 0.5F, false);
        		currentWorld.setBlockToAir(c.posX, c.posY, c.posZ);
        		EntityFallingSand entityfallingstone = new EntityFallingSand(currentWorld, (float)c.posX + 0.5F, (float)c.posY + 0.5F, (float)c.posZ + 0.5F, Block.stone.blockID);
                currentWorld.spawnEntityInWorld(entityfallingstone);
                
                byte flags = 0;
                int count = a / 100;
                while(count > 0 && flags != 31){
                	int num = currentWorld.rand.nextInt(5);
                	//top block
                	if(num == 0){
                		if((flags & 1) == 1)
                			num++;
                		else{
                			flags |= 1;
                			if(currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ) == Block.stone.blockID){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + 1, c.posZ);
                				if(!fallingStones.contains(d)){
                					count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 1
                	if(num == 1){
                		if((flags & 2) == 1)
                			num++;
                		else{
                			flags |= 2;
                			int yplus = -50;
                			if(currentWorld.getBlockId(c.posX + 1, c.posY, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX + 1, c.posY - 1, c.posZ) == 0)
                				yplus = 0;
                			else if(currentWorld.getBlockId(c.posX + 1, c.posY - 1, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX + 1, c.posY - 2, c.posZ) == 0)
                				yplus = -1;
                			else if(currentWorld.getBlockId(c.posX + 1, c.posY + 1, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX + 1, c.posY, c.posZ) == 0)
                				yplus = 1;
                			
                			if(yplus != -50){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX + 1, c.posY + yplus, c.posZ);

                				if(!fallingStones.contains(d)){
                					count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 2
                	if(num == 2){
                		if((flags & 4) == 1)
                			num++;
                		else{
                			flags |= 4;
                			int yplus = -50;
                			if(currentWorld.getBlockId(c.posX - 1, c.posY, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX - 1, c.posY - 1, c.posZ) == 0)
                				yplus = 0;
                			else if(currentWorld.getBlockId(c.posX - 1, c.posY - 1, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX - 1, c.posY - 2, c.posZ) == 0)
                				yplus = -1;
                			else if(currentWorld.getBlockId(c.posX - 1, c.posY + 1, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX - 1, c.posY, c.posZ) == 0)
                				yplus = 1;
                			
                			if(yplus != -50){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX - 1, c.posY + yplus, c.posZ);
                				if(!fallingStones.contains(d)){
                					count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 3
                	if(num == 3){
                		if((flags & 8) == 1)
                			num++;
                		else{
                			flags |= 8;
                			int yplus = -50;
                			if(currentWorld.getBlockId(c.posX, c.posY, c.posZ + 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY - 1, c.posZ + 1) == 0)
                				yplus = 0;
                			else if(currentWorld.getBlockId(c.posX, c.posY - 1, c.posZ + 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY - 2, c.posZ + 1) == 0)
                				yplus = -1;
                			else if(currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ + 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY, c.posZ + 1) == 0)
                				yplus = 1;
                			
                			if(yplus != -50){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + yplus, c.posZ + 1);
                				if(!fallingStones.contains(d)){
                					count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 4
                	if(num == 4){
                		if((flags & 16) == 1)
                			num = 0;
                		else{
                			flags |= 16;
                			int yplus = -50;
                			if(currentWorld.getBlockId(c.posX, c.posY, c.posZ - 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY - 1, c.posZ - 1) == 0)
                				yplus = 0;
                			else if(currentWorld.getBlockId(c.posX, c.posY - 1, c.posZ - 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY - 2, c.posZ - 1) == 0)
                				yplus = -1;
                			else if(currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ - 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY, c.posZ - 1) == 0)
                				yplus = 1;
                			
                			if(yplus != -50){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + yplus, c.posZ - 1);
                				if(!fallingStones.contains(d)){
                					count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                				}
                			}
                			continue;
                		}
                	}
                }
                fallingStones.remove(i);
                fallingStonesChance.remove(i);
        	i--;
            }
        }
	}
	
	public void handleCaveIn(World currentWorld, List<ChunkCoordinates> caveinStones, List<Integer> caveinStonesChance){
		//cycle through each coordinate : value (0-99 = time, 6000-66000 = count)
        ChunkCoordinates c;
        Block block;
        for(int i = 0; i < caveinStones.size(); i++)
        {
        	int a = caveinStonesChance.get(i);
            c = caveinStones.get(i);
            if(currentWorld.getBlockId(c.posX, c.posY, c.posZ) != Block.stone.blockID){
                caveinStones.remove(i);
                caveinStonesChance.remove(i);
                i--;
                continue;
            }
        		
			//if value % 1000 != 0
				//spawn breaking particles under rock
            if(a % 1000 != 0){
                caveinStonesChance.set(i, a - 1);
            	if(currentWorld.rand.nextInt(20) > 0) continue;
            	block = Block.stone;
                float f = 0.1F;
                double d = (double)c.posX + currentWorld.rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinX();
                double d1 = ((double)c.posY + block.getBlockBoundsMinY()) - (double)f;
                double d2 = (double)c.posZ + currentWorld.rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinZ() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinZ();
                currentWorld.spawnParticle("tilecrack_"+block.blockID+"_0", d, d1, d2, 0, 0, 0);
                /*EntityFX digfx = (new EntityDiggingFX(currentWorld, d, d1, d2, 0.0D, 0.0D, 0.0D, block, 0, 0)).func_4041_a(c.posX, c.posY, c.posZ).func_407_b(0.2F).func_405_d(0.6F);
                digfx.renderDistanceWeight = 16D;
                game.effectRenderer.addEffect(digfx);*/
            }
			//else
			//make block breaking sound
			//convert block to fallingSand(stone ID)
			//Check above or side blocks(if valid spot and not in caveIn already) and add them to caveIn with 3 - 5 time, and 1 less count. 
            else{
            	block = Block.stone;
        		currentWorld.playSound((float)c.posX + 0.5F, (float)c.posY - 0.5F, (float)c.posZ + 0.5F, block.stepSound.stepSoundName, (block.stepSound.getVolume() + 4.0F) / 8F, block.stepSound.getPitch() * 0.5F, false);
        		currentWorld.setBlockToAir(c.posX, c.posY, c.posZ);
        		EntityFallingSand entityfallingstone = new EntityFallingSand(currentWorld, (float)c.posX + 0.5F, (float)c.posY + 0.5F, (float)c.posZ + 0.5F, Block.stone.blockID);
                currentWorld.spawnEntityInWorld(entityfallingstone);
                
                byte flags = 0;
                int count = a / 1000 - 1;
                while(count > 0 && flags != 31){
                	int num = currentWorld.rand.nextInt(5);
                	//top block
                	if(num == 0){
                		if((flags & 1) == 1)
                			num++;
                		else{
                			flags |= 1;
                			if(currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ) == Block.stone.blockID){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + 1, c.posZ);
                				if(!caveinStones.contains(d)){
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 1
                	if(num == 1){
                		if((flags & 2) == 1)
                			num++;
                		else{
                			flags |= 2;
                			int yplus = -50;
                			if(currentWorld.getBlockId(c.posX + 1, c.posY, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX + 1, c.posY - 1, c.posZ) == 0)
                				yplus = 0;
                			else if(currentWorld.getBlockId(c.posX + 1, c.posY - 1, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX + 1, c.posY - 2, c.posZ) == 0)
                				yplus = -1;
                			else if(currentWorld.getBlockId(c.posX + 1, c.posY + 1, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX + 1, c.posY, c.posZ) == 0)
                				yplus = 1;
                			
                			if(yplus != -50){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX + 1, c.posY + yplus, c.posZ);
                				if(!caveinStones.contains(d)){
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 2
                	if(num == 2){
                		if((flags & 4) == 1)
                			num++;
                		else{
                			flags |= 4;
                			int yplus = -50;
                			if(currentWorld.getBlockId(c.posX - 1, c.posY, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX - 1, c.posY - 1, c.posZ) == 0)
                				yplus = 0;
                			else if(currentWorld.getBlockId(c.posX - 1, c.posY - 1, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX - 1, c.posY - 2, c.posZ) == 0)
                				yplus = -1;
                			else if(currentWorld.getBlockId(c.posX - 1, c.posY + 1, c.posZ) == Block.stone.blockID && currentWorld.getBlockId(c.posX - 1, c.posY, c.posZ) == 0)
                				yplus = 1;
                			
                			if(yplus != -50){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX - 1, c.posY + yplus, c.posZ);
                				if(!caveinStones.contains(d)){
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 3
                	if(num == 3){
                		if((flags & 8) == 1)
                			num++;
                		else{
                			flags |= 8;
                			int yplus = -50;
                			if(currentWorld.getBlockId(c.posX, c.posY, c.posZ + 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY - 1, c.posZ + 1) == 0)
                				yplus = 0;
                			else if(currentWorld.getBlockId(c.posX, c.posY - 1, c.posZ + 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY - 2, c.posZ + 1) == 0)
                				yplus = -1;
                			else if(currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ + 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY, c.posZ + 1) == 0)
                				yplus = 1;
                			
                			if(yplus != -50){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + yplus, c.posZ + 1);
                				if(!caveinStones.contains(d)){
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 4
                	if(num == 4){
                		if((flags & 16) == 1)
                			num = 0;
                		else{
                			flags |= 16;
                			int yplus = -50;
                			if(currentWorld.getBlockId(c.posX, c.posY, c.posZ - 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY - 1, c.posZ - 1) == 0)
                				yplus = 0;
                			else if(currentWorld.getBlockId(c.posX, c.posY - 1, c.posZ - 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY - 2, c.posZ - 1) == 0)
                				yplus = -1;
                			else if(currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ - 1) == Block.stone.blockID && currentWorld.getBlockId(c.posX, c.posY, c.posZ - 1) == 0)
                				yplus = 1;
                			
                			if(yplus != -50){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + yplus, c.posZ - 1);
                				if(!caveinStones.contains(d)){
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                				}
                			}
                			continue;
                		}
                	}
                }

                caveinStones.remove(i);
                caveinStonesChance.remove(i);
            	i--;
        	}
        }
	}
	
	public void handleEruption(World currentWorld, List<ChunkCoordinates> lava, List<Integer> lavaChance){
		//cycle through each coordinate : value (0-999 = time, 8000-96000 = count
        ChunkCoordinates c;
        Block block;
        for(int i = 0; i < lava.size(); i++)
        {
        	int a = lavaChance.get(i);
            c = lava.get(i);
            if(currentWorld.getBlockId(c.posX, c.posY, c.posZ) != Block.lavaMoving.blockID && currentWorld.getBlockId(c.posX, c.posY, c.posZ) != Block.lavaStill.blockID || a < 0){
                lava.remove(i);
                lavaChance.remove(i);
            	i--;
            	continue;
            }
			//if value % 1000 != 0
			//spawn lava particles above lava or within count block radius above other lava
            
            if(a % 1000 != 0){
                lavaChance.set(i, a - 1);
            	if(currentWorld.rand.nextInt(18) > 0) continue;
            	double d = (float)c.posX + currentWorld.rand.nextFloat();
                double d1 = (double)c.posY + Block.lavaMoving.getBlockBoundsMaxY();
                double d2 = (float)c.posZ + currentWorld.rand.nextFloat();
                currentWorld.spawnParticle("lava", d, d1, d2, 0.0D, 0.0D, 0.0D);
                /*EntityFX lavafx = new EntityLavaFX(currentWorld, d, d1, d2);
                lavafx.renderDistanceWeight = 16D;
                game.effectRenderer.addEffect(lavafx);*/
            }
			//else
			//make lava sound
			//create lava source block above , and remove this block from map
			//Check above and side lava blocks(if valid spot and not in eruptionLava already) and add them to eruptionLava with 11 - 15 time, and 1 less count. 
            else{
            	if(currentWorld.rand.nextInt(4) > 0)
                    currentWorld.playSoundEffect((double) ((float) c.posX + 0.5F), ((double) (float) c.posY - 0.5F), ((double) (float) c.posZ + 0.5F), "random.fizz", 0.4F, 0.5F);
        		if(currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ) == 0 || currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ) == Block.stone.blockID || currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ) == Block.lavaMoving.blockID || currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ) == Block.lavaStill.blockID)
        			currentWorld.setBlock(c.posX, c.posY + 1, c.posZ, Block.lavaMoving.blockID, 0, 2);
        		if(currentWorld.getBlockId(c.posX + 1, c.posY, c.posZ) == 0 || currentWorld.getBlockId(c.posX + 1, c.posY, c.posZ) == Block.stone.blockID || currentWorld.getBlockId(c.posX + 1, c.posY, c.posZ) == Block.lavaMoving.blockID || currentWorld.getBlockId(c.posX + 1, c.posY, c.posZ) == Block.lavaStill.blockID)
        			currentWorld.setBlock(c.posX + 1, c.posY, c.posZ, Block.lavaMoving.blockID, 0, 2);
        		if(currentWorld.getBlockId(c.posX - 1, c.posY, c.posZ) == 0 || currentWorld.getBlockId(c.posX - 1, c.posY, c.posZ) == Block.stone.blockID || currentWorld.getBlockId(c.posX - 1, c.posY, c.posZ) == Block.lavaMoving.blockID || currentWorld.getBlockId(c.posX - 1, c.posY, c.posZ) == Block.lavaStill.blockID)
        			currentWorld.setBlock(c.posX - 1, c.posY, c.posZ, Block.lavaMoving.blockID, 0, 2);
        		if(currentWorld.getBlockId(c.posX, c.posY, c.posZ + 1) == 0 || currentWorld.getBlockId(c.posX, c.posY, c.posZ + 1) == Block.stone.blockID || currentWorld.getBlockId(c.posX, c.posY, c.posZ + 1) == Block.lavaMoving.blockID || currentWorld.getBlockId(c.posX, c.posY, c.posZ + 1) == Block.lavaStill.blockID)
        			currentWorld.setBlock(c.posX, c.posY, c.posZ + 1, Block.lavaMoving.blockID, 0, 2);
        		if(currentWorld.getBlockId(c.posX, c.posY, c.posZ - 1) == 0 || currentWorld.getBlockId(c.posX, c.posY, c.posZ - 1) == Block.stone.blockID || currentWorld.getBlockId(c.posX, c.posY, c.posZ - 1) == Block.lavaMoving.blockID || currentWorld.getBlockId(c.posX, c.posY, c.posZ - 1) == Block.lavaStill.blockID)
        			currentWorld.setBlock(c.posX, c.posY, c.posZ - 1, Block.lavaMoving.blockID, 0, 2);

                byte flags = 0;
                int count = a / 1000 - 1;
                while(count > 0 && flags != 31){
                	int num = currentWorld.rand.nextInt(5);
                	//top block
                	if(num == 0){
                		if((flags & 1) == 1)
                			num++;
                		else{
                			flags |= 1;
                			if(currentWorld.getBlockId(c.posX, c.posY + 1, c.posZ) == Block.lavaMoving.blockID){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + 1, c.posZ);
                				if(!lava.contains(d)){
                                    lava.add(d);
                                    lavaChance.add(count * 1000 + currentWorld.rand.nextInt(13) + 13);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 1
                	if(num == 1){
                		if((flags & 2) == 1)
                			num++;
                		else{
                			flags |= 2;

                			if(currentWorld.getBlockId(c.posX + 1, c.posY, c.posZ) == Block.lavaMoving.blockID){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX + 1, c.posY + 1, c.posZ);
                				if(!lava.contains(d)){
                                    lava.add(d);
                                    lavaChance.add((count - currentWorld.rand.nextInt(3)) * 1000 + currentWorld.rand.nextInt(13) + 13);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 2
                	if(num == 2){
                		if((flags & 4) == 1)
                			num++;
                		else{
                			flags |= 4;

                			if(currentWorld.getBlockId(c.posX - 1, c.posY, c.posZ) == Block.lavaMoving.blockID){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX - 1, c.posY, c.posZ);
                				if(!lava.contains(d)){
                                    lava.add(d);
                                    lavaChance.add((count - currentWorld.rand.nextInt(3)) + currentWorld.rand.nextInt(13) + 13);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 3
                	if(num == 3){
                		if((flags & 8) == 1)
                			num++;
                		else{
                			flags |= 8;

                			if(currentWorld.getBlockId(c.posX, c.posY, c.posZ + 1) == Block.lavaMoving.blockID){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY, c.posZ + 1);
                				if(!lava.contains(d)){
                                    lava.add(d);
                                    lavaChance.add((count - currentWorld.rand.nextInt(3)) + currentWorld.rand.nextInt(13) + 13);
                				}
                			}
                			continue;
                		}
                	}
                	//side block 4
                	if(num == 4){
                		if((flags & 16) == 1)
                			num = 0;
                		else{
                			flags |= 16;
                			if(currentWorld.getBlockId(c.posX, c.posY, c.posZ - 1) == Block.lavaMoving.blockID){
                				ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY, c.posZ - 1);
                				if(!lava.contains(d)){
                                    lava.add(d);
                                    lavaChance.add((count - currentWorld.rand.nextInt(3)) + currentWorld.rand.nextInt(13) + 13);
                				}
                			}
                			continue;
                		}
                	}
                }
                lava.remove(i);
                lavaChance.remove(i);
                i--;
                
        	}
        }
	}
	
	static int fallStoneFrequency = 100;
	static int caveInFrequency = 100;
	static int eruptionFrequency = 100;
	static int fallStoneMagnitude = 1;
	static int caveInMagnitude = 3;
	static int eruptionMagnitude = 6;
    static Map<Integer, List<ChunkCoordinates>> fallStonesC;
	static Map<Integer, List<Integer>> fallStonesI;
	static Map<Integer, List<ChunkCoordinates>> caveInStonesC;
	static Map<Integer, List<Integer>> caveInStonesI;
	static Map<Integer, List<ChunkCoordinates>> eruptionLavaC;
	static Map<Integer, List<Integer>> eruptionLavaI;

    @Override
    public void tickStart(EnumSet<TickType> tickTypes, Object... data) {

    }

    @Override
    public void tickEnd(EnumSet<TickType> tickTypes, Object... data) {
        onTickInGame((World) data[0]);
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.WORLD);
    }

    @Override
    public String getLabel() {
        return "Deadly caves tick";
    }
}
