package ogresean;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.config.Configuration;

import java.util.*;

public class DeadlyCaves {

    static int fallStoneFrequency = 65;
    static int caveInFrequency = 25;
    static int eruptionFrequency = 10;
    static int fallStoneMagnitude = 1;
    static int caveInMagnitude = 3;
    static int eruptionMagnitude = 2;
    static int chunkRange = 1;
    static Map<Integer, List<ChunkCoordinates>> fallStonesC;
    static Map<Integer, List<Integer>> fallStonesI;
    static Map<Integer, List<ChunkCoordinates>> caveInStonesC;
    static Map<Integer, List<Integer>> caveInStonesI;
    static Map<Integer, List<ChunkCoordinates>> eruptionLavaC;
    static Map<Integer, List<Integer>> eruptionLavaI;
    static Set<Block> stones;
    static Set<Block> pierced;

    public DeadlyCaves() {
        fallStonesC = new HashMap<Integer, List<ChunkCoordinates>>();
        caveInStonesC = new HashMap<Integer, List<ChunkCoordinates>>();
        eruptionLavaC = new HashMap<Integer, List<ChunkCoordinates>>();
        fallStonesI = new HashMap<Integer, List<Integer>>();
        caveInStonesI = new HashMap<Integer, List<Integer>>();
        eruptionLavaI = new HashMap<Integer, List<Integer>>();
        stones = new HashSet<Block>();
        pierced = new HashSet<Block>();
    }

    public void setConfigurationSettings(Configuration config) {
        config.addCustomCategoryComment("Deadly caves", "For Frequencies, the higher the number the more often the event can occur, up to 1000.  A frequency below 1 will make the event never occur.  For Magnitudes, the higher the number the greater the effect of the event.  For example, setting the Cave In Magnitude to 100 will create nearly endless Cave Ins that can make entire cave systems collapse (which is extremely dangerous, but very fun to try to outrun).");
        fallStoneFrequency = config.get("Deadly caves", "Falling stone frequency", 65).getInt();
        caveInFrequency = config.get("Deadly caves", "Cave In frequency", 25).getInt();
        eruptionFrequency = config.get("Deadly caves", "Eruption frequency", 10).getInt();
        fallStoneMagnitude = config.get("Deadly caves", "Falling stone magnitude", 1).getInt();
        caveInMagnitude = config.get("Deadly caves", "Cave In magnitude", 3).getInt();
        eruptionMagnitude = config.get("Deadly caves", "Eruption magnitude", 2).getInt();
        chunkRange = config.get("Deadly caves", "Chunk range", 1, "Range around player(s) where caves events can occur").getInt();
        for (String id : config.get("Deadly caves", "Falling Stones", new String[]{"stone"}, "Blocks that should fall, by block name").getStringList()) {
            stones.add(GameData.getBlockRegistry().getObject(id));
        }
        for (String id : config.get("Deadly caves", "Pierced by lava", new String[]{"dirt", "stone"}, "Blocks that lava can pierce through, by block id").getStringList()) {
            pierced.add(GameData.getBlockRegistry().getObject(id));
        }
        if (stones.size() == 0) {
            fallStoneFrequency = 0;
            caveInFrequency = 0;
        }
        FMLCommonHandler.instance().bus().register(this);
    }

    public void onTickInGame(World currentWorld) {
        if (currentWorld == null || currentWorld.playerEntities.isEmpty()) return;
        //if new world, create new coordinate arraylists
        if (!fallStonesC.containsKey(currentWorld.provider.dimensionId)) {
            fallStonesC.put(currentWorld.provider.dimensionId, new ArrayList<ChunkCoordinates>());
            caveInStonesC.put(currentWorld.provider.dimensionId, new ArrayList<ChunkCoordinates>());
            eruptionLavaC.put(currentWorld.provider.dimensionId, new ArrayList<ChunkCoordinates>());
            fallStonesI.put(currentWorld.provider.dimensionId, new ArrayList<Integer>());
            caveInStonesI.put(currentWorld.provider.dimensionId, new ArrayList<Integer>());
            eruptionLavaI.put(currentWorld.provider.dimensionId, new ArrayList<Integer>());
        }
        List<ChunkCoordinates> fallingStones = fallStonesC.get(currentWorld.provider.dimensionId);
        List<Integer> fallingStonesChance = fallStonesI.get(currentWorld.provider.dimensionId);
        List<ChunkCoordinates> caveinStones = caveInStonesC.get(currentWorld.provider.dimensionId);
        List<Integer> caveinStonesChance = caveInStonesI.get(currentWorld.provider.dimensionId);
        List<ChunkCoordinates> lava = eruptionLavaC.get(currentWorld.provider.dimensionId);
        List<Integer> lavaChance = eruptionLavaI.get(currentWorld.provider.dimensionId);
        if (currentWorld.getWorldTime() % 20 == 0) {
            EntityPlayer player = (EntityPlayer) currentWorld.playerEntities.get(currentWorld.rand.nextInt(currentWorld.playerEntities.size()));
            int i = MathHelper.floor_double(player.posX / 16.0D);
            int k = MathHelper.floor_double(player.posZ / 16.0D);
            if (chunkRange > 0) {
                i += currentWorld.rand.nextInt(chunkRange * 2 + 1) - chunkRange;
                k += currentWorld.rand.nextInt(chunkRange * 2 + 1) - chunkRange;
            }
            Chunk chunk = null;
            if (currentWorld.getChunkProvider().chunkExists(i, k)) {
                chunk = currentWorld.getChunkFromChunkCoords(i, k);
            }
            if (chunk != null && chunk.isChunkLoaded && chunk.isTerrainPopulated) {
                int j = MathHelper.floor_double(player.posY);
                if (j < 65 && currentWorld.rand.nextInt(1000) < fallStoneFrequency)
                    causeFallStone(currentWorld, i * 16, j, k * 16, fallingStones, fallingStonesChance);
                if (j < 33 && currentWorld.rand.nextInt(1000) < caveInFrequency)
                    causeCaveIn(currentWorld, i * 16, j, k * 16, caveinStones, caveinStonesChance);
                if (j < 17 && currentWorld.rand.nextInt(1000) < eruptionFrequency)
                    causeEruption(currentWorld, i * 16, j, k * 16, lava, lavaChance);
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

    public void causeFallStone(World currentWorld, int i, int j, int k, List<ChunkCoordinates> fallingStones, List<Integer> fallingStonesChance) {
        int m = (64 - j) / 10 + 5; //5 to 11
        //find nearby stone blocks with nothing underneath
        for (int a = 0; a < m; a++) {
            int x = i + currentWorld.rand.nextInt(16);
            int y = j + currentWorld.rand.nextInt(12);
            int z = k + currentWorld.rand.nextInt(16);
            if (stones.contains(currentWorld.getBlock(x, y, z)) && currentWorld.isAirBlock(x, y - 1, z)) {
                fallingStones.add(new ChunkCoordinates(x, y, z));
                fallingStonesChance.add(currentWorld.rand.nextInt(30) + 30 + (currentWorld.rand.nextInt(4) + 1) * 100 * fallStoneMagnitude);
                break;
            }
        }
    }

    public void causeCaveIn(World currentWorld, int i, int j, int k, List<ChunkCoordinates> caveinStones, List<Integer> caveinStonesChance) {
        int m = (32 - j) / 5 + 5; //5 to 11
        //find nearby stone blocks with nothing underneath
        for (int a = 0; a < m; a++) {
            int x = i + currentWorld.rand.nextInt(16);
            int y = j + currentWorld.rand.nextInt(12);
            int z = k + currentWorld.rand.nextInt(16);
            if (stones.contains(currentWorld.getBlock(x, y, z)) && currentWorld.isAirBlock(x, y - 1, z)) {
                caveinStones.add(new ChunkCoordinates(x, y, z));
                caveinStonesChance.add(currentWorld.rand.nextInt(320) + 120 + (currentWorld.rand.nextInt(6) + 1) * 1000 * caveInMagnitude);
                break;
            }
        }
    }

    public void causeEruption(World currentWorld, int i, int j, int k, List<ChunkCoordinates> lava, List<Integer> lavaChance) {
        int m = (16 - j) / 2 + 12; //12 to 19
        //find nearby lava source blocks with lava/stone/air above
        for (int a = 0; a < m; a++) {
            int x = i + currentWorld.rand.nextInt(16);
            int y = currentWorld.rand.nextInt(j + 1);
            int z = k + currentWorld.rand.nextInt(16);
            while (currentWorld.getBlock(x, y, z).getMaterial() == Material.lava) {
                y++;
            }
            if (currentWorld.isAirBlock(x, y, z) || pierced.contains(currentWorld.getBlock(x, y, z))) {
                lava.add(new ChunkCoordinates(x, y - 1, z));
                lavaChance.add(currentWorld.rand.nextInt(100) + 60 + (currentWorld.rand.nextInt(5) + 1) * 1000 * eruptionMagnitude);
            }
        }
    }

    public void handleFallStone(World currentWorld, List<ChunkCoordinates> fallingStones, List<Integer> fallingStonesChance) {
        //cycle through each coordinate : value (0-99 = time, 100-1100 = count)
        ChunkCoordinates c;
        Chunk chunk;
        for (int i = 0; i < fallingStones.size(); i++) {
            c = fallingStones.get(i);
            chunk = currentWorld.getChunkFromBlockCoords(c.posX, c.posZ);
            if (chunk == null || !chunk.isChunkLoaded || !stones.contains(currentWorld.getBlock(c.posX, c.posY, c.posZ))) {
                fallingStones.remove(i);
                fallingStonesChance.remove(i);
                i--;
                continue;
            }
            int a = fallingStonesChance.get(i);
            Block block = currentWorld.getBlock(c.posX, c.posY, c.posZ);
            //if value % 100 != 0
            //spawn breaking particles under rock
            if (a % 100 != 0) {
                fallingStonesChance.set(i, a - 1);
                if (currentWorld.rand.nextInt(25) > 0) continue;
                float f = 0.1F;
                double d = (double) c.posX + currentWorld.rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinX();
                double d1 = ((double) c.posY + block.getBlockBoundsMinY()) - (double) f;
                double d2 = (double) c.posZ + currentWorld.rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinZ();
                currentWorld.spawnParticle("blockcrack_" + Block.getIdFromBlock(block) + "_" + currentWorld.getBlockMetadata(c.posX, c.posY, c.posZ), d, d1, d2, 0, -0.4D, 0);
            }
            //else
            //make block breaking sound
            //convert block to fallingSand(stone ID)
            //Check above or side blocks(if valid spot and not in fallStones already) and add them to fallStones with 3 - 5 time, and 1 less count.
            else {
                currentWorld.playSoundEffect((double) c.posX + 0.5F, (double) c.posY - 0.5F, (double) c.posZ + 0.5F, block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch() * 1.5F);
                EntityFallingBlock entityfallingstone = new EntityFallingBlock(currentWorld, (float) c.posX + 0.5F, (float) c.posY + 0.5F, (float) c.posZ + 0.5F, block, currentWorld.getBlockMetadata(c.posX, c.posY, c.posZ));
                currentWorld.spawnEntityInWorld(entityfallingstone);

                byte flags = 0;
                int count = a / 100;
                while (count > 0 && flags != 31) {
                    int num = currentWorld.rand.nextInt(5);
                    //top block
                    if (num == 0) {
                        if ((flags & 1) == 1)
                            num++;
                        else {
                            flags |= 1;
                            if (stones.contains(currentWorld.getBlock(c.posX, c.posY + 1, c.posZ))) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + 1, c.posZ);
                                if (!fallingStones.contains(d)) {
                                    count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 1
                    if (num == 1) {
                        if ((flags & 2) == 2)
                            num++;
                        else {
                            flags |= 2;
                            int yplus = -50;
                            if (stones.contains(currentWorld.getBlock(c.posX + 1, c.posY, c.posZ)) && currentWorld.isAirBlock(c.posX + 1, c.posY - 1, c.posZ))
                                yplus = 0;
                            else if (stones.contains(currentWorld.getBlock(c.posX + 1, c.posY - 1, c.posZ)) && currentWorld.isAirBlock(c.posX + 1, c.posY - 2, c.posZ))
                                yplus = -1;
                            else if (stones.contains(currentWorld.getBlock(c.posX + 1, c.posY + 1, c.posZ)) && currentWorld.isAirBlock(c.posX + 1, c.posY, c.posZ))
                                yplus = 1;

                            if (yplus != -50) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX + 1, c.posY + yplus, c.posZ);

                                if (!fallingStones.contains(d)) {
                                    count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 2
                    if (num == 2) {
                        if ((flags & 4) == 4)
                            num++;
                        else {
                            flags |= 4;
                            int yplus = -50;
                            if (stones.contains(currentWorld.getBlock(c.posX - 1, c.posY, c.posZ)) && currentWorld.isAirBlock(c.posX - 1, c.posY - 1, c.posZ))
                                yplus = 0;
                            else if (stones.contains(currentWorld.getBlock(c.posX - 1, c.posY - 1, c.posZ)) && currentWorld.isAirBlock(c.posX - 1, c.posY - 2, c.posZ))
                                yplus = -1;
                            else if (stones.contains(currentWorld.getBlock(c.posX - 1, c.posY + 1, c.posZ)) && currentWorld.isAirBlock(c.posX - 1, c.posY, c.posZ))
                                yplus = 1;

                            if (yplus != -50) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX - 1, c.posY + yplus, c.posZ);
                                if (!fallingStones.contains(d)) {
                                    count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 3
                    if (num == 3) {
                        if ((flags & 8) == 8)
                            num++;
                        else {
                            flags |= 8;
                            int yplus = -50;
                            if (stones.contains(currentWorld.getBlock(c.posX, c.posY, c.posZ + 1)) && currentWorld.isAirBlock(c.posX, c.posY - 1, c.posZ + 1))
                                yplus = 0;
                            else if (stones.contains(currentWorld.getBlock(c.posX, c.posY - 1, c.posZ + 1)) && currentWorld.isAirBlock(c.posX, c.posY - 2, c.posZ + 1))
                                yplus = -1;
                            else if (stones.contains(currentWorld.getBlock(c.posX, c.posY + 1, c.posZ + 1)) && currentWorld.isAirBlock(c.posX, c.posY, c.posZ + 1))
                                yplus = 1;

                            if (yplus != -50) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + yplus, c.posZ + 1);
                                if (!fallingStones.contains(d)) {
                                    count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 4
                    if (num == 4) {
                        if ((flags & 16) != 16) {
                            flags |= 16;
                            int yplus = -50;
                            if (stones.contains(currentWorld.getBlock(c.posX, c.posY, c.posZ - 1)) && currentWorld.isAirBlock(c.posX, c.posY - 1, c.posZ - 1))
                                yplus = 0;
                            else if (stones.contains(currentWorld.getBlock(c.posX, c.posY - 1, c.posZ - 1)) && currentWorld.isAirBlock(c.posX, c.posY - 2, c.posZ - 1))
                                yplus = -1;
                            else if (stones.contains(currentWorld.getBlock(c.posX, c.posY + 1, c.posZ - 1)) && currentWorld.isAirBlock(c.posX, c.posY, c.posZ - 1))
                                yplus = 1;

                            if (yplus != -50) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + yplus, c.posZ - 1);
                                if (!fallingStones.contains(d)) {
                                    count--;
                                    fallingStones.add(d);
                                    fallingStonesChance.add(count * 100 + currentWorld.rand.nextInt(4) + 4);
                                }
                            }
                        }
                    }
                }
                fallingStones.remove(i);
                fallingStonesChance.remove(i);
                i--;
            }
        }
    }

    public void handleCaveIn(World currentWorld, List<ChunkCoordinates> caveinStones, List<Integer> caveinStonesChance) {
        //cycle through each coordinate : value (0-99 = time, 6000-66000 = count)
        ChunkCoordinates c;
        Chunk chunk;
        for (int i = 0; i < caveinStones.size(); i++) {
            c = caveinStones.get(i);
            chunk = currentWorld.getChunkFromBlockCoords(c.posX, c.posZ);
            if (chunk == null || !chunk.isChunkLoaded || !stones.contains(currentWorld.getBlock(c.posX, c.posY, c.posZ))) {
                caveinStones.remove(i);
                caveinStonesChance.remove(i);
                i--;
                continue;
            }
            int a = caveinStonesChance.get(i);
            Block block = currentWorld.getBlock(c.posX, c.posY, c.posZ);
            //if value % 1000 != 0
            //spawn breaking particles under rock
            if (a % 1000 != 0) {
                caveinStonesChance.set(i, a - 1);
                if (currentWorld.rand.nextInt(20) > 0) continue;
                float f = 0.1F;
                double d = (double) c.posX + currentWorld.rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinX();
                double d1 = ((double) c.posY + block.getBlockBoundsMinY()) - (double) f;
                double d2 = (double) c.posZ + currentWorld.rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinZ();
                currentWorld.spawnParticle("blockcrack_" + Block.getIdFromBlock(block) + "_" + currentWorld.getBlockMetadata(c.posX, c.posY, c.posZ), d, d1, d2, 0, -0.4D, 0);
            }
            //else
            //make block breaking sound
            //convert block to fallingSand(stone ID)
            //Check above or side blocks(if valid spot and not in caveIn already) and add them to caveIn with 3 - 5 time, and 1 less count.
            else {
                currentWorld.playSoundEffect((float) c.posX + 0.5F, (float) c.posY - 0.5F, (float) c.posZ + 0.5F, block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch() * 1.5F);
                EntityFallingBlock entityfallingstone = new EntityFallingBlock(currentWorld, (float) c.posX + 0.5F, (float) c.posY + 0.5F, (float) c.posZ + 0.5F, block, currentWorld.getBlockMetadata(c.posX, c.posY, c.posZ));
                currentWorld.spawnEntityInWorld(entityfallingstone);

                byte flags = 0;
                int count = a / 1000 - 1;
                while (count > 0 && flags != 31) {
                    int num = currentWorld.rand.nextInt(5);
                    //top block
                    if (num == 0) {
                        if ((flags & 1) == 1)
                            num++;
                        else {
                            flags |= 1;
                            if (stones.contains(currentWorld.getBlock(c.posX, c.posY + 1, c.posZ))) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + 1, c.posZ);
                                if (!caveinStones.contains(d)) {
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 1
                    if (num == 1) {
                        if ((flags & 2) == 2)
                            num++;
                        else {
                            flags |= 2;
                            int yplus = -50;
                            if (stones.contains(currentWorld.getBlock(c.posX + 1, c.posY, c.posZ)) && currentWorld.isAirBlock(c.posX + 1, c.posY - 1, c.posZ))
                                yplus = 0;
                            else if (stones.contains(currentWorld.getBlock(c.posX + 1, c.posY - 1, c.posZ)) && currentWorld.isAirBlock(c.posX + 1, c.posY - 2, c.posZ))
                                yplus = -1;
                            else if (stones.contains(currentWorld.getBlock(c.posX + 1, c.posY + 1, c.posZ)) && currentWorld.isAirBlock(c.posX + 1, c.posY, c.posZ))
                                yplus = 1;

                            if (yplus != -50) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX + 1, c.posY + yplus, c.posZ);
                                if (!caveinStones.contains(d)) {
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 2
                    if (num == 2) {
                        if ((flags & 4) == 4)
                            num++;
                        else {
                            flags |= 4;
                            int yplus = -50;
                            if (stones.contains(currentWorld.getBlock(c.posX - 1, c.posY, c.posZ)) && currentWorld.isAirBlock(c.posX - 1, c.posY - 1, c.posZ))
                                yplus = 0;
                            else if (stones.contains(currentWorld.getBlock(c.posX - 1, c.posY - 1, c.posZ)) && currentWorld.isAirBlock(c.posX - 1, c.posY - 2, c.posZ))
                                yplus = -1;
                            else if (stones.contains(currentWorld.getBlock(c.posX - 1, c.posY + 1, c.posZ)) && currentWorld.isAirBlock(c.posX - 1, c.posY, c.posZ))
                                yplus = 1;

                            if (yplus != -50) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX - 1, c.posY + yplus, c.posZ);
                                if (!caveinStones.contains(d)) {
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 3
                    if (num == 3) {
                        if ((flags & 8) == 8)
                            num++;
                        else {
                            flags |= 8;
                            int yplus = -50;
                            if (stones.contains(currentWorld.getBlock(c.posX, c.posY, c.posZ + 1)) && currentWorld.isAirBlock(c.posX, c.posY - 1, c.posZ + 1))
                                yplus = 0;
                            else if (stones.contains(currentWorld.getBlock(c.posX, c.posY - 1, c.posZ + 1)) && currentWorld.isAirBlock(c.posX, c.posY - 2, c.posZ + 1))
                                yplus = -1;
                            else if (stones.contains(currentWorld.getBlock(c.posX, c.posY + 1, c.posZ + 1)) && currentWorld.isAirBlock(c.posX, c.posY, c.posZ + 1))
                                yplus = 1;

                            if (yplus != -50) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + yplus, c.posZ + 1);
                                if (!caveinStones.contains(d)) {
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 4
                    if (num == 4) {
                        if ((flags & 16) != 16) {
                            flags |= 16;
                            int yplus = -50;
                            if (stones.contains(currentWorld.getBlock(c.posX, c.posY, c.posZ - 1)) && currentWorld.isAirBlock(c.posX, c.posY - 1, c.posZ - 1))
                                yplus = 0;
                            else if (stones.contains(currentWorld.getBlock(c.posX, c.posY - 1, c.posZ - 1)) && currentWorld.isAirBlock(c.posX, c.posY - 2, c.posZ - 1))
                                yplus = -1;
                            else if (stones.contains(currentWorld.getBlock(c.posX, c.posY + 1, c.posZ - 1)) && currentWorld.isAirBlock(c.posX, c.posY, c.posZ - 1))
                                yplus = 1;

                            if (yplus != -50) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + yplus, c.posZ - 1);
                                if (!caveinStones.contains(d)) {
                                    caveinStones.add(d);
                                    caveinStonesChance.add(count * 1000 + currentWorld.rand.nextInt(8) + 8);
                                }
                            }
                        }
                    }
                }

                caveinStones.remove(i);
                caveinStonesChance.remove(i);
                i--;
            }
        }
    }

    public void handleEruption(World currentWorld, List<ChunkCoordinates> lava, List<Integer> lavaChance) {
        //cycle through each coordinate : value (0-999 = time, 8000-96000 = count
        ChunkCoordinates c;
        Chunk chunk;
        for (int i = 0; i < lava.size(); i++) {
            c = lava.get(i);
            chunk = currentWorld.getChunkFromBlockCoords(c.posX, c.posZ);
            int a = lavaChance.get(i);
            if (chunk == null || !chunk.isChunkLoaded || !(currentWorld.getBlock(c.posX, c.posY, c.posZ).getMaterial() == Material.lava) || a < 0) {
                lava.remove(i);
                lavaChance.remove(i);
                i--;
                continue;
            }
            //if value % 1000 != 0
            //spawn lava particles above lava or within count block radius above other lava

            if (a % 1000 != 0) {
                lavaChance.set(i, a - 1);
                if (currentWorld.rand.nextInt(18) > 0) continue;
                double d = (float) c.posX + currentWorld.rand.nextFloat();
                double d1 = (double) c.posY + Blocks.flowing_lava.getBlockBoundsMaxY();
                double d2 = (float) c.posZ + currentWorld.rand.nextFloat();
                currentWorld.spawnParticle("lava", d, d1, d2, 0.0D, 0.0D, 0.0D);
            }
            //else
            //make lava sound
            //create lava source block above , and remove this block from map
            //Check above and side lava blocks(if valid spot and not in eruptionLava already) and add them to eruptionLava with 11 - 15 time, and 1 less count.
            else {
                if (currentWorld.rand.nextInt(4) > 0)
                    currentWorld.playSoundEffect((double) ((float) c.posX + 0.5F), ((double) (float) c.posY - 0.5F), ((double) (float) c.posZ + 0.5F), "random.fizz", 0.4F, 0.5F);
                if (currentWorld.isAirBlock(c.posX, c.posY + 1, c.posZ) || pierced.contains(currentWorld.getBlock(c.posX, c.posY + 1, c.posZ)) || currentWorld.getBlock(c.posX, c.posY + 1, c.posZ) == Blocks.lava)
                    currentWorld.setBlock(c.posX, c.posY + 1, c.posZ, Blocks.flowing_lava, 0, 2);
                if (currentWorld.isAirBlock(c.posX + 1, c.posY, c.posZ) || pierced.contains(currentWorld.getBlock(c.posX + 1, c.posY, c.posZ)) || currentWorld.getBlock(c.posX + 1, c.posY, c.posZ) == Blocks.lava)
                    currentWorld.setBlock(c.posX + 1, c.posY, c.posZ, Blocks.flowing_lava, 0, 2);
                if (currentWorld.isAirBlock(c.posX - 1, c.posY, c.posZ) || pierced.contains(currentWorld.getBlock(c.posX - 1, c.posY, c.posZ)) || currentWorld.getBlock(c.posX - 1, c.posY, c.posZ) == Blocks.lava)
                    currentWorld.setBlock(c.posX - 1, c.posY, c.posZ, Blocks.flowing_lava, 0, 2);
                if (currentWorld.isAirBlock(c.posX, c.posY, c.posZ + 1) || pierced.contains(currentWorld.getBlock(c.posX, c.posY, c.posZ + 1)) || currentWorld.getBlock(c.posX, c.posY, c.posZ + 1) == Blocks.lava)
                    currentWorld.setBlock(c.posX, c.posY, c.posZ + 1, Blocks.flowing_lava, 0, 2);
                if (currentWorld.isAirBlock(c.posX, c.posY, c.posZ - 1) || pierced.contains(currentWorld.getBlock(c.posX, c.posY, c.posZ - 1)) || currentWorld.getBlock(c.posX, c.posY, c.posZ - 1) == Blocks.lava)
                    currentWorld.setBlock(c.posX, c.posY, c.posZ - 1, Blocks.flowing_lava, 0, 2);

                byte flags = 0;
                int count = a / 1000 - 1;
                while (count > 0 && flags != 31) {
                    int num = currentWorld.rand.nextInt(5);
                    //top block
                    if (num == 0) {
                        if ((flags & 1) == 1)
                            num++;
                        else {
                            flags |= 1;
                            if (currentWorld.getBlock(c.posX, c.posY + 1, c.posZ) == Blocks.flowing_lava) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY + 1, c.posZ);
                                if (!lava.contains(d)) {
                                    lava.add(d);
                                    lavaChance.add(count * 1000 + currentWorld.rand.nextInt(13) + 13);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 1
                    if (num == 1) {
                        if ((flags & 2) == 2)
                            num++;
                        else {
                            flags |= 2;

                            if (currentWorld.getBlock(c.posX + 1, c.posY, c.posZ) == Blocks.flowing_lava) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX + 1, c.posY + 1, c.posZ);
                                if (!lava.contains(d)) {
                                    lava.add(d);
                                    lavaChance.add((count - currentWorld.rand.nextInt(3)) * 1000 + currentWorld.rand.nextInt(13) + 13);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 2
                    if (num == 2) {
                        if ((flags & 4) == 4)
                            num++;
                        else {
                            flags |= 4;

                            if (currentWorld.getBlock(c.posX - 1, c.posY, c.posZ) == Blocks.flowing_lava) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX - 1, c.posY, c.posZ);
                                if (!lava.contains(d)) {
                                    lava.add(d);
                                    lavaChance.add((count - currentWorld.rand.nextInt(3)) + currentWorld.rand.nextInt(13) + 13);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 3
                    if (num == 3) {
                        if ((flags & 8) == 8)
                            num++;
                        else {
                            flags |= 8;

                            if (currentWorld.getBlock(c.posX, c.posY, c.posZ + 1) == Blocks.flowing_lava) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY, c.posZ + 1);
                                if (!lava.contains(d)) {
                                    lava.add(d);
                                    lavaChance.add((count - currentWorld.rand.nextInt(3)) + currentWorld.rand.nextInt(13) + 13);
                                }
                            }
                            continue;
                        }
                    }
                    //side block 4
                    if (num == 4) {
                        if ((flags & 16) != 16) {
                            flags |= 16;
                            if (currentWorld.getBlock(c.posX, c.posY, c.posZ - 1) == Blocks.flowing_lava) {
                                ChunkCoordinates d = new ChunkCoordinates(c.posX, c.posY, c.posZ - 1);
                                if (!lava.contains(d)) {
                                    lava.add(d);
                                    lavaChance.add((count - currentWorld.rand.nextInt(3)) + currentWorld.rand.nextInt(13) + 13);
                                }
                            }
                        }
                    }
                }
                lava.remove(i);
                lavaChance.remove(i);
                i--;
            }
        }
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.WorldTickEvent event) {
        if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
            onTickInGame(event.world);
        }
    }
}
